package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.security.E2ECrypto
import com.moa.finance.security.MockBackend
import com.moa.finance.security.VulnConfig
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.Notice
import com.moa.finance.ui.components.NoticeKind
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaSuccess
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

/**
 * 비밀번호 변경 1/2 — 본인확인 (현재 비밀번호).
 * 인증단계 우회 실습(D-05,37,44): 안전 모드는 이 단계를 통과해야 2페이지로 갈 수 있고,
 * 취약 모드(AUTH_STEP_BYPASS)에서는 딥링크(labapp://password-change)로 2페이지에 바로 진입 가능.
 */
@Composable
fun PasswordVerifyScreen(onVerified: () -> Unit, onBack: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "비밀번호 변경 · 본인확인", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(24.dp))
            Step(1)
            Spacer(Modifier.height(20.dp))
            MoaText("본인확인이 필요해요", size = 22.sp, weight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            MoaText("현재 회원 비밀번호를 입력해 주세요.", size = 13.sp, color = MoaTextSecondary)
            Spacer(Modifier.height(22.dp))
            FieldLabel("현재 비밀번호")
            PasswordField(current, { current = it }, show) { show = !show }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("다음 · 본인확인", enabled = current.isNotEmpty(), onClick = onVerified)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * 비밀번호 변경 2/2 — 새 비밀번호 설정.
 * @param verified 1페이지(본인확인)를 통과했는지. 안전 모드에서 false면 접근 차단.
 */
@Composable
fun PasswordChangeScreen(verified: Boolean, onDone: () -> Unit, onBack: () -> Unit) {
    val showMessage = LocalShowMessage.current
    val bypass = VulnConfig.isVulnerable(VulnConfig.Flag.AUTH_STEP_BYPASS)
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showNext by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // 안전 모드 + 본인확인 미통과 + (딥링크 등) 직접 진입 → 차단
    val blocked = !bypass && !verified

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "비밀번호 변경", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(24.dp))
            Step(2)
            Spacer(Modifier.height(20.dp))

            if (blocked) {
                Notice("본인확인을 먼저 해주세요. 이 화면에 바로 접근할 수 없어요.", kind = NoticeKind.Warning)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("본인확인 하러 가기", onClick = onBack)
                Spacer(Modifier.height(24.dp))
                return@Column
            }

            if (bypass && !verified) {
                Notice("본인확인 없이 진입했어요 (인증단계 우회). 보안 실습 설정에서 끄면 차단됩니다.", kind = NoticeKind.Error)
                Spacer(Modifier.height(16.dp))
            }

            MoaText("새 비밀번호를 설정할게요", size = 22.sp, weight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            MoaText(
                "영문·숫자·특수문자를 포함한 8~20자로 설정해 주세요.",
                size = 13.sp, color = MoaTextSecondary, lineHeight = 20.sp,
            )
            Spacer(Modifier.height(22.dp))
            FieldLabel("새 비밀번호")
            PasswordField(next, { next = it.take(20) }, showNext) { showNext = !showNext }
            Spacer(Modifier.height(18.dp))
            FieldLabel("새 비밀번호 확인")
            PasswordField(confirm, { confirm = it.take(20) }, showConfirm) { showConfirm = !showConfirm }

            val matches = next.isNotEmpty() && next == confirm
            val ruleOk = passwordRuleOk(next)
            Spacer(Modifier.height(12.dp))
            if (confirm.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (matches) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = if (matches) MoaSuccess else MoaDanger,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    MoaText(
                        if (matches) "비밀번호가 일치해요" else "비밀번호가 일치하지 않아요",
                        size = 12.sp, weight = FontWeight.SemiBold,
                        color = if (matches) MoaSuccess else MoaDanger,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            PrimaryButton(
                "비밀번호 변경하기",
                enabled = ruleOk && matches,
                onClick = {
                    // E2E: 새 비밀번호를 평문이 아닌 암호화 봉투로 전송 (서버 앱단에서만 복호화)
                    val env = E2ECrypto.encryptField(next, MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID)
                    MockBackend.e2eDecrypt(env)
                    showMessage(
                        when {
                            bypass && !verified -> "본인확인 없이 비밀번호가 변경됐어요 (우회)"
                            !env.encrypted -> "비밀번호를 변경했어요 (※E2E 미적용: 평문 전송)"
                            else -> "비밀번호를 변경했어요"
                        }
                    )
                    onDone()
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Step(current: Int) {
    Row {
        listOf(1 to "본인확인", 2 to "새 비밀번호").forEach { (n, label) ->
            Column(Modifier.weight(1f).padding(end = 6.dp)) {
                androidx.compose.material3.HorizontalDivider(
                    thickness = 3.dp,
                    color = if (n <= current) MoaBlue else com.moa.finance.ui.theme.MoaDivider,
                )
                Spacer(Modifier.height(8.dp))
                MoaText(
                    "$n. $label", size = 12.sp, weight = FontWeight.SemiBold,
                    color = if (n == current) MoaTextSecondary else MoaTextTertiary,
                )
            }
        }
    }
}

internal fun passwordRuleOk(pw: String): Boolean =
    pw.length in 8..20 && pw.any(Char::isLetter) && pw.any(Char::isDigit) && pw.any { !it.isLetterOrDigit() }

@Composable
internal fun PasswordField(value: String, onChange: (String) -> Unit, visible: Boolean, onToggle: () -> Unit) {
    MoaTextField(
        value,
        { onChange(it.take(20)) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = visible,
    ) {
        MoaText(
            if (visible) "숨기기" else "보기",
            size = 12.sp, weight = FontWeight.SemiBold, color = MoaBlue,
            modifier = Modifier.clickable(onClick = onToggle),
        )
    }
}
