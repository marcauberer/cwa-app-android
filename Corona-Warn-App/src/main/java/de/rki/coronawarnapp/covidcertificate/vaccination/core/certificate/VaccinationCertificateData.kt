package de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate

import de.rki.coronawarnapp.covidcertificate.cose.HealthCertificateHeader

/**
 * Represents the parsed data from the QR code
 */
data class VaccinationCertificateData(
    val header: HealthCertificateHeader,
    val certificate: VaccinationDGCV1,
)
