package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.security.CertificateManager
import com.moa.finance.security.E2ECrypto
import com.moa.finance.security.MockBackend
import com.moa.finance.security.PinManager
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.SecurePinPad
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaTextSecondary

/** 간편비밀번호 등록: 6자리 입력 → 확인 재입력 → PBKDF2 저장 + E2E 암호화 전송(모의). */
@Composable
fun PinRegisterScreen(userId: String, onDone: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val showMessage = LocalShowMessage.current
    val pinManager = remember { PinManager(context) }
    var step by remember { mutableStateOf(0) } // 0: 입력, 1: 확인
    var first by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit(full: String) {
        if (step == 0) {
            first = full; pin = ""; step = 1; error = null
        } else {
            if (full != first) {
                error = "비밀번호가 일치하지 않아요"; pin = ""; first = ""; step = 0
            } else {
                pinManager.register(full)
                // E2E: 서버 등록 시 PIN을 평문이 아닌 암호화 봉투로 전송 (서버 앱단에서만 복호화)
                val env = E2ECrypto.encryptField(full, MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID)
                val serverSaw = MockBackend.e2eDecrypt(env) // 왕복 검증(데모)
                showMessage(if (env.encrypted) "간편비밀번호를 암호화해 등록했어요 (E2E)" else "간편비밀번호를 등록했어요(※E2E 미적용: 평문 전송)")
                onDone()
            }
        }
    }

    LaunchedEffect(pin) { if (pin.length == 6) submit(pin) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "간편비밀번호 등록", onBack = onBack)
        Column(
            Modifier.fillMaxSize().padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            MoaText(
                if (step == 0) "사용할 6자리를 입력해 주세요" else "한 번 더 입력해 주세요",
                size = 20.sp, weight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(10.dp))
            MoaText(
                "다음부터 이 6자리로 간편하게 로그인해요.\n생일·연속 숫자는 피해 주세요.",
                size = 13.sp, color = MoaTextSecondary, align = TextAlign.Center, lineHeight = 20.sp,
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                MoaText(it, size = 13.sp, weight = FontWeight.SemiBold, color = MoaDanger)
            }
            Spacer(Modifier.height(40.dp))
            SecurePinPad(value = pin, onChange = { pin = it })
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 간편비밀번호 로그인: 6자리 검증 → 성공 시 인증서(개인키) 서명으로 세션 생성. */
@Composable
fun PinLoginScreen(userId: String, onSuccess: () -> Unit, onUseCertificate: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val showMessage = LocalShowMessage.current
    val pinManager = remember { PinManager(context) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit(full: String) {
        when {
            pinManager.isLockedOut() -> {
                error = "시도가 많아 잠겼어요. ${pinManager.lockRemainingSeconds()}초 후 다시 시도해 주세요"; pin = ""
            }
            pinManager.verify(full) -> {
                // E2E: 간편비번을 암호화 봉투로 서버에 전송(검증은 로컬, 데모용 왕복)
                val env = E2ECrypto.encryptField(full, MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID)
                MockBackend.e2eDecrypt(env)
                if (CertificateManager.login(userId)) {
                    onSuccess()
                } else {
                    error = "인증서 확인에 실패했어요. 금융인증서로 로그인해 주세요"; pin = ""
                }
            }
            else -> {
                error = if (pinManager.isLockedOut())
                    "5회 틀려 잠겼어요. 잠시 후 다시 시도해 주세요"
                else "비밀번호가 올바르지 않아요. 다시 입력해 주세요"
                pin = ""
            }
        }
    }

    LaunchedEffect(pin) { if (pin.length == 6) submit(pin) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "간편비밀번호 로그인", onBack = onBack)
        Column(
            Modifier.fillMaxSize().padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            MoaText("간편비밀번호 6자리", size = 20.sp, weight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            MoaText("등록한 6자리를 입력해 주세요.", size = 13.sp, color = MoaTextSecondary)
            error?.let {
                Spacer(Modifier.height(12.dp))
                MoaText(it, size = 13.sp, weight = FontWeight.SemiBold, color = MoaDanger)
            }
            Spacer(Modifier.height(40.dp))
            SecurePinPad(value = pin, onChange = { pin = it })
            Spacer(Modifier.height(20.dp))
            MoaText(
                "금융인증서로 로그인",
                size = 13.sp, weight = FontWeight.SemiBold,
                color = com.moa.finance.ui.theme.MoaBlue,
                modifier = Modifier.clickable(onClick = onUseCertificate).padding(8.dp),
            )
        }
    }
}
