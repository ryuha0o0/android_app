package com.moa.finance.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.Base64

/**
 * 생체인증(기기)·금융인증서 로그인에서 "개인키" 역할을 하는 하드웨어 보안키.
 *
 * 설계 의도(안전한 기준, API 5~10):
 *  - 개인키는 Android Keystore(가능하면 StrongBox/TEE) 안에서만 생성·사용되고
 *    앱이 바이트로 꺼낼 수 없습니다. 서버에는 공개키만 등록합니다.
 *  - 로그인/거래 시 서버가 준 1회성 challenge(난수)를 개인키로 서명해 보냅니다.
 *    → 인증정보 재사용·고정(replay/fixation) 방지. (D-22,23,63)
 *  - 생체인증은 "비밀번호 대신" 이 개인키 사용을 잠금 해제하는 수단입니다.
 *    생체정보 자체는 기기를 떠나지 않습니다.
 */
object KeystoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "moa_device_signing_key"
    private const val SIGN_ALGO = "SHA256withECDSA"

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun hasKey(): Boolean = keyStore().containsAlias(ALIAS)

    /** 기기 서명키(EC P-256) 생성. 이미 있으면 그대로 둠. requireBiometric=true면 생체 인증 후에만 서명 가능. */
    fun ensureKey(requireBiometric: Boolean = false): PublicKey {
        val ks = keyStore()
        if (ks.containsAlias(ALIAS)) {
            return ks.getCertificate(ALIAS).publicKey
        }
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE,
        )
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .apply {
                if (requireBiometric) {
                    setUserAuthenticationRequired(true)
                    // 매 서명마다 생체 인증 요구. API 30+ 는 세분화 파라미터, 이하 호환 경로.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    } else {
                        @Suppress("DEPRECATION")
                        setUserAuthenticationValidityDurationSeconds(-1)
                    }
                }
            }
            .build()
        generator.initialize(spec)
        return generator.generateKeyPair().public
    }

    fun publicKeyBase64(): String? {
        if (!hasKey()) return null
        val pub = keyStore().getCertificate(ALIAS).publicKey
        return Base64.getEncoder().encodeToString(pub.encoded)
    }

    /** challenge 를 기기 개인키로 서명. 반환값(Base64)을 서버가 등록된 공개키로 검증. */
    fun sign(challenge: ByteArray): String {
        val entry = keyStore().getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        val privateKey: PrivateKey = entry.privateKey
        val signature = Signature.getInstance(SIGN_ALGO).apply {
            initSign(privateKey)
            update(challenge)
        }
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    fun deleteKey() {
        val ks = keyStore()
        if (ks.containsAlias(ALIAS)) ks.deleteEntry(ALIAS)
    }
}
