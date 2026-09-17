package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.BtcPayProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BtcPaySignatureVerifierTest {

    private val secret = "top-secret"
    private val body = """{"type":"InvoiceSettled","invoiceId":"abc"}"""
    private lateinit var verifier: BtcPaySignatureVerifier

    @BeforeEach
    fun setUp() {
        val props = BtcPayProperties()
        props.webhookSecret = secret
        verifier = BtcPaySignatureVerifier(props)
    }

    @Test
    fun acceptsSignatureProducedWithTheSharedSecret() {
        assertTrue(verifier.isValid(body, "sha256=" + sign(body, secret)))
    }

    @Test
    fun acceptsSignatureWithoutThePrefix() {
        assertTrue(verifier.isValid(body, sign(body, secret)))
    }

    @Test
    fun rejectsSignatureFromAnotherSecret() {
        assertFalse(verifier.isValid(body, "sha256=" + sign(body, "wrong-secret")))
    }

    @Test
    fun rejectsWhenTheBodyWasTamperedWith() {
        val signature = "sha256=" + sign(body, secret)
        assertFalse(verifier.isValid("""{"type":"InvoiceSettled","invoiceId":"other"}""", signature))
    }

    @Test
    fun rejectsMissingSignature() {
        assertFalse(verifier.isValid(body, null))
        assertFalse(verifier.isValid(body, ""))
    }

    @Test
    fun rejectsEverythingWhenNoSecretIsConfigured() {
        val props = BtcPayProperties()
        val unconfigured = BtcPaySignatureVerifier(props)
        assertFalse(unconfigured.isValid(body, "sha256=" + sign(body, secret)))
    }

    private fun sign(payload: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
