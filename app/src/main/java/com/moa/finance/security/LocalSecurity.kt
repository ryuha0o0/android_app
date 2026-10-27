package com.moa.finance.security

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 세션 매니저를 화면 어디서나 쓰도록 제공하는 CompositionLocal.
 * MainActivity 에서 실제 인스턴스를 주입합니다.
 */
val LocalSession = staticCompositionLocalOf<SessionManager> {
    error("SessionManager not provided")
}
