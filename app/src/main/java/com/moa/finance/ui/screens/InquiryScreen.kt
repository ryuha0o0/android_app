package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.data.Transaction
import com.moa.finance.data.TransferResult
import com.moa.finance.ui.components.AccountPickerCard
import com.moa.finance.ui.components.AccountSummary
import com.moa.finance.ui.components.InfoRow
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaTabRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.SecondaryButton
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.components.formatSignedWon
import com.moa.finance.ui.components.formatWon
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaBlueSoft
import com.moa.finance.ui.theme.MoaChip
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

private val inquiryTabs = listOf("전체계좌", "거래내역", "이체결과", "해지계좌")

@Composable
fun InquiryScreen(
    initialTab: Int,
    initialAccountId: String?,
    onBack: () -> Unit,
    onTransfer: (accountId: String?) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    var accountId by rememberSaveable { mutableStateOf(initialAccountId ?: MoaRepository.primaryAccountId) }

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "조회", onBack = onBack)
        MoaTabRow(inquiryTabs, tab, onSelect = { tab = it })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            when (tab) {
                0 -> AllAccountsTab(
                    onHistory = { accountId = it; tab = 1 },
                    onTransfer = onTransfer,
                )
                1 -> HistoryTab(accountId, onAccountChange = { accountId = it })
                2 -> TransferResultTab()
                else -> ClosedAccountsTab()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColumnScope.AllAccountsTab(onHistory: (String) -> Unit, onTransfer: (String?) -> Unit) {
    val repo = MoaRepository
    val showMessage = LocalShowMessage.current
    var detail by remember { mutableStateOf<String?>(null) }

    Spacer(Modifier.height(20.dp))
    MoaText("모든 계좌의 잔액", size = 13.sp, color = MoaTextSecondary)
    Spacer(Modifier.height(6.dp))
    MoaText(formatWon(repo.totalBalance), size = 28.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MoaText("연결된 계좌 ${repo.accounts.size}개", size = 12.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
        Row(
            Modifier.clickable { repo.refresh(); showMessage("잔액을 새로 불러왔어요") },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoaText("${repo.lastRefreshed} 새로고침", size = 12.sp, color = MoaTextTertiary)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = MoaTextTertiary, modifier = Modifier.size(14.dp))
        }
    }
    Spacer(Modifier.height(18.dp))

    repo.accounts.forEach { account ->
        WhiteCard {
            AccountSummary(account.bankInitial, account.name, "${account.bankName} · ${account.maskedNumber}")
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText(formatWon(account.balance), size = 22.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Tag(account.tag)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(Modifier.clickable { onHistory(account.id) }, verticalAlignment = Alignment.CenterVertically) {
                        MoaText("거래내역 보기", size = 12.sp, color = MoaTextTertiary)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MoaTextTertiary, modifier = Modifier.size(14.dp))
                    }
                }
                MoaText(
                    account.actionLabel,
                    size = 13.sp,
                    weight = FontWeight.Bold,
                    color = MoaBlue,
                    modifier = Modifier.clickable {
                        if (account.transferable) onTransfer(account.id) else detail = account.id
                    },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    SecondaryButton(
        "다른 금융기관 계좌 연결",
        onClick = { showMessage("오픈뱅킹 계좌 연결은 준비 중이에요") },
        container = MoaBlueSoft,
        leading = { Icon(Icons.Filled.Add, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(18.dp)) },
    )

    detail?.let { id ->
        val account = repo.account(id)
        AlertDialog(
            onDismissRequest = { detail = null },
            containerColor = MoaSurface,
            title = { Text(account.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    InfoRow("금융기관", account.bankName)
                    InfoRow("계좌번호", account.number)
                    InfoRow("현재 납입액", formatWon(account.balance))
                    InfoRow("월 납입액", formatWon(200_000))
                    InfoRow("만기일", "2027.06.30")
                }
            },
            confirmButton = { TextButton(onClick = { detail = null }) { Text("확인", color = MoaBlue) } },
        )
    }
}

private enum class TxFilter(val label: String) { All("전체"), In("입금"), Out("출금") }

@Composable
private fun ColumnScope.HistoryTab(accountId: String, onAccountChange: (String) -> Unit) {
    val repo = MoaRepository
    var filter by remember { mutableStateOf(TxFilter.All) }
    var newestFirst by remember { mutableStateOf(true) }
    val showMessage = LocalShowMessage.current
    val account = repo.account(accountId)

    Spacer(Modifier.height(20.dp))
    AccountPickerCard(account, repo.accounts, onSelect = { onAccountChange(it.id) }) {
        Spacer(Modifier.height(14.dp))
        MoaText(formatWon(account.balance), size = 24.sp, weight = FontWeight.ExtraBold)
    }

    Spacer(Modifier.height(22.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MoaText("2026.10.01 – 10.24", size = 14.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(
            Icons.Outlined.CalendarMonth,
            contentDescription = "기간 선택",
            tint = MoaTextSecondary,
            modifier = Modifier.size(22.dp).clickable { showMessage("조회 기간 변경은 준비 중이에요") },
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        TxFilter.entries.forEach { f ->
            val selected = f == filter
            Box(
                Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) MoaTextPrimary else MoaSurface)
                    .clickable { filter = f }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                MoaText(
                    f.label, size = 13.sp, weight = FontWeight.SemiBold,
                    color = if (selected) Color.White else MoaTextSecondary,
                )
            }
        }
    }

    val items = repo.transactionsOf(account.id)
        .filter {
            when (filter) {
                TxFilter.All -> true
                TxFilter.In -> it.isDeposit
                TxFilter.Out -> !it.isDeposit
            }
        }
        .let { if (newestFirst) it else it.reversed() }

    Spacer(Modifier.height(18.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MoaText("최근 거래 ${items.size}건", size = 12.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
        Row(Modifier.clickable { newestFirst = !newestFirst }, verticalAlignment = Alignment.CenterVertically) {
            MoaText(if (newestFirst) "최신순" else "과거순", size = 12.sp, color = MoaTextSecondary)
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = MoaTextSecondary, modifier = Modifier.size(14.dp))
        }
    }
    Spacer(Modifier.height(10.dp))

    WhiteCard {
        if (items.isEmpty()) {
            MoaText("조회 기간 내 거래내역이 없어요", size = 13.sp, color = MoaTextSecondary)
        }
        var lastDate: String? = null
        items.forEachIndexed { index, tx ->
            if (tx.dateLabel != lastDate) {
                if (index > 0) {
                    HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(vertical = 10.dp))
                }
                MoaText(tx.dateLabel, size = 12.sp, color = MoaTextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                lastDate = tx.dateLabel
            }
            TransactionRow(tx)
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (tx.isDeposit) MoaBlueLight else MoaChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (tx.isDeposit) Icons.Outlined.SouthWest else Icons.Outlined.NorthEast,
                contentDescription = if (tx.isDeposit) "입금" else "출금",
                tint = if (tx.isDeposit) MoaBlue else MoaTextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            MoaText(tx.title, size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            MoaText("${tx.time} · ${tx.method}", size = 12.sp, color = MoaTextTertiary)
        }
        Column(horizontalAlignment = Alignment.End) {
            MoaText(
                formatSignedWon(tx.amount),
                size = 15.sp,
                weight = FontWeight.Bold,
                color = if (tx.isDeposit) MoaBlue else MoaTextPrimary,
            )
            Spacer(Modifier.height(3.dp))
            MoaText("잔액 ${formatWon(tx.balanceAfter)}", size = 11.sp, color = MoaTextTertiary)
        }
    }
}

@Composable
private fun ColumnScope.TransferResultTab() {
    val repo = MoaRepository
    val showMessage = LocalShowMessage.current
    val results = repo.transferResults.sortedByDescending { it.id }
    var receipt by remember { mutableStateOf<TransferResult?>(null) }

    Spacer(Modifier.height(20.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MoaSurface)
            .clickable { showMessage("조회 기간 변경은 준비 중이에요") }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoaText("2026.10.01 – 10.24", size = 14.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MoaTextSecondary, modifier = Modifier.size(20.dp))
    }
    Spacer(Modifier.height(14.dp))

    val latest = results.firstOrNull()
    if (latest == null) {
        WhiteCard { MoaText("이체 내역이 없어요", size = 14.sp, color = MoaTextSecondary) }
        return
    }

    WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(22.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(MoaBlueLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            MoaText("${latest.recipientName}님께 이체 완료", size = 17.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            MoaText(formatWon(latest.amount), size = 28.sp, weight = FontWeight.ExtraBold)
        }
        HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(vertical = 18.dp))
        TransferInfo(latest)
        Spacer(Modifier.height(16.dp))
        SecondaryButton("이체확인증 보기", onClick = { receipt = latest })
    }

    if (results.size > 1) {
        Spacer(Modifier.height(24.dp))
        MoaText("이전 이체 결과", size = 15.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        results.drop(1).forEach { r ->
            WhiteCard(onClick = { receipt = r }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText(r.recipientName, size = 15.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Tag("완료")
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText("${r.dateTime.take(10)} · ${r.recipientBank}", size = 12.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
                    MoaText(formatWon(r.amount), size = 16.sp, weight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    receipt?.let { r ->
        AlertDialog(
            onDismissRequest = { receipt = null },
            containerColor = MoaSurface,
            title = { Text("이체확인증", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    InfoRow("받는 분", r.recipientName)
                    InfoRow("이체 금액", formatWon(r.amount))
                    TransferInfo(r)
                }
            },
            confirmButton = { TextButton(onClick = { receipt = null }) { Text("닫기", color = MoaBlue) } },
        )
    }
}

@Composable
private fun TransferInfo(r: TransferResult) {
    InfoRow("이체일시", r.dateTime)
    InfoRow("출금계좌", "${r.fromAccount.bankName} · ${r.fromAccount.shortNumber}")
    InfoRow("받는 계좌", "${r.recipientBank} · ••${r.recipientNumber.filter(Char::isDigit).takeLast(4)}")
    InfoRow("수수료", formatWon(r.fee))
    InfoRow("받는 분 통장 표시", r.memo)
}

@Composable
private fun ColumnScope.ClosedAccountsTab() {
    val showMessage = LocalShowMessage.current
    val periods = listOf("최근 1년 · 2025.10 – 2026.10", "최근 3년 · 2023.10 – 2026.10", "전체 기간")
    var period by remember { mutableStateOf(periods[0]) }
    var periodMenu by remember { mutableStateOf(false) }
    val closed = MoaRepository.closedAccounts

    Spacer(Modifier.height(26.dp))
    MoaText("해지한 계좌도 한눈에", size = 22.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(10.dp))
    MoaText(
        "모아에 연결했던 계좌 중\n해지 내역이 있는 계좌를 확인할 수 있어요.",
        size = 13.sp, color = MoaTextSecondary, lineHeight = 20.sp,
    )
    Spacer(Modifier.height(20.dp))
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MoaSurface)
                .clickable { periodMenu = true }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoaText(period, size = 14.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MoaTextPrimary)
        }
        DropdownMenu(periodMenu, onDismissRequest = { periodMenu = false }, containerColor = MoaSurface) {
            periods.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { period = it; periodMenu = false }) }
        }
    }
    Spacer(Modifier.height(18.dp))
    MoaText("해지계좌 ${closed.size}개", size = 12.sp, color = MoaTextTertiary)
    Spacer(Modifier.height(10.dp))

    closed.forEach { c ->
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText(c.bankName, size = 13.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
                Tag("해지 완료", container = MoaChip, content = MoaTextSecondary)
            }
            Spacer(Modifier.height(14.dp))
            AccountSummary(c.bankName.take(1), c.name, "${c.bankName} · ${c.number.substringBeforeLast('-')}-••${c.number.takeLast(4)}")
            HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(vertical = 16.dp))
            InfoRow("개설일", c.openedAt)
            InfoRow("해지일", c.closedAt)
            InfoRow("해지 시 잔액", formatWon(c.finalBalance))
        }
        Spacer(Modifier.height(16.dp))
    }

    WhiteCard(color = MoaBlueLight) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            MoaText("해지계좌 이용 안내", size = 14.sp, weight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        MoaText(
            "해지된 계좌는 이체·출금할 수 없어요. 해지 이전의 거래내역이나 증명서가 필요하면 해당 금융기관에 문의해 주세요.",
            size = 12.sp, color = MoaTextSecondary, lineHeight = 18.sp,
        )
    }
    Spacer(Modifier.height(10.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { showMessage("고객센터 1588-0000으로 연결해 드릴게요") }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = MoaTextPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        MoaText("계좌 관련 도움이 필요하신가요?", size = 14.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MoaTextTertiary, modifier = Modifier.size(18.dp))
    }
}
