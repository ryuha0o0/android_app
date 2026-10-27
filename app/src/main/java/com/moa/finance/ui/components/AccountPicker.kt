package com.moa.finance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.Account
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextTertiary

/** White card showing the selected account; tapping it opens a dropdown of [accounts]. */
@Composable
fun AccountPickerCard(
    selected: Account,
    accounts: List<Account>,
    onSelect: (Account) -> Unit,
    label: String? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        WhiteCard(onClick = { expanded = true }) {
            if (label != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText(label, size = 12.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "계좌 선택", tint = MoaTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                AccountSummary(selected.bankInitial, selected.name, "${selected.bankName} · ${selected.maskedNumber}")
            } else {
                AccountSummary(selected.bankInitial, selected.name, "${selected.bankName} · ${selected.maskedNumber}") {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "계좌 선택", tint = MoaTextPrimary)
                }
            }
            footer?.invoke()
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MoaSurface) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            MoaText(account.name, size = 14.sp, weight = FontWeight.SemiBold)
                            MoaText("${account.bankName} · ${formatWon(account.balance)}", size = 12.sp, color = MoaTextTertiary)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(account)
                    },
                )
            }
        }
    }
}
