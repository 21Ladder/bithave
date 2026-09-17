package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.BtcPayProperties
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Verifies the BTCPay-Sig header, which is an HMAC-SHA256 over the raw request body
@Component
class BtcPaySignatureVerifier(private val btcPayProperties: BtcPayProperties) {

    fun isValid(rawBody: String, signatureHeader: String?): Boolean {
        val secret = btcPayProperties.webhookSecret
        if (secret.isBlank() || signatureHeader.isNullOrBlank()) {
            return false
        }

        val provided = signatureHeader.removePrefix("sha256=").trim()
        val expected = hmacSha256Hex(rawBody, secret)

        // Constant time compare, a plain equals would leak the signature byte by byte
        return MessageDigest.isEqual(
            provided.lowercase().toByteArray(StandardCharsets.UTF_8),
            expected.toByteArray(StandardCharsets.UTF_8)
        )
    }

    private fun hmacSha256Hex(payload: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val digest = mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
