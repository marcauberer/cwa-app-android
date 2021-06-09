package de.rki.coronawarnapp.covidcertificate.cose

import org.joda.time.Instant

data class HealthCertificateHeader(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
)
