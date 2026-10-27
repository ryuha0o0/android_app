package com.moa.finance

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.moa.finance.security.DeviceIntegrity
import com.moa.finance.security.LocalSession
import com.moa.finance.security.SessionManager
import com.moa.finance.security.VulnConfig
import com.moa.finance.ui.navigation.MoaApp
import com.moa.finance.ui.theme.MoaTheme

class MainActivity : ComponentActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VulnConfig.load(this)
        session = SessionManager(this)

        applyScreenCapturePolicy()

        // 앱 실행 단계 무결성 평가 (API 18,19). 위험 신호가 있으면 안내만 하고 진행.
        val report = DeviceIntegrity.evaluate(this)
        if (report.risky) {
            Toast.makeText(this, "보안 위험 감지: ${report.signals.joinToString(", ")}", Toast.LENGTH_LONG).show()
        }

        // 딥링크: labapp://password-change → 비밀번호 변경 화면 강제 진입 (인증단계 우회 실습)
        val deepLinkHost = intent?.data?.takeIf { it.scheme == "labapp" }?.host

        enableEdgeToEdge()
        setContent {
            MoaTheme {
                CompositionLocalProvider(LocalSession provides session) {
                    MoaApp(deepLinkHost = deepLinkHost)
                }
            }
        }
    }

    /** 화면 캡처·백그라운드 보호 (D-38,60). ALLOW_SCREEN_CAPTURE OFF 면 FLAG_SECURE 적용. */
    private fun applyScreenCapturePolicy() {
        if (VulnConfig.isVulnerable(VulnConfig.Flag.ALLOW_SCREEN_CAPTURE)) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // 토글이 바뀌었을 수 있으니 재적용
        applyScreenCapturePolicy()
    }
}
