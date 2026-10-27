package com.moa.finance.security

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf

/**
 * ────────────────────────────────────────────────────────────────────────
 *  보안 실습(Security Lab) 토글
 * ────────────────────────────────────────────────────────────────────────
 * 이 앱은 금융보안원 전자금융기반시설 평가기준 등 점검항목을 직접 진단해 보는
 * 교육용 앱입니다. 각 플래그를 켜면 해당 "취약한 구현"이, 끄면 "안전한 구현"이
 * 동작합니다. 학습자는 같은 기능을 두 모드로 비교하며 취약점을 진단합니다.
 *
 * 실제 운영 앱이 아니며, 모든 데이터/통신은 기기 내부(로컬)에서만 처리됩니다.
 * 운영 배포 시에는 이 파일과 vulnerable 분기를 통째로 제거해야 합니다.
 */
object VulnConfig {

    /** 각 시나리오 식별자. 문서(docs/security/SECURITY-LAB.md)의 번호와 매핑됩니다. */
    enum class Flag(val title: String, val note: String) {
        PLAINTEXT_AUTOLOGIN("자동로그인 평문 저장", "토큰/자격증명을 SharedPreferences 평문 저장 (D-26,27 / A-14)"),
        PREDICTABLE_UUID("유추 가능한 기기 UUID", "순차/시간 기반 UUID로 세션·기기 바인딩 (A-14 / B-10)"),
        NO_DEVICE_BINDING("기기 바인딩 미검증", "다른 기기의 토큰 재사용 허용 (C-07,08)"),
        WEAK_SESSION_ID("예측 가능한 세션 ID", "순차 증가 세션 ID (D-22,30 / B-10)"),
        NO_LOGIN_LOCKOUT("로그인 시도 제한 없음", "인증 오류 횟수 제한 미적용 (D-49 / B-12)"),
        NO_SESSION_TIMEOUT("세션 타임아웃 없음", "세션 만료 미적용 (D-30,50)"),
        SQLI_TRANSACTION("거래내역 SQL 인젝션", "날짜 조회를 문자열 결합으로 질의 (D-19 / B-02 / C-01)"),
        PARAM_TAMPER_TRANSFER("이체 파라미터 변조", "출금계좌/금액 소유주·한도 재검증 생략 (D-04 / B-08,17)"),
        BROKEN_CARD_AUTHZ("카드 인가 미흡(IDOR)", "타인 카드 ID 접근 허용 (D-04,21 / B-08,17)"),
        AUTH_STEP_BYPASS("인증단계 우회", "본인확인 없이 비밀번호 변경 화면 강제 진입 (D-05,37,44)"),
        WEBVIEW_XSS("WebView XSS", "입력을 그대로 HTML에 삽입 + JS 허용 (D-58 / C-03)"),
        NO_CSRF_TOKEN("CSRF 토큰 없음", "문의/상태변경 요청에 CSRF 토큰 미적용 (D-45 / B-11)"),
        PATH_TRAVERSAL("경로추적(Path Traversal)", "다운로드 파일명 검증 없이 경로 결합 (D-28 / B-14 / C-02)"),
        UNSAFE_FILE_UPLOAD("파일 업로드 검증 없음", "확장자/타입 검증 없이 저장 (D-20 / B-05,13)"),
        ALLOW_SCREEN_CAPTURE("화면 캡처 허용", "FLAG_SECURE 미적용, 백그라운드 노출 (D-38,60)"),
        SKIP_INTEGRITY_CHECK("무결성 점검 생략", "루팅/디버깅/에뮬레이터 탐지 무시 (D-07,14~16 / A-09,20)"),
        ;
    }

    private const val PREFS = "security_lab_flags"
    private val state = mutableStateMapOf<Flag, Boolean>()

    /** 실습 기본값: 모든 취약 시나리오 ON (진단 대상). */
    private val defaults: Map<Flag, Boolean> = Flag.entries.associateWith { true }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        Flag.entries.forEach { flag ->
            state[flag] = prefs.getBoolean(flag.name, defaults[flag] ?: true)
        }
    }

    fun isVulnerable(flag: Flag): Boolean = state[flag] ?: (defaults[flag] ?: true)

    fun set(context: Context, flag: Flag, vulnerable: Boolean) {
        state[flag] = vulnerable
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(flag.name, vulnerable).apply()
    }

    fun snapshot(): Map<Flag, Boolean> = Flag.entries.associateWith { isVulnerable(it) }
}
