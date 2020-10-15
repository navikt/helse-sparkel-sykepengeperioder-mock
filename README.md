# Sykepengeperioder-mock
![Bygg og deploy](https://github.com/navikt/helse-sparkel-sykepengeperioder-mock/workflows/Bygg%20og%20deploy/badge.svg)

## Beskrivelse
Vi sliter til tider med å hente ut sykepengeperioder fra Infotrygd i test. Derfor deployer vi en mock slik at vi ikke blokkerer testing.
 Det er også mye lettere å rigge forskjellige testcaser via mocken fremfor data i infotrygd.

#### Bruk:
Appen holder på et in-memory map over ønsket svar på sykepengehistorikk gitt et fødselsnummer. Dette kan endres med to apikall som nås via autoforward eller manuell port forward (boomer mode):
- `POST /reset`: Fjerner all eksisterende konfigurasjon
- `POST /sykepengehistorikk/{fødselsnummer}`: Lagrer et ønsket svar på oppslag gitt et fødselsnummer.

Forventer en payload som oppfyller:
```
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
```

ála

```
{
  "Sykepengehistorikk": [
    {
      "inntektsopplysninger": [
        {
          "sykepengerFom": "2020-06-01",
          "inntekt": 25000.0,
          "orgnummer": "990000000",
          "refusjonTom": null,
          "refusjonTilArbeidsgiver": true
        }
      ],
      "utbetalteSykeperioder": [
        {
          "fom": "2020-06-01",
          "tom": "2020-06-25",
          "utbetalingsGrad": "100",
          "oppgjorsType": "",
          "utbetalt": "2020-06-30",
          "dagsats": 1234.0,
          "typeKode": "0",
          "typeTekst": "Utbetaling",
          "orgnummer": "990000000"
        }
      ],
      "maksDato": "2021-05-30"
    }
  ]
}
```

- `POST /utbetalingshistorikk/{fødselsnummer}`: Lagrer et ønsket svar på oppslag gitt et fødselsnummer.

Forventer en payload som oppfyller:

```
data class Utbetalingsperiode(
    val fom: LocalDate?,
    val tom: LocalDate?,
    val dagsats: Double,
    val grad: String,
    val typetekst: String,
    val organisasjonsnummer: String
)
```

ála

```
{
  [
    {
        "fom": "2020-06-01",
        "tom": "2020-06-25",
        "dagsats" = 1234.0,
        "grad" = "100",
        "typetekst" = "Utbetaling",
        "organisasjonsnummer" = orgnummer
    }
  ]
}
```

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #område-helse.
