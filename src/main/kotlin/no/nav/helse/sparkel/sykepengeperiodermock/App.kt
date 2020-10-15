package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val applicationBuilder = ApplicationBuilder()
    applicationBuilder.start()
}

private val log = LoggerFactory.getLogger("SparkelSykepengerMock")
private val svarSykepengehistorikk = mutableMapOf<String, List<Sykepengehistorikk>>()
private val svarUtbetalingsperioder = mutableMapOf<String, List<Utbetalingsperiode>>()

class ApplicationBuilder : RapidsConnection.StatusListener {
    private val rapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(System.getenv())).withKtorModule {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
            routing {
                post("/reset") {
                    log.info("Fjerner alle konfigurerte sykepengeperioder")
                    svarSykepengehistorikk.clear()
                    svarUtbetalingsperioder.clear()
                    call.respond(HttpStatusCode.OK)
                }
                post("/sykepengehistorikk/{fødselsnummer}") {
                    val fødselsnummer = call.parameters["fødselsnummer"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Requesten mangler fødselsnummer"
                    )

                    val utbetalteSykeperiode = try {
                        call.receive<List<Sykepengehistorikk>>()
                    } catch (e: ContentTransformationException) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Kunne ikke parse payload")
                    }
                    svarSykepengehistorikk[fødselsnummer] = utbetalteSykeperiode
                    log.info("Oppdatererte mocket sykepengehistorikk for fnr: ${fødselsnummer.substring(4)}*******")
                    call.respond(HttpStatusCode.OK)
                }
                post("/utbetalingshistorikk/{fødselsnummer}") {
                    val fødselsnummer = call.parameters["fødselsnummer"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Requesten mangler fødselsnummer"
                    )

                    val utbetalteSykeperiode = try {
                        call.receive<List<Utbetalingsperiode>>()
                    } catch (e: ContentTransformationException) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Kunne ikke parse payload")
                    }
                    svarUtbetalingsperioder[fødselsnummer] = utbetalteSykeperiode
                    log.info("Oppdatererte mocket utbetalingshistorikk for fnr: ${fødselsnummer.substring(4)}*******")
                    call.respond(HttpStatusCode.OK)
                }
            }
        }.build()

    init {
        rapidsConnection.register(this)
        SparkelSykepengeperioderMockRiver(rapidsConnection, svarSykepengehistorikk)
        SparkelUtbetalingsperioderMockRiver(rapidsConnection, svarUtbetalingsperioder)
    }

    fun start() {
        rapidsConnection.start()
    }
}
