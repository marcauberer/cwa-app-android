package de.rki.coronawarnapp.covidcertificate.common.decoder

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.AES_DECRYPTION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_NO_ALG
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_NO_SIGN1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_PH_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_UNKNOWN_ALG
import de.rki.coronawarnapp.util.encoding.base64
import timber.log.Timber
import javax.inject.Inject

class DccCoseDecoder @Inject constructor(
    private val aesEncryptor: AesCryptography
) {

    fun decode(input: RawCOSEObject): Message = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val message = Message(
            payload = messageObject.extractPayload(),
            kid = messageObject.extractKid()
        )
        message
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID, e)
    }

    fun decodeDscMessage(input: RawCOSEObject): DscMessage = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val protectedHeader = messageObject.extractProtectedHeader()
        DscMessage(
            protectedHeader = protectedHeader,
            unprotectedHeader = messageObject.extractUnprotectedHeader(),
            payload = messageObject.extractPayload(),
            kid = messageObject.extractKid(),
            signature = messageObject.extractSignature(),
            alg = protectedHeader.extractAlgorithm()
        )
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID, e)
    }

    fun decryptMessage(input: RawCOSEObject, decryptionKey: ByteArray): RawCOSEObject = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val content = messageObject[2].GetByteString()
        val decrypted = content.decrypt(decryptionKey)
        messageObject[2] = CBORObject.FromObject(decrypted)
        messageObject.EncodeToBytes()
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun CBORObject.extractProtectedHeader(): CBORObject = try {
        this[1]
    } catch (e: Exception) {
        throw InvalidHealthCertificateException(HC_COSE_PH_INVALID)
    }

    private fun CBORObject.extractUnprotectedHeader(): CBORObject? = try {
        this[2]
    } catch (e: Exception) {
        Timber.e(e, "extractUnprotectedHeader failed")
        null
    }

    private fun CBORObject.extractPayload(): CBORObject {
        val content = this[2].GetByteString()
        return CBORObject.DecodeFromBytes(content)
    }

    private fun CBORObject.extractSignature(): ByteArray = try {
        this[3].GetByteString()
    } catch (e: Exception) {
        throw InvalidHealthCertificateException(HC_COSE_NO_SIGN1)
    }

    private fun CBORObject.extractAlgorithm(): String {
        val algo = try {
            this[1].AsInt32()
        } catch (e: Exception) {
            throw InvalidHealthCertificateException(HC_COSE_NO_ALG)
        }

        return when (algo) {
            -7 -> "ES256"
            -37 -> "PS256"
            else -> throw InvalidHealthCertificateException(HC_COSE_UNKNOWN_ALG)
        }
    }

    private fun CBORObject.extractKid(): String {
        val elementFour = try {
            val protectedHeader = this[0]
            CBORObject.DecodeFromBytes(protectedHeader?.GetByteString()).get(4)!!
        } catch (e: Exception) {
            this[1]?.get(4)
        }
        return elementFour?.GetByteString()?.base64() ?: ""
    }

    private fun ByteArray.decrypt(decryptionKey: ByteArray) = try {
        aesEncryptor.decrypt(
            decryptionKey = decryptionKey,
            encryptedData = this
        )
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(AES_DECRYPTION_FAILED)
    }

    private fun CBORObject.validate(): CBORObject {
        if (size() != 4) {
            throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
        }
        if (!HasTag(18)) {
            throw InvalidHealthCertificateException(HC_COSE_TAG_INVALID)
        }
        return this
    }

    data class Message(
        val payload: CBORObject,
        val kid: String
    )

    data class DscMessage(
        val protectedHeader: CBORObject,
        val unprotectedHeader: CBORObject?,
        val payload: CBORObject,
        val kid: String,
        val signature: ByteArray,
        val alg: Int,
    )
}
