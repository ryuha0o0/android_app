package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.security.MockBackend
import com.moa.finance.security.E2ECrypto
import com.moa.finance.ui.components.AccountPickerCard
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaTabRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.SelectBox
import com.moa.finance.ui.components.formatNumber
import com.moa.finance.ui.components.formatWon
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

private const val TRANSFER_LIMIT = 1_000_000L

@Composable
fun TransferScreen(
    initialTab: Int,
    initialAccountId: String?,
    onBack: () -> Unit,
    onTransferDone: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "이체 / 출금", onBack = onBack)
        MoaTabRow(listOf("계좌이체", "출금"), tab, onSelect = { tab = it })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            if (tab == 0) TransferForm(initialAccountId, onTransferDone) else WithdrawForm(initialAccountId)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Keeps only digits and caps the value; returns null for an empty field. */
private fun parseAmount(text: String): Long? = text.filter(Char::isDigit).take(12).toLongOrNull()

@Composable
private fun ColumnScope.TransferForm(initialAccountId: String?, onTransferDone: () -> Unit) {
    val repo = MoaRepository
    val showMessage = LocalShowMessage.current
    var fromId by rememberSaveable {
        mutableStateOf(initialAccountId?.takeIf { id -> repo.transferableAccounts.any { it.id == id } } ?: repo.primaryAccountId)
    }
    var bank by rememberSaveable { mutableStateOf(repo.banks[1]) }
    var bankMenu by remember { mutableStateOf(false) }
    var recentMenu by remember { mutableStateOf(false) }
    var number by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var tamper by remember { mutableStateOf(false) }
    var tamperFrom by rememberSaveable { mutableStateOf(repo.primaryAccountId) }
    var tamperAmount by rememberSaveable { mutableStateOf("") }

    val from = repo.account(fromId)
    val recipient = repo.lookupRecipient(number)
    val error = when {
        amount == null -> null
        amount!! > from.balance -> "이체 가능 금액을 초과했어요"
        amount!! > TRANSFER_LIMIT -> "1회 이체 한도는 100만원이에요"
        else -> null
    }
    val canSubmit = recipient != null && (amount ?: 0) > 0 && error == null

    Spacer(Modifier.height(26.dp))
    MoaText("누구에게 보낼까요?", size = 22.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    MoaText("받는 분의 계좌를 확인해 주세요.", size = 13.sp, color = MoaTextSecondary)
    Spacer(Modifier.height(20.dp))

    AccountPickerCard(from, repo.transferableAccounts, onSelect = { fromId = it.id }, label = "출금계좌") {
        Spacer(Modifier.height(12.dp))
        MoaText("이체 가능 ${formatWon(from.balance)}", size = 12.sp, color = MoaTextTertiary)
    }

    Spacer(Modifier.height(22.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        FieldLabel("받는 분", Modifier.weight(1f))
        Box {
            MoaText(
                "최근 / 자주 쓰는",
                size = 12.sp,
                weight = FontWeight.SemiBold,
                color = MoaBlue,
                modifier = Modifier.padding(bottom = 8.dp).clickable { recentMenu = true },
            )
            DropdownMenu(recentMenu, onDismissRequest = { recentMenu = false }, containerColor = MoaSurface) {
                repo.transferResults.distinctBy { it.recipientNumber }.forEach { r ->
                    DropdownMenuItem(
                        text = { Text("${r.recipientName} · ${r.recipientBank}") },
                        onClick = {
                            bank = r.recipientBank
                            number = r.recipientNumber
                            recentMenu = false
                        },
                    )
                }
            }
        }
    }
    Box {
        SelectBox(bank, onClick = { bankMenu = true })
        DropdownMenu(bankMenu, onDismissRequest = { bankMenu = false }, containerColor = MoaSurface) {
            repo.banks.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { bank = it; bankMenu = false }) }
        }
    }

    Spacer(Modifier.height(18.dp))
    FieldLabel("계좌번호")
    MoaTextField(
        number,
        { number = it.filter { c -> c.isDigit() || c == '-' }.take(16) },
        placeholder = "계좌번호 입력",
        keyboardType = KeyboardType.Number,
    ) {
        if (recipient != null) {
            MoaText("${recipient} 확인", size = 13.sp, weight = FontWeight.SemiBold, color = MoaBlue)
        }
    }

    Spacer(Modifier.height(18.dp))
    FieldLabel("보낼 금액")
    MoaTextField(
        amount?.let(::formatNumber) ?: "",
        { amount = parseAmount(it) },
        placeholder = "0",
        keyboardType = KeyboardType.Number,
    ) {
        MoaText("원", size = 14.sp, weight = FontWeight.SemiBold, color = MoaBlue)
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("+1만원" to 10_000L, "+5만원" to 50_000L, "+10만원" to 100_000L).forEach { (label, add) ->
            AmountChip(label, Modifier.weight(1f)) { amount = (amount ?: 0) + add }
        }
        AmountChip("전액", Modifier.weight(1f)) { amount = minOf(from.balance, TRANSFER_LIMIT) }
    }

    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        MoaText(
            error ?: "1회 한도 100만원 · 수수료 무료",
            size = 12.sp,
            color = if (error != null) MoaDanger else MoaTextTertiary,
            modifier = Modifier.weight(1f),
        )
        MoaText(
            "한도 안내",
            size = 12.sp,
            weight = FontWeight.SemiBold,
            color = MoaBlue,
            modifier = Modifier.clickable { showMessage("1회·1일 이체 한도는 각 100만원이에요") },
        )
    }
    Spacer(Modifier.height(14.dp))
    PrimaryButton("다음 · 받는 분 확인", onClick = { confirm = true }, enabled = canSubmit)

    Spacer(Modifier.height(10.dp))
    MoaText(
        "요청 직접 전송 (실습)",
        size = 12.sp, weight = FontWeight.SemiBold, color = MoaBlue,
        modifier = Modifier.clickable { tamper = true }.padding(6.dp),
    )

    if (confirm && recipient != null && amount != null) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = MoaSurface,
            title = { Text("${recipient}님께 보낼까요?", fontWeight = FontWeight.Bold) },
            text = {
                Text("$bank $number\n${formatWon(amount!!)}을 보내요.\n출금계좌: ${from.name} (${from.shortNumber})")
            },
            confirmButton = {
                TextButton(onClick = {
                    // E2E: 입출금 민감 정보(받는 계좌번호)를 암호화 봉투로 전송
                    MockBackend.e2eDecrypt(E2ECrypto.encryptField(number, MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID))
                    runCatching { repo.executeTransfer(from.id, bank, number, recipient, amount!!) }
                        .onSuccess { confirm = false; onTransferDone() }
                        .onFailure { confirm = false; showMessage(it.message ?: "이체에 실패했어요") }
                }) { Text("보내기", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("취소", color = MoaTextSecondary) }
            },
        )
    }

    // 파라미터 변조 실습: 화면 검증(canSubmit)을 우회해 출금계좌 ID와 금액을 직접 입력해 전송.
    if (tamper) {
        AlertDialog(
            onDismissRequest = { tamper = false },
            containerColor = MoaSurface,
            title = { Text("요청 파라미터 직접 전송", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "프록시로 요청을 가로채 변조하는 상황을 재현해요. 화면 한도·소유 검증을 건너뛰고 서버로 바로 보냅니다.",
                        color = MoaTextSecondary, fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    FieldLabel("fromAccountId")
                    MoaTextField(tamperFrom, { tamperFrom = it }, placeholder = "예: saving / install")
                    Spacer(Modifier.height(10.dp))
                    FieldLabel("amount")
                    MoaTextField(tamperAmount, { tamperAmount = it.filter(Char::isDigit) }, placeholder = "예: 9000000", keyboardType = KeyboardType.Number)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = tamperAmount.toLongOrNull() ?: 0L
                    runCatching { repo.executeTransfer(tamperFrom.trim(), bank, number.ifBlank { "000-000-000000" }, recipient ?: "수취인", amt) }
                        .onSuccess { tamper = false; showMessage("변조 요청이 실행됐어요 (${formatWon(amt)})"); onTransferDone() }
                        .onFailure { tamper = false; showMessage("거부됨: ${it.message}") }
                }) { Text("전송", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { tamper = false }) { Text("취소", color = MoaTextSecondary) } },
        )
    }
}

