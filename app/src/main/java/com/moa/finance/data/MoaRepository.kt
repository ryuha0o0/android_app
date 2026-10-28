package com.moa.finance.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.moa.finance.security.VulnConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * In-memory data source with sample data from the design.
 * Values are Compose state so every screen updates when a transfer happens.
 */
object MoaRepository {

    const val TRANSFER_LIMIT = 1_000_000L

    val banks = listOf("한빛은행", "새봄은행", "늘봄은행", "Labbit은행", "하나로은행")

    /** 테스트 계정 (실제 CA 연동 대신 CI 기반 인증서 로그인 실습용). */
    const val TEST_USER_ID = "labbit-test-01"
    const val TEST_CI = "CI:TEST:9f1c7a0b3d5e4f628a1b"  // 가짜 연계정보(CI)

    var user by mutableStateOf(
        UserProfile(name = "김모아", phone = "010-0000-1234", email = "moa@example.com", joinedDays = 128, ci = TEST_CI)
    )

    val accounts = mutableStateListOf(
        Account("living", "한빛은행", "생활비 통장", "110-245-337890", 3_245_800, tag = "대표계좌"),
        Account("saving", "새봄은행", "모으기 통장", "220-018-554507", 8_500_000, tag = "입출금"),
        Account("install", "늘봄은행", "차곡차곡 적금", "3333-08-221234", 2_000_000, tag = "적금", actionLabel = "상세보기", transferable = false),
    )

    val transactions = mutableStateListOf(
        Transaction(4, "living", "10월 24일 토요일", "09:32", "김하늘", "계좌이체", -50_000, 3_245_800),
        Transaction(3, "living", "10월 23일 금요일", "10:00", "모아컴퍼니", "급여", 2_800_000, 3_295_800),
        Transaction(2, "living", "10월 23일 금요일", "08:45", "오늘커피", "체크카드", -4_500, 495_800),
        Transaction(1, "living", "10월 22일 목요일", "18:20", "모아마트", "체크카드", -32_700, 500_300),
    )

    val transferResults = mutableStateListOf(
        TransferResult(2, "김하늘", "새봄은행", "220-145-005678", accounts[0], 50_000, "2026.10.24 09:32", memo = "김모아"),
        TransferResult(1, "내 모으기 통장", "새봄은행", "220-018-554507", accounts[0], 300_000, "2026.10.10 08:10", memo = "김모아"),
    )

    val closedAccounts = listOf(
        ClosedAccount("한빛은행", "여행 준비 통장", "110-198-552345", "2024.03.12", "2026.08.31", 0),
    )

    var balanceHidden by mutableStateOf(false)
    var lastRefreshed by mutableStateOf("09:40")
    var cardPaused by mutableStateOf(false)
    var primaryAccountId by mutableStateOf("living")

    private val recipients = mapOf("220-145-005678" to "김하늘", "220145005678" to "김하늘")

    val totalBalance: Long get() = accounts.sumOf { it.balance }
    val transferableAccounts: List<Account> get() = accounts.filter { it.transferable }

    fun account(id: String?): Account = accounts.firstOrNull { it.id == id } ?: accounts.first()

    fun transactionsOf(accountId: String): List<Transaction> =
        transactions.filter { it.accountId == accountId }.sortedByDescending { it.id }

    /** Looks up the account holder; any number with 10+ digits resolves in this demo. */
    fun lookupRecipient(accountNumber: String): String? {
        val digits = accountNumber.filter(Char::isDigit)
        if (digits.length < 10) return null
        return recipients[accountNumber] ?: recipients[digits] ?: "김하늘"
    }

    fun refresh() {
        lastRefreshed = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    /**
     * 이체 실행. 서버가 없으므로 이 함수가 "서버 측 최종 검증" 역할을 합니다.
     *
     * 파라미터 변조 실습 (D-04,21 / B-08,17):
     *  - PARAM_TAMPER_TRANSFER ON  : 출금계좌 소유/이체가능 여부, 1회 한도, 잔액을
     *        재검증하지 않음 → 화면 검증을 우회해 보낸 변조 요청이 그대로 실행됨
     *        (한도 초과 이체, 비이체 계좌 출금, 잔액 초과 음수잔액 등).
     *  - OFF : 소유·한도·잔액을 모두 재검증하고 위반 시 거부.
     */
    fun executeTransfer(fromId: String, bank: String, number: String, recipient: String, amount: Long): TransferResult {
        val index = accounts.indexOfFirst { it.id == fromId }
        require(index >= 0) { "존재하지 않는 출금계좌예요." }
        val from = accounts[index]
        if (!VulnConfig.isVulnerable(VulnConfig.Flag.PARAM_TAMPER_TRANSFER)) {
            require(from.transferable) { "이체할 수 없는 계좌예요." }
            require(amount in 1..TRANSFER_LIMIT) { "1회 이체 한도(100만원)를 벗어났어요." }
            require(amount <= from.balance) { "잔액이 부족해요." }
        } else {
            require(amount > 0) { "금액을 확인해 주세요." }
        }
        return transfer(fromId, bank, number, recipient, amount)
    }

    fun transfer(fromId: String, bank: String, number: String, recipient: String, amount: Long): TransferResult {
        val index = accounts.indexOfFirst { it.id == fromId }
        require(index >= 0) { "Unknown account $fromId" }
        val from = accounts[index]

        val updated = from.copy(balance = from.balance - amount)
        accounts[index] = updated

        val now = LocalDateTime.now()
        val nextId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
        transactions.add(
            Transaction(
                id = nextId,
                accountId = fromId,
                dateLabel = now.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREAN)),
                time = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                title = recipient,
                method = "계좌이체",
                amount = -amount,
                balanceAfter = updated.balance,
            )
        )
        val result = TransferResult(
            id = (transferResults.maxOfOrNull { it.id } ?: 0) + 1,
            recipientName = recipient,
            recipientBank = bank,
            recipientNumber = number,
            fromAccount = updated,
            amount = amount,
            dateTime = now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            memo = user.name,
        )
        transferResults.add(result)
        return result
    }

    fun withdraw(fromId: String, amount: Long): String {
        val index = accounts.indexOfFirst { it.id == fromId }
        val from = accounts[index]
        require(amount in 1..from.balance) { "Insufficient balance" }
        val updated = from.copy(balance = from.balance - amount)
        accounts[index] = updated
        val now = LocalDateTime.now()
        transactions.add(
            Transaction(
                id = (transactions.maxOfOrNull { it.id } ?: 0) + 1,
                accountId = fromId,
                dateLabel = now.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", java.util.Locale.KOREAN)),
                time = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                title = "ATM 출금",
                method = "출금",
                amount = -amount,
                balanceAfter = updated.balance,
            )
        )
        return (100000..999999).random().toString()
    }

    fun setPrimary(accountId: String) {
        primaryAccountId = accountId
        for (i in accounts.indices) {
            val acc = accounts[i]
            accounts[i] = when {
                acc.id == accountId -> acc.copy(tag = "대표계좌")
                acc.tag == "대표계좌" -> acc.copy(tag = "입출금")
                else -> acc
            }
        }
    }
}
