package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asOptionalLocalDate
import no.nav.helse.sparkel.sykepengeperiodermock.SparkelSykepengeperioderMockRiver
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.sykepengeperiodermock.Inntektsopplysning
import no.nav.helse.sparkel.sykepengeperiodermock.Sykepengehistorikk
import no.nav.helse.sparkel.sykepengeperiodermock.UtbetalteSykepengeperioder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate

@TestInstance(Lifecycle.PER_CLASS)
internal class SparkelSykepengeperioderMockRiverTest {

    private val testrapid = TestRapid()
    private val fødselsnummer = "10101012345"
    private val orgnummer = "991024000"

    init {
        SparkelSykepengeperioderMockRiver(testrapid, mutableMapOf(
            fødselsnummer to listOf(Sykepengehistorikk(
                inntektsopplysninger = listOf(Inntektsopplysning(
                    sykepengerFom = LocalDate.of(2020, 6, 1),
                    inntekt = 25000.0,
                    orgnummer = orgnummer,
                    refusjonTom = null,
                    refusjonTilArbeidsgiver = true
                )),
                utbetalteSykepengeperioder = listOf(UtbetalteSykepengeperioder(
                    fom = LocalDate.of(2020,6,1),
                    tom = LocalDate.of(2020,6,30),
                    utbetalingsGrad = "100",
                    oppgjorsType = "",
                    dagsats = 1234.0,
                    utbetalt = LocalDate.of(2020,6,30),
                    typeKode = "0",
                    typeTekst = "Utbetaling",
                    orgnummer = orgnummer
                )),
                maksDato = LocalDate.of(2021, 5,30)
            ))
        ))
    }

    @Test
    fun `løser behov med tom liste om vi ikke matcher på fnr`() {
        testrapid.sendTestMessage(enkeltBehov("nytt-fnr"))
        val løsning = testrapid.inspektør.løsning("Sykepengehistorikk")
        assertFalse(løsning.isMissingOrNull())
        assertEquals(0, løsning.size())
    }

    @Test
    fun `løser behov med perioder om vi ikke matcher på fnr og har data`() {
        testrapid.sendTestMessage(enkeltBehov(fødselsnummer))
        val løsning = testrapid.inspektør.løsning("Sykepengehistorikk")
        assertFalse(løsning.isMissingOrNull())
        assertEquals(1, løsning.size())
        assertEquals(1, løsning.first()["inntektsopplysninger"].size())
        assertEquals(1, løsning.first()["utbetalteSykepengeperioder"].size())
        assertEquals(LocalDate.of(2021, 5,30), løsning.first()["maksDato"].asOptionalLocalDate())
    }

    private fun enkeltBehov(fødselsnummer: String) =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Sykepengehistorikk" ],
            "@id" : "id",
            "@opprettet" : "2020-05-18",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "$fødselsnummer",
            "Sykepengehistorikk": {
                "historikkFom" : "2020-01-18",
                "historikkTom" : "2020-05-17"
            }
        }
        """
}

fun TestRapid.RapidInspector.meldinger() =
    (0 until size).map { index -> message(index) }

fun TestRapid.RapidInspector.hendelser(type: String) =
    meldinger().filter { it.path("@event_name").asText() == type }

fun TestRapid.RapidInspector.løsning(behov: String) =
    hendelser("behov")
        .filter { it.hasNonNull("@løsning") }
        .last { it.path("@behov").map(JsonNode::asText).contains(behov) }
        .path("@løsning").path(behov)
