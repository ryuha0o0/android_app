package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaTabRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaSuccess
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(1) }
    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "회원정보 수정", onBack = onBack)
        MoaTabRow(listOf("기본정보", "비밀번호 변경"), tab, onSelect = { tab = it })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            if (tab == 0) BasicInfoTab() else PasswordTab(onDone = onBack)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileCard(onChangePhone: () -> Unit) {
    val user = MoaRepository.user
    WhiteCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText(user.name, size = 17.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            MoaText("본인 확인됨", size = 11.sp, color = MoaTextTertiary)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText(user.phone, size = 13.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
            MoaText("번호 변경", size = 12.sp, weight = FontWeight.Bold, color = MoaBlue, modifier = Modifier.clickable(onClick = onChangePhone))
        }
    }
}

@Composable
private fun ColumnScope.BasicInfoTab() {
    val showMessage = LocalShowMessage.current
    val user = MoaRepository.user
    var phone by remember { mutableStateOf(user.phone) }
    var email by remember { mutableStateOf(user.email) }

    Spacer(Modifier.height(20.dp))
    ProfileCard(onChangePhone = {})
    Spacer(Modifier.height(26.dp))
    MoaText("기본정보를 확인해 주세요", size = 22.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    MoaText("연락처는 이체 알림과 본인 확인에 사용돼요.", size = 13.sp, color = MoaTextSecondary)
    Spacer(Modifier.height(22.dp))
    FieldLabel("휴대폰 번호")
    MoaTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '-' }.take(13) }, keyboardType = KeyboardType.Phone)
    Spacer(Modifier.height(18.dp))
    FieldLabel("이메일")
    MoaTextField(email, { email = it.trim() }, keyboardType = KeyboardType.Email)
    Spacer(Modifier.height(28.dp))
    val valid = phone.filter(Char::isDigit).length >= 10 && email.contains('@')
    PrimaryButton("저장하기", enabled = valid, onClick = {
        MoaRepository.user = user.copy(phone = phone, email = email)
        showMessage("기본정보를 저장했어요")
    })
}

private fun passwordRuleOk(pw: String): Boolean =
    pw.length in 8..20 && pw.any(Char::isLetter) && pw.any(Char::isDigit) && pw.any { !it.isLetterOrDigit() }

@Composable
private fun ColumnScope.PasswordTab(onDone: () -> Unit) {
    val showMessage = LocalShowMessage.current
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNext by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Spacer(Modifier.height(20.dp))
    ProfileCard(onChangePhone = { showMessage("기본정보 탭에서 번호를 바꿀 수 있어요") })
    Spacer(Modifier.height(26.dp))
    MoaText("비밀번호를 변경할게요", size = 22.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    MoaText(
        "회원정보 보호를 위한 비밀번호예요.\n금융인증서와 계좌 비밀번호는 바뀌지 않아요.",
        size = 13.sp, color = MoaTextSecondary, lineHeight = 20.sp,
    )

    Spacer(Modifier.height(22.dp))
    FieldLabel("현재 비밀번호")
    PasswordField(current, { current = it }, showCurrent) { showCurrent = !showCurrent }
    Spacer(Modifier.height(18.dp))
    FieldLabel("새 비밀번호")
    PasswordField(next, { next = it }, showNext) { showNext = !showNext }
    Spacer(Modifier.height(18.dp))
    FieldLabel("새 비밀번호 확인")
    PasswordField(confirm, { confirm = it }, showConfirm) { showConfirm = !showConfirm }

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
    MoaText(
        "영문·숫자·특수문자를 포함한 8~20자로 설정해 주세요.\n생일이나 연속된 숫자는 피해 주세요.",
        size = 11.sp,
        color = if (next.isNotEmpty() && !ruleOk) MoaDanger else MoaTextTertiary,
        lineHeight = 17.sp,
    )
    Spacer(Modifier.height(18.dp))
    PrimaryButton(
        "비밀번호 변경하기",
        enabled = current.isNotEmpty() && ruleOk && matches && current != next,
        onClick = {
            showMessage("비밀번호를 변경했어요")
            onDone()
        },
    )
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, visible: Boolean, onToggle: () -> Unit) {
    MoaTextField(
        value,
        { onChange(it.take(20)) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = visible,
    ) {
        MoaText(
            if (visible) "숨기기" else "보기",
            size = 12.sp,
            weight = FontWeight.SemiBold,
            color = MoaBlue,
            modifier = Modifier.clickable(onClick = onToggle),
        )
    }
}
