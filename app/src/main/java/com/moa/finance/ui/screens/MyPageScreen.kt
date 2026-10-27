package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MenuDivider
import com.moa.finance.ui.components.MenuGroup
import com.moa.finance.ui.components.MenuRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun MyPageScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onManageAccount: () -> Unit,
    onSecurityLab: () -> Unit,
    onLogout: () -> Unit,
) {
    val showMessage = LocalShowMessage.current
    val user = MoaRepository.user
    var confirmLogout by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "마이페이지", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(MoaBlueLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    MoaText("${user.name}님", size = 22.sp, weight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    MoaText("Labbit과 함께한 지 ${user.joinedDays}일", size = 12.sp, color = MoaTextTertiary)
                }
                IconButton(onClick = { showMessage("앱 설정은 준비 중이에요") }) {
                    Icon(Icons.Outlined.Settings, contentDescription = "설정", tint = MoaTextSecondary)
                }
            }

            Spacer(Modifier.height(24.dp))
            MenuGroup {
                MenuRow("회원정보 수정", Icons.Outlined.ManageAccounts, "연락처 · 비밀번호 변경", iconTint = MoaBlue, onClick = onEditProfile)
                MenuDivider()
                MenuRow("계좌 관리", Icons.Outlined.AccountBalance, "계좌 비밀번호 변경 · 한도제한 해제", iconTint = MoaBlue, onClick = onManageAccount)
            }

            Spacer(Modifier.height(24.dp))
            MoaText("인증과 보안", size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            MenuGroup {
                MenuRow("금융인증서 관리", Icons.Outlined.VerifiedUser, "인증서 연결 · 갱신") { showMessage("금융인증서가 정상 연결되어 있어요") }
                MenuDivider()
                MenuRow("기기 생체인증", Icons.Outlined.Face, "등록됨 · 다음 로그인부터 간편하게") { showMessage("생체인증이 등록되어 있어요") }
            }

            Spacer(Modifier.height(14.dp))
            MenuGroup {
                MenuRow("알림 설정", Icons.Outlined.Notifications) { showMessage("알림 설정은 준비 중이에요") }
                MenuDivider()
                MenuRow("고객센터", Icons.Outlined.HeadsetMic) { showMessage("고객센터 1588-0000") }
                MenuDivider()
                MenuRow("약관 및 개인정보 처리방침", Icons.Outlined.Description) { showMessage("약관 전문은 준비 중이에요") }
            }

            Spacer(Modifier.height(24.dp))
            MoaText("개발 · 실습", size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            MenuGroup {
                MenuRow("보안 실습 설정", Icons.Outlined.Science, "취약/안전 모드 전환 · 취약점 진단", iconTint = MoaDanger, onClick = onSecurityLab)
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText(
                    "로그아웃",
                    size = 12.sp,
                    color = MoaTextSecondary,
                    modifier = Modifier.clickable { confirmLogout = true }.padding(vertical = 6.dp),
                )
                Spacer(Modifier.weight(1f))
                MoaText("앱 버전 1.0.0 · 최신 버전", size = 11.sp, color = MoaTextTertiary)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = MoaSurface,
            title = { Text("로그아웃 할까요?", fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("로그아웃", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("취소", color = MoaTextSecondary) } },
        )
    }
}
