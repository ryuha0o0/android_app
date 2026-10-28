package com.moa.finance.security

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import java.security.spec.MGF1ParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

/**
 * 로컬 모의 서버 — 실제 서버가 없으므로 "서버가 해야 할 검증"을 기기 안에서 흉내 냅니다.
 * 암호 연산은 진짜(ECDSA 검증, RSA/AES 복호화)이고, 네트워크만 로컬입니다. 운영 코드가 아닙니다.
 *
 * 역할:
 *  - 로그인 챌린지 발급/검증 (챌린지-응답, 1회성) : /auth/challenge, /auth/login/certificate
 *  - 기기 공개키 등록 (가입 시) : /devices
 *  - E2E 공개키 제공 + 서버측 복호화 : /crypto/e2e-key
 */
object MockBackend {

    private val rng = SecureRandom()
    private val b64e = Base64.getEncoder()
    private val b64d = Base64.getDecoder()

    // 등록된 기기 공개키 (userId -> EC PublicKey)
    private val registeredKeys = ConcurrentHashMap<String, PublicKey>()
    // 발급한 챌린지 (challengeId -> bytes), 1회성
    private val challenges = ConcurrentHashMap<String, ByteArray>()

    // 서버 E2E RSA 키쌍 (앱 시작 시 1회 생성)
    private val e2eKeyPair: KeyPair by lazy {
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048, rng) }.generateKeyPair()
    }
    const val E2E_KEY_ID = "e2e-v1"

    fun e2ePublicKey(): PublicKey = e2eKeyPair.public

    /** 가입 시 기기 공개키 등록. publicKeyBase64 는 Keystore 가 내보낸 X.509 인코딩. */
    fun registerDeviceKey(userId: String, publicKeyBase64: String) {
        val spec = X509EncodedKeySpec(b64d.decode(publicKeyBase64))
        val kf = java.security.KeyFactory.getInstance("EC")
        registeredKeys[userId] = kf.generatePublic(spec)
    }

    fun hasRegisteredKey(userId: String): Boolean = registeredKeys.containsKey(userId)

    /** 로그인용 1회성 챌린지 발급. */
    fun issueChallenge(): Pair<String, ByteArray> {
        val id = "chal-" + b64e.encodeToString(ByteArray(9).also { rng.nextBytes(it) })
        val challenge = ByteArray(32).also { rng.nextBytes(it) }
        challenges[id] = challenge
        return id to challenge
    }

    /** 기기 개인키로 서명한 값을 등록 공개키로 검증. 성공 시 챌린지를 소비(재사용 차단)하고 true. */
    fun verifyLogin(userId: String, challengeId: String, signatureBase64: String): Boolean {
        val challenge = challenges.remove(challengeId) ?: return false // 소비됨/만료
        val pub = registeredKeys[userId] ?: return false
        return runCatching {
            Signature.getInstance("SHA256withECDSA").run {
                initVerify(pub)
                update(challenge)
                verify(b64d.decode(signatureBase64))
            }
        }.getOrDefault(false)
    }

    /** E2E 봉투를 서버 RSA 개인키로 복호화(검증·시연용). 평문을 돌려줍니다. */
    fun e2eDecrypt(env: E2ECrypto.Envelope): String {
        if (!env.encrypted) return env.plaintext // 취약: 평문 그대로 들어옴
        val oaep = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
        val aesKeyBytes = Cipher.getInstance("RSA/ECB/OAEPPadding").run {
            init(Cipher.DECRYPT_MODE, e2eKeyPair.private, oaep)
            doFinal(b64d.decode(env.encKey))
        }
        val plain = Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKeyBytes, "AES"), GCMParameterSpec(128, b64d.decode(env.iv)))
            doFinal(b64d.decode(env.ciphertext))
        }
        return String(plain, Charsets.UTF_8)
    }
}
