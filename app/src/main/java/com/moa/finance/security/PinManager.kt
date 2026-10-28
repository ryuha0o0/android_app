package com.moa.finance.security

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * 간편 비밀번호(6자리) 로그인.
 *
 * 흐름: 최초 금융인증서 로그인 후 6자리 PIN을 등록 → 이후 PIN으로 인증서(개인키) 사용을 잠금 해제해 로그인.
 * PIN 자체는 로그인 수단이 아니라 "인증서를 여는 열쇠"입니다(로그인은 챌린지-응답 서명으로 완료).
 *
 * 6자리는 경우의 수가 100만뿐이라 약합니다. 반드시 다음을 함께 적용합니다:
 *  - 저장: 원문 금지. PBKDF2(솔트·10만 반복) 검증자만 암호화 저장. (WEAK_PIN_STORAGE 켜면 솔트 없는 SHA-256)
 *  - 시도 제한: 5회 실패 시 잠금. (NO_PIN_LOCKOUT 켜면 무제한 — 취약)
 *  - 기기 바인딩: 등록한 기기에서만 유효. (NO_DEVICE_BINDING 공유)
 */
class PinManager(context: Context) {

    private val prefs = context.getSharedPreferences("pin_store", Context.MODE_PRIVATE)
    private val secure = SecureStore(context)
    private val rng = SecureRandom()

    private var failures = 0
    private var lockedUntil = 0L
    private val maxAttempts = 5
    private val lockMillis = 60_000L

    fun isRegistered(): Boolean = prefs.contains(KEY_VERIFIER)

    private fun currentDeviceUuid(): String = secure.getString(SecureStore.KEY_DEVICE_UUID) ?: ""

    fun register(pin: String) {
        require(pin.length == 6 && pin.all(Char::isDigit))
        val salt = ByteArray(16).also { rng.nextBytes(it) }
        val verifier = derive(pin, salt)
        prefs.edit()
            .putString(KEY_VERIFIER, verifier)
            .putString(KEY_SALT, Base64.getEncoder().encodeToString(salt))
            .putString(KEY_DEVICE, currentDeviceUuid())
            .apply()
        failures = 0
        lockedUntil = 0L
    }

    fun isLockedOut(): Boolean =
        !VulnConfig.isVulnerable(VulnConfig.Flag.NO_PIN_LOCKOUT) && System.currentTimeMillis() < lockedUntil

    fun lockRemainingSeconds(): Long = ((lockedUntil - System.currentTimeMillis()).coerceAtLeast(0L)) / 1000L

    /** PIN 검증. 성공 시 true (호출부가 이어서 인증서 로그인으로 세션을 만든다). */
    fun verify(pin: String): Boolean {
        if (isLockedOut()) return false
        // 기기 바인딩 확인
        if (!VulnConfig.isVulnerable(VulnConfig.Flag.NO_DEVICE_BINDING)) {
            val bound = prefs.getString(KEY_DEVICE, "") ?: ""
            if (bound.isNotEmpty() && bound != currentDeviceUuid()) return false
        }
        val salt = Base64.getDecoder().decode(prefs.getString(KEY_SALT, "") ?: "")
        val expected = prefs.getString(KEY_VERIFIER, "") ?: ""
        val ok = constantTimeEquals(derive(pin, salt), expected)
        if (ok) {
            failures = 0
            lockedUntil = 0L
        } else if (!VulnConfig.isVulnerable(VulnConfig.Flag.NO_PIN_LOCKOUT)) {
            failures++
            if (failures >= maxAttempts) {
                lockedUntil = System.currentTimeMillis() + lockMillis
                failures = 0
            }
        }
        return ok
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun derive(pin: String, salt: ByteArray): String {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.WEAK_PIN_STORAGE)) {
            // 취약: 솔트 없는 단일 해시 → 레인보우 테이블로 6자리 역산 쉬움
            val md = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return Base64.getEncoder().encodeToString(md)
        }
        // 안전: PBKDF2-HMAC-SHA256, 솔트 + 10만 반복
        val spec = PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(key)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val x = a.toByteArray(); val y = b.toByteArray()
        if (x.size != y.size) return false
        var r = 0
        for (i in x.indices) r = r or (x[i].toInt() xor y[i].toInt())
        return r == 0
    }

    private companion object {
        const val KEY_VERIFIER = "pin_verifier"
        const val KEY_SALT = "pin_salt"
        const val KEY_DEVICE = "pin_device"
    }
}
