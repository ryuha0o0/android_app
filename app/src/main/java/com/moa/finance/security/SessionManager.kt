package com.moa.finance.security

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * 세션·토큰·자동로그인·기기 바인딩·로그인 시도 제한을 한곳에서 관리.
 *
 * 점검항목: D-22,26,30,49,50,62 / A-14 / B-10,12 / C-07,08 / E-F14,S07,S08
 *
 * 여러 취약 시나리오를 토글로 비교합니다.
 *  - WEAK_SESSION_ID   : 순차 증가 세션 ID(예측 가능) vs 128bit 난수
 *  - PREDICTABLE_UUID  : 시간 기반 UUID vs randomUUID
 *  - NO_DEVICE_BINDING : 토큰에 기기 바인딩 미포함(타기기 재사용) vs 바인딩 검증
 *  - NO_LOGIN_LOCKOUT  : 실패 횟수 제한 없음 vs 5회 초과 잠금
 *  - NO_SESSION_TIMEOUT: 만료 없음 vs 유휴 10분 만료
 *  - PLAINTEXT_AUTOLOGIN: 자동로그인 토큰 평문 저장(SecureStore 참조)
 */
class SessionManager(private val context: Context) {

    private val store = SecureStore(context)
    private val secureRandom = SecureRandom()
    private val seq = AtomicLong(1000L)

    var currentSessionId by mutableStateOf<String?>(null)
        private set
    var boundDeviceUuid by mutableStateOf<String?>(null)
        private set
    private var lastActivityAt: Long = 0L

    private var failedAttempts = 0
    private var lockedUntil = 0L

    private val maxAttempts = 5
    private val lockMillis = 60_000L
    private val idleTimeoutMillis = 10 * 60_000L

    // ── 기기 UUID (기기 바인딩 식별자) ───────────────────────────────
    fun deviceUuid(): String {
        store.getString(SecureStore.KEY_DEVICE_UUID)?.let { return it }
        val uuid = if (VulnConfig.isVulnerable(VulnConfig.Flag.PREDICTABLE_UUID)) {
            // 취약: 시간+순번 기반으로 예측·충돌 가능
            "DEV-" + System.currentTimeMillis() + "-" + seq.getAndIncrement()
        } else {
            // 안전: 암호학적 난수 UUID v4
            UUID.randomUUID().toString()
        }
        store.putString(SecureStore.KEY_DEVICE_UUID, uuid)
        return uuid
    }

    // ── 세션 ID 발급 ────────────────────────────────────────────────
    private fun newSessionId(): String =
        if (VulnConfig.isVulnerable(VulnConfig.Flag.WEAK_SESSION_ID)) {
            // 취약: 순차 증가 → 다음 세션 ID 추측 가능
            "SESS-" + seq.getAndIncrement()
        } else {
            // 안전: 128bit 난수
            val bytes = ByteArray(16).also { secureRandom.nextBytes(it) }
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }

    // ── 로그인 시도 제한 ────────────────────────────────────────────
    fun isLockedOut(): Boolean {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.NO_LOGIN_LOCKOUT)) return false
        return System.currentTimeMillis() < lockedUntil
    }

    fun lockRemainingSeconds(): Long =
        ((lockedUntil - System.currentTimeMillis()).coerceAtLeast(0L)) / 1000L

    fun recordFailure() {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.NO_LOGIN_LOCKOUT)) return
        failedAttempts++
        if (failedAttempts >= maxAttempts) {
            lockedUntil = System.currentTimeMillis() + lockMillis
            failedAttempts = 0
        }
    }

    fun login(userId: String): String {
        failedAttempts = 0
        lockedUntil = 0L
        boundDeviceUuid = deviceUuid()
        currentSessionId = newSessionId()
        lastActivityAt = System.currentTimeMillis()
        store.putString(SecureStore.KEY_USER_ID, userId)
        return currentSessionId!!
    }

    // ── 자동로그인 토큰 ────────────────────────────────────────────
    fun enableAutoLogin(userId: String) {
        val binding = if (VulnConfig.isVulnerable(VulnConfig.Flag.NO_DEVICE_BINDING)) "" else deviceUuid()
        // 토큰 = userId:기기바인딩:난수 (평문/암호화는 SecureStore 토글이 결정)
        val nonce = ByteArray(16).also { secureRandom.nextBytes(it) }
        val token = userId + "|" + binding + "|" + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce)
        store.putString(SecureStore.KEY_AUTOLOGIN_TOKEN, token)
    }

    /** 저장된 자동로그인 토큰으로 세션 복구. 기기 바인딩이 맞아야 성공(안전 모드). */
    fun tryAutoLogin(): Boolean {
        val token = store.getString(SecureStore.KEY_AUTOLOGIN_TOKEN) ?: return false
        val parts = token.split("|")
        if (parts.size < 3) return false
        val (userId, binding, _) = parts
        if (!VulnConfig.isVulnerable(VulnConfig.Flag.NO_DEVICE_BINDING)) {
            if (binding != deviceUuid()) return false // 다른 기기 → 거부
        }
        login(userId)
        return true
    }

    // ── 세션 타임아웃 ──────────────────────────────────────────────
    fun touch() { lastActivityAt = System.currentTimeMillis() }

    fun isExpired(): Boolean {
        if (currentSessionId == null) return true
        if (VulnConfig.isVulnerable(VulnConfig.Flag.NO_SESSION_TIMEOUT)) return false
        return System.currentTimeMillis() - lastActivityAt > idleTimeoutMillis
    }

    fun logout() {
        // 안전: 세션·자동로그인 토큰 모두 무효화 (D-50 세션 종료)
        currentSessionId = null
        boundDeviceUuid = null
        lastActivityAt = 0L
        store.clear()
    }
}
