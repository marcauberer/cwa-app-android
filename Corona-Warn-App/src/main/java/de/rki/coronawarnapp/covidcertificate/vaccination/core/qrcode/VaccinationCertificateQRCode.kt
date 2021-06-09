package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate.VaccinationCertificateData

data class VaccinationCertificateQRCode(
    val qrCodeString: QrCodeString,
    val parsedData: VaccinationCertificateData,
) {
    val uniqueCertificateIdentifier: String
        get() = parsedData.certificate.vaccinationDatas.single().uniqueCertificateIdentifier
}

typealias QrCodeString = String
