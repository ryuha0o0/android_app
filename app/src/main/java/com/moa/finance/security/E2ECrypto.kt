package com.moa.finance.security

import java.security.PublicKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import java.security.spec.MGF1ParameterSpec

/**
 * E2E(종단간) 필드 암호화 — TLS 위에 한 겹 더.
 *
 * 비밀번호·간편비번·계좌번호 같은 민감 필드를 앱에서 암호화해 보내고, 서버 앱단에서만 복호화합니다.
 * → TLS를 종료하는 중간 프록시·WAF·접근로그에도 평문이 남지 않습니다. (D-47,51 / B-15)
 *
 * 하이브리드 방식:
 *  1) 랜덤 AES-256-GCM 키로 평문을 암호화(기밀성+무결성)
 *  2) 그 AES 키를 서버 RSA 공개키(OAEP-SHA256)로 감싼다
 *  3) {encKey, iv, ciphertext, keyId} 를 전송 → 서버가 RSA 개인키로 AES 키를 풀어 복호화
 *
 * VulnConfig.NO_E2E_ENCRYPTION 이 켜지면 암호화하지 않고 평문을 그대로 담습니다(실습용).
 */
object E2ECrypto {

    /** 전송용 봉투. plaintext 가 채워져 있으면 암호화를 적용하지 않은 평문 상태(취약). */
    data class Envelope(
        val encrypted: Boolean,
        val keyId: String,
        val encKey: String = "",      // RSA(OAEP)로 감싼 AES 키 (Base64)
        val iv: String = "",          // GCM nonce (Base64)
        val ciphertext: String = "",  // AES-GCM 암호문 (Base64)
        val plaintext: String = "",   // 취약 모드: 평문 그대로
    )

    private val rng = SecureRandom()
    private val b64 = Base64.getEncoder()

    fun encryptField(plain: String, serverKey: PublicKey, keyId: String): Envelope {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.NO_E2E_ENCRYPTION)) {
            return Envelope(encrypted = false, keyId = keyId, plaintext = plain)
        }
        // 1) AES-256-GCM
        val aesKey = KeyGenerator.getInstance("AES").apply { init(256, rng) }.generateKey()
        val iv = ByteArray(12).also { rng.nextBytes(it) }
        val gcm = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(128, iv))
        }
        val ct = gcm.doFinal(plain.toByteArray(Charsets.UTF_8))
        // 2) RSA-OAEP(SHA-256) 로 AES 키 봉인
        val oaep = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
        val rsa = Cipher.getInstance("RSA/ECB/OAEPPadding").apply {
            init(Cipher.ENCRYPT_MODE, serverKey, oaep)
        }
        val encKey = rsa.doFinal(aesKey.encoded)
        return Envelope(
            encrypted = true,
            keyId = keyId,
            encKey = b64.encodeToString(encKey),
            iv = b64.encodeToString(iv),
            ciphertext = b64.encodeToString(ct),
        )
    }
}
