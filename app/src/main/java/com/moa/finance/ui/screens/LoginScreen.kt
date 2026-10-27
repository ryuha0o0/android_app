package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.SecondaryButton
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaBlueSoft
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onBack: () -> Unit,
) {
    val showMessage = LocalShowMessage.current
    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "로그인", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(28.dp))
            MoaText("Labbit", size = 28.sp, weight = FontWeight.ExtraBold, color = MoaBlue)
            Spacer(Modifier.height(16.dp))
            MoaText("내 모든 금융을,\n가볍게 모아.", size = 26.sp, weight = FontWeight.ExtraBold, lineHeight = 36.sp)
            Spacer(Modifier.height(12.dp))
            MoaText("하나의 인증으로 시작하는 편안한 금융", size = 13.sp, color = MoaTextSecondary)

            Spacer(Modifier.height(36.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(92.dp).clip(RoundedCornerShape(26.dp)).background(MoaBlueLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.GppGood, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(18.dp))
                Tag("기본 로그인 · 금융인증서", container = MoaBlueSoft)
                Spacer(Modifier.height(14.dp))
                MoaText(
                    "안전한 금융인증서로\n내 계좌와 자산을 한 번에 확인하세요.",
                    size = 13.sp,
                    color = MoaTextSecondary,
                    align = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
            PrimaryButton("금융인증서로 로그인", onClick = onLogin)
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                "등록한 생체인증으로 로그인",
                onClick = onLogin,
                container = MoaSurface,
                content = MoaTextPrimary,
                leading = { Icon(Icons.Outlined.Face, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(20.dp)) },
            )

            Spacer(Modifier.height(20.dp))
            WhiteCard {
                MoaText("처음에는 금융인증서로 인증해 주세요", size = 13.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                MoaText(
                    "최초 인증 후 기기 생체인증을 등록하면 다음부터 인증서를 간편하게 사용할 수 있어요. 생체정보는 기기 안에서만 확인돼요.",
                    size = 12.sp,
                    color = MoaTextSecondary,
                    lineHeight = 18.sp,
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                MoaText(
                    "회원가입", size = 13.sp, weight = FontWeight.Bold, color = MoaBlue,
                    modifier = Modifier.clickable(onClick = onSignUp).padding(8.dp),
                )
                MoaText(
                    "인증서 관리", size = 13.sp, color = MoaTextTertiary,
                    modifier = Modifier.clickable { showMessage("인증서 관리는 로그인 후 이용할 수 있어요") }.padding(8.dp),
                )
                MoaText(
                    "도움말", size = 13.sp, color = MoaTextTertiary,
                    modifier = Modifier.clickable { showMessage("고객센터 1588-0000") }.padding(8.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
