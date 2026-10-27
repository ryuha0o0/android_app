package com.moa.finance.data

data class Account(
    val id: String,
    val bankName: String,
    val name: String,
    val number: String,
    val balance: Long,
    val tag: String,
    val actionLabel: String = "이체하기",
    val transferable: Boolean = true,
) {
    val bankInitial: String get() = bankName.take(1)
    val maskedNumber: String get() = number.substringBeforeLast('-') + "-••" + number.takeLast(4)
    val shortNumber: String get() = "••" + number.takeLast(4)
}

data class Transaction(
    val id: Long,
    val accountId: String,
    val dateLabel: String,
    val time: String,
    val title: String,
    val method: String,
    val amount: Long,
    val balanceAfter: Long,
) {
    val isDeposit: Boolean get() = amount > 0
}

data class TransferResult(
    val id: Long,
    val recipientName: String,
    val recipientBank: String,
    val recipientNumber: String,
    val fromAccount: Account,
    val amount: Long,
    val dateTime: String,
    val fee: Long = 0,
    val memo: String,
)

data class ClosedAccount(
    val bankName: String,
    val name: String,
    val number: String,
    val openedAt: String,
    val closedAt: String,
    val finalBalance: Long,
)

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val joinedDays: Int,
)
