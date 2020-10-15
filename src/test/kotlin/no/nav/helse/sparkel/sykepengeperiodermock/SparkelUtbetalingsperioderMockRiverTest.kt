package no.nav.helse.sparkel.sykepengeperiodermock

import no.nav.helse.rapids_rivers.asOptionalLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate

@TestInstance(Lifecycle.PER_CLASS)
internal class SparkelUtbetalingsperioderMockRiverTest {

    private val testrapid = TestRapid()
    private val fødselsnummer = "10101012345"
    private val orgnummer = "991024000"

    init {
        SparkelUtbetalingsperioderMockRiver(testrapid, mutableMapOf(
            fødselsnummer to listOf(Utbetalingsperiode(
                fom = LocalDate.of(2020, 6, 1),
                tom = LocalDate.of(2020, 6, 30),
                dagsats = 1234.0,
                grad = "100",
                typetekst = "Utbetaling",
                organisasjonsnummer = orgnummer
            ))
        ))
    }

    @Test
    fun `løser behov med tom liste om vi ikke matcher på fnr`() {
        testrapid.sendTestMessage(enkeltBehov("nytt-fnr"))
        val løsning = testrapid.inspektør.løsning("HentInfotrygdutbetalinger")
        assertFalse(løsning.isMissingOrNull())
        assertEquals(0, løsning.size())
    }

    @Test
    fun `løser behov med perioder om vi ikke matcher på fnr og har data`() {
        testrapid.sendTestMessage(enkeltBehov(fødselsnummer))
        val løsning = testrapid.inspektør.løsning("HentInfotrygdutbetalinger")
        assertFalse(løsning.isMissingOrNull())
        assertEquals(1, løsning.size())

        val utbetalingsperiode = løsning.first()
        assertEquals(LocalDate.of(2020, 6, 1), utbetalingsperiode["fom"].asOptionalLocalDate())
        assertEquals(LocalDate.of(2020, 6, 30), utbetalingsperiode["tom"].asOptionalLocalDate())
        assertEquals(utbetalingsperiode["dagsats"].asDouble(), 1234.0)
        assertEquals(utbetalingsperiode["grad"].asText(), "100")
        assertEquals(utbetalingsperiode["typetekst"].asText(), "Utbetaling")
        assertEquals(utbetalingsperiode["organisasjonsnummer"].asText(), orgnummer)
    }

    private fun enkeltBehov(fødselsnummer: String) =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentInfotrygdutbetalinger" ],
            "@id" : "id",
            "@opprettet" : "2020-05-18",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "$fødselsnummer",
            "HentInfotrygdutbetalinger": {
                "historikkFom": "2017-10-15",
                "historikkTom": "2020-10-15"
            }
        }
        """
}
