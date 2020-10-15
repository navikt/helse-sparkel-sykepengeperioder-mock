package no.nav.helse.sparkel.sykepengeperiodermock

import java.time.LocalDate

data class Sykepengehistorikk(
    val inntektsopplysninger: List<Inntektsopplysning>,
    val utbetalteSykepengeperioder: List<UtbetalteSykepengeperioder>,
    val maksDato: LocalDate?
)

data class Inntektsopplysning(
    val sykepengerFom: LocalDate,
    val inntekt: Double,
    val orgnummer: String,
    val refusjonTom: LocalDate?,
    val refusjonTilArbeidsgiver: Boolean
)

data class UtbetalteSykepengeperioder(
    val fom: LocalDate?,
    val tom: LocalDate?,
    val utbetalingsGrad: String,
    val oppgjorsType: String,
    val utbetalt: LocalDate?,
    val dagsats: Double,
    val typeKode: String,
    val typeTekst: String,
    val orgnummer: String
)
