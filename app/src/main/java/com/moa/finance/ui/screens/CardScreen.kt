package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.InfoRow
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MenuDivider
import com.moa.finance.ui.components.MenuGroup
import com.moa.finance.ui.components.MenuRow
import com.moa.finance.ui.components.MoaTabRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.components.formatWon
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueSoft
import com.moa.finance.ui.theme.MoaCardNavy
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun CardScreen(initialTab: Int, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "카드", onBack = onBack)
        MoaTabRow(listOf("카드 신청", "내 카드 관리"), tab, onSelect = { tab = it })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            if (tab == 0) CardApplyTab() else MyCardTab()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CheckCardVisual(modifier: Modifier = Modifier) {
    Column(
        modifier
            .width(250.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MoaCardNavy)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("Labbit", size = 15.sp, weight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Contactless, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(width = 30.dp, height = 22.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFDCE2EC)))
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("데일리 체크", size = 12.sp, weight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
            MoaText("국내전용", size = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ColumnScope.CardApplyTab() {
    var applied by remember { mutableStateOf(false) }

    Spacer(Modifier.height(22.dp))
    Tag("일상에 꼭 맞는 체크카드", container = MoaBlueSoft)
    Spacer(Modifier.height(14.dp))
    MoaText("매일 쓰는 만큼,\n차곡차곡 돌아오는 혜택", size = 23.sp, weight = FontWeight.ExtraBold, lineHeight = 33.sp)
    Spacer(Modifier.height(26.dp))
    CheckCardVisual(Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(18.dp))
    MoaText("Labbit 데일리 체크", size = 17.sp, weight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(6.dp))
    MoaText(
        "연회비 없음 · 국내전용 · 후불교통 선택",
        size = 12.sp, color = MoaTextTertiary,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(22.dp))
    WhiteCard(padding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)) {
        Benefit(Icons.Outlined.LocalCafe, "커피 · 편의점 5% 캐시백", "자주 가는 곳에서 쏠쏠하게")
        Benefit(Icons.Outlined.DirectionsBus, "대중교통 3% 캐시백", "출퇴근길에도 챙기는 혜택")
        Benefit(Icons.Outlined.ShoppingBag, "온라인 쇼핑 1% 캐시백", "일상 속 쇼핑을 더 가볍게")
    }
    Spacer(Modifier.height(14.dp))
    MoaText(
        "전월 이용금액 20만원 이상 · 월 통합 최대 1만원\n혜택별 대상 가맹점과 제공 조건을 확인해 주세요.",
        size = 11.sp, color = MoaTextTertiary, lineHeight = 17.sp,
    )
    Spacer(Modifier.height(16.dp))
    PrimaryButton(if (applied) "신청 완료" else "카드 신청하기", onClick = { applied = true }, enabled = !applied)
    Spacer(Modifier.height(10.dp))
    MoaText(
        "상품설명서 및 약관 확인 후 신청할 수 있어요.",
        size = 11.sp, color = MoaTextTertiary,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

@Composable
private fun Benefit(icon: ImageVector, title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            MoaText(title, size = 14.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            MoaText(desc, size = 12.sp, color = MoaTextTertiary)
        }
    }
}

@Composable
private fun ColumnScope.MyCardTab() {
    val repo = MoaRepository
    val showMessage = LocalShowMessage.current
    var history by remember { mutableStateOf(false) }
    var lost by remember { mutableStateOf(false) }

    Spacer(Modifier.height(22.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MoaText("Labbit 데일리 체크", size = 20.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        if (repo.cardPaused) Tag("일시정지", container = Color(0xFFFDECEC), content = Color(0xFFE5484D)) else Tag("정상 이용")
    }
    Spacer(Modifier.height(18.dp))
    CheckCardVisual(Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(10.dp))
    MoaText(
        "•••• •••• •••• 4821 · ${repo.user.name}",
        size = 12.sp, color = MoaTextTertiary,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(20.dp))

    WhiteCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("10월 이용금액", size = 14.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            MoaText("10.24 기준", size = 11.sp, color = MoaTextTertiary)
        }
        Spacer(Modifier.height(12.dp))
        MoaText(formatWon(324_500), size = 26.sp, weight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("이번 달 예상 캐시백", size = 12.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
            MoaText(formatWon(4_800), size = 14.sp, weight = FontWeight.Bold, color = MoaBlue)
        }
    }
    Spacer(Modifier.height(14.dp))

    MenuGroup {
        MenuRow("카드 이용내역", icon = Icons.AutoMirrored.Outlined.ReceiptLong, onClick = { history = true })
        MenuDivider()
        MenuRow(
            "결제계좌 관리",
            icon = Icons.Outlined.AccountBalanceWallet,
            subtitle = "한빛은행 · 생활비 통장 ••7890",
            onClick = { showMessage("결제계좌 변경은 준비 중이에요") },
        )
    }
    Spacer(Modifier.height(14.dp))
    MenuGroup {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                MoaText("카드 일시정지", size = 15.sp, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                MoaText("필요할 때 잠시 사용을 멈춰요", size = 12.sp, color = MoaTextTertiary)
            }
            Switch(
                checked = repo.cardPaused,
                onCheckedChange = {
                    repo.cardPaused = it
                    showMessage(if (it) "카드를 일시정지했어요" else "카드 사용을 다시 시작했어요")
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MoaBlue,
                    uncheckedTrackColor = MoaDivider,
                    uncheckedBorderColor = MoaDivider,
                    uncheckedThumbColor = MoaSurface,
                ),
            )
        }
        MenuDivider()
        MenuRow("분실신고 / 재발급", icon = Icons.Outlined.GppMaybe, onClick = { lost = true })
    }

    if (history) {
        AlertDialog(
            onDismissRequest = { history = false },
            containerColor = MoaSurface,
            title = { Text("10월 카드 이용내역", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    InfoRow("10.23 오늘커피", formatWon(4_500))
                    InfoRow("10.22 모아마트", formatWon(32_700))
                    InfoRow("10.18 모아버스", formatWon(1_500))
                    InfoRow("10.15 모아온라인몰", formatWon(86_000))
                    HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(vertical = 6.dp))
                    InfoRow("합계", formatWon(324_500), valueWeight = FontWeight.Bold)
                }
            },
            confirmButton = { TextButton(onClick = { history = false }) { Text("닫기", color = MoaBlue) } },
        )
    }
    if (lost) {
        AlertDialog(
            onDismissRequest = { lost = false },
            containerColor = MoaSurface,
            title = { Text("분실신고 할까요?", fontWeight = FontWeight.Bold) },
            text = { Text("신고 즉시 카드 사용이 정지되고, 재발급 카드는 영업일 기준 5일 안에 받아볼 수 있어요.") },
            confirmButton = {
                TextButton(onClick = {
                    lost = false
                    repo.cardPaused = true
                    showMessage("분실신고가 접수됐어요")
                }) { Text("신고하기", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { lost = false }) { Text("취소", color = MoaTextSecondary) } },
        )
    }
}
