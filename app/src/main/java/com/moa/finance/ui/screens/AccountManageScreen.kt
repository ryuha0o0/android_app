package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.AccountPickerCard
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MenuDivider
import com.moa.finance.ui.components.MenuGroup
import com.moa.finance.ui.components.MenuRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.components.formatWon
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

@Composable
fun AccountManageScreen(onBack: () -> Unit) {
    val repo = MoaRepository
    val showMessage = LocalShowMessage.current
    var accountId by rememberSaveable { mutableStateOf(repo.primaryAccountId) }
    var renaming by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var limitRequest by remember { mutableStateOf(false) }
    var limitRequested by remember { mutableStateOf(false) }
    val account = repo.account(accountId)

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "계좌 관리", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(20.dp))
            AccountPickerCard(account, repo.accounts, onSelect = { accountId = it.id }) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText(formatWon(account.balance), size = 24.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Tag(account.tag)
                }
            }

            Spacer(Modifier.height(26.dp))
            MoaText("계좌 설정", size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            MenuGroup {
                MenuRow("계좌 비밀번호 변경", Icons.Outlined.Key, "현재 비밀번호 확인 후 변경할 수 있어요", iconTint = MoaBlue) {
                    showMessage("계좌 비밀번호 변경은 영업점 또는 ARS에서 할 수 있어요")
                }
                MenuDivider()
                MenuRow("계좌 별명 변경", Icons.Outlined.Edit, account.name) {
                    newName = account.name
                    renaming = true
                }
                MenuDivider()
                MenuRow("대표계좌 설정", Icons.Outlined.StarBorder) {
                    repo.setPrimary(account.id)
                    showMessage("${account.name}을(를) 대표계좌로 설정했어요")
                }
            }

            Spacer(Modifier.height(18.dp))
            WhiteCard(color = MoaBlueLight) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoaText("한도제한 계좌", size = 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    MoaText(
                        "해제 가능 여부 확인", size = 12.sp, weight = FontWeight.Bold, color = MoaBlue,
                        modifier = Modifier.clickable { showMessage("증빙서류 제출 시 해제할 수 있어요") },
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row {
                    MoaText("1회 이체 한도", size = 13.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
                    MoaText("100만원", size = 15.sp, weight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    MoaText("1일 이체 한도", size = 13.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
                    MoaText("100만원", size = 15.sp, weight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                MoaText(
                    "거래 목적 확인과 증빙서류 심사 후 해제돼요.\n금융기관 기준에 따라 추가 확인이 필요할 수 있어요.",
                    size = 12.sp, color = MoaTextSecondary, lineHeight = 18.sp,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    if (limitRequested) "해제 신청 접수됨" else "한도제한 해제 신청",
                    enabled = !limitRequested,
                    onClick = { limitRequest = true },
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoaText("계좌 연결을 해제할 수 있어요", size = 12.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
                MoaText(
                    "연결 해제", size = 12.sp, weight = FontWeight.SemiBold, color = MoaTextSecondary,
                    modifier = Modifier.clickable { showMessage("대표계좌는 연결 해제할 수 없어요") },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (renaming) {
        AlertDialog(
            onDismissRequest = { renaming = false },
            containerColor = MoaSurface,
            title = { Text("계좌 별명 변경", fontWeight = FontWeight.Bold) },
            text = { MoaTextField(newName, { newName = it.take(15) }, placeholder = "별명 입력") },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        val index = repo.accounts.indexOfFirst { it.id == account.id }
                        repo.accounts[index] = account.copy(name = newName.trim())
                        renaming = false
                    },
                ) { Text("저장", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("취소", color = MoaTextSecondary) } },
        )
    }
    if (limitRequest) {
        AlertDialog(
            onDismissRequest = { limitRequest = false },
            containerColor = MoaSurface,
            title = { Text("한도제한 해제 신청", fontWeight = FontWeight.Bold) },
            text = { Text("급여·사업 등 거래 목적을 확인할 수 있는 서류가 필요해요. 신청 후 영업일 기준 2~3일 안에 결과를 알려드려요.") },
            confirmButton = {
                TextButton(onClick = {
                    limitRequest = false
                    limitRequested = true
                    showMessage("한도제한 해제 신청이 접수됐어요")
                }) { Text("신청하기", color = MoaBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { limitRequest = false }) { Text("취소", color = MoaTextSecondary) } },
        )
    }
}
