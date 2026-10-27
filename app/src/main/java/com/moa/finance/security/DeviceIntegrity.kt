package com.moa.finance.security

import android.content.Context
import android.os.Build
import android.os.Debug
import java.io.File

/**
 * 기기 무결성·위험 신호 평가 (앱 실행 단계, API 18·19).
 *
 * 점검항목: D-07(OS 변조 탐지), D-14~16(무결성/디버깅/난독화), A-09, A-20 / E-F09,F20,S26
 *
 *  - VULN (SKIP_INTEGRITY_CHECK ON) : evaluate() 가 항상 안전으로 보고 → 탐지 로직 우회 실습.
 *  - SECURE : 루팅/커스텀OS, 디버거 attach, Frida 등 후킹 프레임워크, 에뮬레이터를
 *             점검해 위험 신호를 모읍니다. (단순 지표 기반 — 실제 상용 탐지는 더 강력)
 *
 * 학습 포인트: 이런 탐지는 '방어선'일 뿐 우회 가능하므로, 서버 측 거래 검증과 반드시 병행.
 */
object DeviceIntegrity {

    data class Report(
        val rooted: Boolean,
        val debuggerAttached: Boolean,
        val hookingDetected: Boolean,
        val emulator: Boolean,
    ) {
        val risky: Boolean get() = rooted || debuggerAttached || hookingDetected || emulator
        val signals: List<String> get() = buildList {
            if (rooted) add("루팅/커스텀 OS 흔적")
            if (debuggerAttached) add("디버거 연결")
            if (hookingDetected) add("후킹 프레임워크(Frida 등)")
            if (emulator) add("에뮬레이터 환경")
        }
    }

    fun evaluate(context: Context): Report {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.SKIP_INTEGRITY_CHECK)) {
            return Report(rooted = false, debuggerAttached = false, hookingDetected = false, emulator = false)
        }
        return Report(
            rooted = isRooted(),
            debuggerAttached = Debug.isDebuggerConnected() || Debug.waitingForDebugger(),
            hookingDetected = isHookingPresent(),
            emulator = isEmulator(),
        )
    }

    private val suPaths = listOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/app/Superuser.apk",
        "/data/local/xbin/su", "/data/local/su", "/su/bin/su", "/system/bin/magisk",
    )

    private fun isRooted(): Boolean {
        if (suPaths.any { File(it).exists() }) return true
        // test-keys 로 서명된 커스텀 빌드
        if (Build.TAGS?.contains("test-keys") == true) return true
        return false
    }

    /** Frida/Xposed 등은 메모리 맵·열린 포트·주입 라이브러리 흔적을 남깁니다(단순 지표). */
    private fun isHookingPresent(): Boolean {
        val maps = runCatching { File("/proc/self/maps").readText() }.getOrNull().orEmpty()
        if (listOf("frida", "xposed", "substrate").any { maps.contains(it, ignoreCase = true) }) return true
        val suspiciousLibs = listOf("frida-agent", "frida-gadget")
        return suspiciousLibs.any { lib -> maps.contains(lib, ignoreCase = true) }
    }

    private fun isEmulator(): Boolean {
        val fp = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val product = Build.PRODUCT ?: ""
        return fp.startsWith("generic") || fp.contains("emulator", true) ||
            model.contains("Emulator", true) || model.contains("Android SDK built for", true) ||
            product.contains("sdk", true) || Build.HARDWARE.contains("goldfish", true) ||
            Build.HARDWARE.contains("ranchu", true)
    }
}