@Composable
private fun ColumnScope.WithdrawForm(initialAccountId: String?) {
    val repo = MoaRepository
    var fromId by rememberSaveable {
        mutableStateOf(initialAccountId?.takeIf { id -> repo.transferableAccounts.any { it.id == id } } ?: repo.primaryAccountId)
    }
    var amount by rememberSaveable { mutableStateOf<Long?>(null) }
    var code by remember { mutableStateOf<String?>(null) }
    val from = repo.account(fromId)
    val valid = amount != null && amount!! in 10_000..from.balance && amount!! % 10_000 == 0L

    Spacer(Modifier.height(26.dp))
    MoaText("카드 없이 출금할게요", size = 22.sp, weight = FontWeight.ExtraBold)
    Spacer(Modifier.height(8.dp))
    MoaText("출금 코드를 받아 가까운 ATM에서 찾을 수 있어요.", size = 13.sp, color = MoaTextSecondary)
    Spacer(Modifier.height(20.dp))
    AccountPickerCard(from, repo.transferableAccounts, onSelect = { fromId = it.id }, label = "출금계좌") {
        Spacer(Modifier.height(12.dp))
        MoaText("출금 가능 ${formatWon(from.balance)}", size = 12.sp, color = MoaTextTertiary)
    }
    Spacer(Modifier.height(22.dp))
    FieldLabel("출금 금액 (만원 단위)")
    MoaTextField(
        amount?.let(::formatNumber) ?: "",
        { amount = parseAmount(it) },
        placeholder = "0",
        keyboardType = KeyboardType.Number,
    ) {
        MoaText("원", size = 14.sp, weight = FontWeight.SemiBold, color = MoaBlue)
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("+1만원" to 10_000L, "+5만원" to 50_000L, "+10만원" to 100_000L, "+50만원" to 500_000L).forEach { (label, add) ->
            AmountChip(label, Modifier.weight(1f)) { amount = (amount ?: 0) + add }
        }
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton("출금 코드 받기", onClick = {
        // E2E: 입출금 민감 정보(출금 금액)를 암호화 봉투로 전송
        MockBackend.e2eDecrypt(E2ECrypto.encryptField(amount.toString(), MockBackend.e2ePublicKey(), MockBackend.E2E_KEY_ID))
        code = repo.withdraw(from.id, amount!!)
    }, enabled = valid)

    code?.let {
        AlertDialog(
            onDismissRequest = { code = null; amount = null },
            containerColor = MoaSurface,
            title = { Text("출금 코드", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(it, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MoaBlue, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("30분 안에 ATM에서 입력해 주세요.", color = MoaTextSecondary)
                }
            },
            confirmButton = { TextButton(onClick = { code = null; amount = null }) { Text("확인", color = MoaBlue) } },
        )
    }
}

@Composable
private fun AmountChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MoaSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MoaText(label, size = 12.sp, weight = FontWeight.SemiBold, color = MoaTextSecondary)
    }
}
