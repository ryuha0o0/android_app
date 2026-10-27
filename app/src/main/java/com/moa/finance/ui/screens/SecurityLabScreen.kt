package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.security.VulnConfig
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.SecondaryButton
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSuccess
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary

/**
 * 보안 실습(Security Lab) 설정 — 각 취약 시나리오를 켜고 끄며 진단합니다.
 * 스위치 ON = 취약(vulnerable) 구현, OFF = 안전(secure) 구현.
 */
@Composable
fun SecurityLabScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val showMessage = LocalShowMessage.current
    val flags = VulnConfig.snapshot()

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "보안 실습 설정", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(16.dp))
            MoaText(
                "스위치를 켜면 취약한 구현이, 끄면 안전한 구현이 동작해요. 같은 기능을 두 모드로 비교하며 진단하세요.",
                size = 12.sp, color = MoaTextSecondary, lineHeight = 18.sp,
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SecondaryButton(
                    "전체 안전 모드",
                    onClick = {
                        VulnConfig.Flag.entries.forEach { VulnConfig.set(context, it, false) }
                        showMessage("모든 시나리오를 안전 모드로 바꿨어요")
                    },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                SecondaryButton(
                    "전체 취약 모드",
                    onClick = {
                        VulnConfig.Flag.entries.forEach { VulnConfig.set(context, it, true) }
                        showMessage("모든 시나리오를 취약 모드로 바꿨어요")
                    },
                    container = MoaSurface,
                    content = MoaDanger,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
            WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                VulnConfig.Flag.entries.forEachIndexed { i, flag ->
                    if (i > 0) HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MoaText(flag.title, size = 14.sp, weight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                val vulnerable = flags[flag] == true
                                MoaText(
                                    if (vulnerable) "취약" else "안전",
                                    size = 11.sp, weight = FontWeight.Bold,
                                    color = if (vulnerable) MoaDanger else MoaSuccess,
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            MoaText(flag.note, size = 11.sp, color = MoaTextSecondary, lineHeight = 16.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Switch(
                            checked = flags[flag] == true,
                            onCheckedChange = { VulnConfig.set(context, flag, it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MoaDanger,
                                uncheckedTrackColor = MoaDivider,
                                uncheckedBorderColor = MoaDivider,
                                uncheckedThumbColor = MoaSurface,
                            ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

