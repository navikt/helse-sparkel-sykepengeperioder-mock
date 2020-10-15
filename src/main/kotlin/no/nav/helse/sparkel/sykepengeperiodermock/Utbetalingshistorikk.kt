package no.nav.helse.sparkel.sykepengeperiodermock

import java.time.LocalDate

data class Utbetalingsperiode(
    val fom: LocalDate?,
    val tom: LocalDate?,
    val dagsats: Double,
    val grad: String,
    val typetekst: String,
    val organisasjonsnummer: String
)
