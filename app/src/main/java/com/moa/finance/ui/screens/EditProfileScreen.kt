package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
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
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MenuGroup
import com.moa.finance.ui.components.MenuRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

/** 회원정보 수정 — 기본정보(연락처·이메일) + 비밀번호 변경 진입. (비밀번호 변경은 2페이지로 분리) */
@Composable
fun EditProfileScreen(onBack: () -> Unit, onChangePassword: () -> Unit) {
    val showMessage = LocalShowMessage.current
    val user = MoaRepository.user
    var phone by remember { mutableStateOf(user.phone) }
    var email by remember { mutableStateOf(user.email) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "회원정보 수정", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(20.dp))
            WhiteCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText(user.name, size = 17.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    MoaText("본인 확인됨", size = 11.sp, color = MoaTextTertiary)
                }
                Spacer(Modifier.height(10.dp))
                MoaText(user.phone, size = 13.sp, color = MoaTextSecondary)
            }

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
            Spacer(Modifier.height(24.dp))
            val valid = phone.filter(Char::isDigit).length >= 10 && email.contains('@')
            PrimaryButton("저장하기", enabled = valid, onClick = {
                MoaRepository.user = user.copy(phone = phone, email = email)
                showMessage("기본정보를 저장했어요")
            })

            Spacer(Modifier.height(26.dp))
            MoaText("보안", size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            MenuGroup {
                MenuRow(
                    "비밀번호 변경",
                    icon = Icons.Outlined.Lock,
                    subtitle = "본인확인 후 새 비밀번호로 변경",
                    iconTint = MoaBlue,
                    onClick = onChangePassword,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
