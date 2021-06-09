package de.rki.coronawarnapp.covidcertificate.test.core.certificate

import de.rki.coronawarnapp.covidcertificate.cose.HealthCertificateHeader

data class TestCertificateData(
    val header: HealthCertificateHeader,
    val certificate: TestCertificateDccV1,
)
