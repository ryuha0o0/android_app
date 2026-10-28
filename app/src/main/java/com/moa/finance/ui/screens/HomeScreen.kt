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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.AccountSummary
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.components.formatWon
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun HomeScreen(
    onAccounts: () -> Unit,
    onTransfer: (accountId: String?) -> Unit,
    onWithdraw: () -> Unit,
    onCards: () -> Unit,
    onAccountDetail: (accountId: String) -> Unit,
) {
    val showMessage = LocalShowMessage.current
    val repo = MoaRepository
    var query by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("모든 금융기관") }
    var institutionMenu by remember { mutableStateOf(false) }

    val visibleAccounts = repo.accounts.filter { institution == "모든 금융기관" || it.bankName == institution }
        .filter { query.isBlank() || it.name.contains(query.trim()) || it.bankName.contains(query.trim()) }
    val primary = repo.account(repo.primaryAccountId)

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MoaSurface)
                .statusBarsPadding()
                .height(56.dp)
                .padding(start = ScreenPadding, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoaText("Labbit", size = 22.sp, weight = FontWeight.ExtraBold, color = MoaBlue, modifier = Modifier.weight(1f))
            IconButton(onClick = { showMessage("새로운 알림이 없어요") }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "알림", tint = MoaTextPrimary)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(16.dp))
            // Search
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MoaSurface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = MoaTextTertiary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) MoaText("계좌, 이체, 카드를 검색해 보세요", size = 13.sp, color = MoaTextTertiary)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = MoaTextPrimary),
                        cursorBrush = SolidColor(MoaBlue),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText("${repo.user.name}님, 안녕하세요", size = 20.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.LightMode, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Box {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MoaSurface)
                        .clickable { institutionMenu = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MoaText(institution, size = 12.sp, weight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(institutionMenu, onDismissRequest = { institutionMenu = false }, containerColor = MoaSurface) {
                    (listOf("모든 금융기관") + repo.accounts.map { it.bankName }.distinct()).forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { institution = it; institutionMenu = false })
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            // Total balance card
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MoaBlue)
                    .clickable(onClick = onAccounts)
                    .padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText("내 계좌 총 잔액", size = 13.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                    Icon(
                        if (repo.balanceHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "잔액 숨기기",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).clickable { repo.balanceHidden = !repo.balanceHidden },
                    )
                }
                Spacer(Modifier.height(14.dp))
                MoaText(
                    if (repo.balanceHidden) "잔액 숨김" else formatWon(repo.totalBalance),
                    size = 30.sp,
                    weight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText(
                        "${repo.accounts.size}개 계좌 · 오늘 ${repo.lastRefreshed} 기준",
                        size = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "새로고침",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).clickable {
                            repo.refresh()
                            showMessage("잔액을 새로 불러왔어요")
                        },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickAction(Icons.Outlined.AccountBalanceWallet, "계좌조회", onAccounts)
                QuickAction(Icons.Outlined.NorthEast, "이체하기") { onTransfer(null) }
                QuickAction(Icons.Outlined.LocalAtm, "출금하기", onWithdraw)
                QuickAction(Icons.Outlined.CreditCard, "카드관리", onCards)
            }

            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText("내 계좌", size = 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(Modifier.clickable(onClick = onAccounts), verticalAlignment = Alignment.CenterVertically) {
                    MoaText("전체보기", size = 12.sp, color = MoaTextTertiary)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MoaTextTertiary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(12.dp))

            val shown = if (query.isBlank() && institution == "모든 금융기관") listOf(primary) else visibleAccounts
            if (shown.isEmpty()) {
                WhiteCard {
                    MoaText("검색 결과가 없어요", size = 14.sp, color = MoaTextSecondary)
                }
            }
            shown.forEach { account ->
                WhiteCard(onClick = { onAccountDetail(account.id) }) {
                    AccountSummary(account.bankName, account.name, "${account.bankName} · ${account.maskedNumber}")
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MoaText(
                            if (repo.balanceHidden) "잔액 숨김" else formatWon(account.balance),
                            size = 22.sp,
                            weight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (account.transferable) Tag("이체", onClick = { onTransfer(account.id) })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.GppGood, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                MoaText("안심하고 이용하세요. 보안 상태가 안전해요.", size = 12.sp, color = MoaTextSecondary)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(MoaSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        MoaText(label, size = 12.sp, weight = FontWeight.SemiBold, color = MoaTextPrimary)
    }
}
