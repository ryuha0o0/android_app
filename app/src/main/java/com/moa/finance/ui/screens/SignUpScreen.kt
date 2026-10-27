package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.MoaRepository
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

private val terms = listOf("[필수] 서비스 이용약관", "[필수] 개인정보 수집·이용 동의", "[필수] 휴대폰 본인 확인 동의")
private val carriers = listOf("SKT", "KT", "LG U+", "알뜰폰")

@Composable
fun SignUpScreen(onBack: () -> Unit, onComplete: () -> Unit) {
    val showMessage = LocalShowMessage.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var carrier by remember { mutableStateOf<String?>(null) }
    var carrierMenu by remember { mutableStateOf(false) }
    val agreed = remember { mutableStateListOf(false, false, false) }
    var showDone by remember { mutableStateOf(false) }

    val allAgreed = agreed.all { it }
    val canSubmit = name.isNotBlank() && phone.filter(Char::isDigit).length >= 10 && carrier != null && allAgreed

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "회원가입", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(Modifier.height(16.dp))
            Row {
                MoaText("본인 확인 · 1 / 3", size = 12.sp, weight = FontWeight.Bold, color = MoaBlue, modifier = Modifier.weight(1f))
                MoaText("약관 동의 → 가입 완료", size = 12.sp, color = MoaTextTertiary)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(if (i == 0) MoaBlue else MoaDivider)
                    )
                    if (i < 2) Spacer(Modifier.width(6.dp))
                }
            }

            Spacer(Modifier.height(28.dp))
            MoaText("반가워요!\n먼저 본인 확인을 할게요", size = 24.sp, weight = FontWeight.ExtraBold, lineHeight = 34.sp)
            Spacer(Modifier.height(10.dp))
            MoaText("본인 명의의 휴대폰을 준비해 주세요.", size = 13.sp, color = MoaTextSecondary)

            Spacer(Modifier.height(28.dp))
            FieldLabel("이름")
            MoaTextField(name, { name = it }, placeholder = "김모아")
            Spacer(Modifier.height(18.dp))
            FieldLabel("휴대폰 번호")
            Box {
                MoaTextField(
                    phone,
                    { phone = it.filter { c -> c.isDigit() || c == '-' }.take(13) },
                    placeholder = "010-0000-1234",
                    keyboardType = KeyboardType.Phone,
                ) {
                    MoaText(
                        carrier ?: "통신사 선택",
                        size = 13.sp,
                        weight = FontWeight.SemiBold,
                        color = MoaBlue,
                        modifier = Modifier.clickable { carrierMenu = true },
                    )
                }
                DropdownMenu(carrierMenu, onDismissRequest = { carrierMenu = false }, containerColor = MoaSurface) {
                    carriers.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { carrier = it; carrierMenu = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MoaTextTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                MoaText("화면의 이름과 연락처는 가상 정보예요.", size = 12.sp, color = MoaTextTertiary)
            }

            Spacer(Modifier.height(24.dp))
            WhiteCard {
                Row(
                    Modifier.fillMaxWidth().clickable {
                        val target = !allAgreed
                        for (i in agreed.indices) agreed[i] = target
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (allAgreed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (allAgreed) MoaBlue else MoaTextTertiary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    MoaText("필수 약관에 모두 동의", size = 15.sp, weight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                terms.forEachIndexed { i, term ->
                    Row(
                        Modifier.fillMaxWidth().clickable { agreed[i] = !agreed[i] }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (agreed[i]) MoaBlue else MoaDivider,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        MoaText(term, size = 13.sp, color = MoaTextSecondary, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "약관 보기",
                            tint = MoaTextTertiary,
                            modifier = Modifier.size(18.dp).clickable { showMessage("$term 전문은 준비 중이에요") },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            PrimaryButton("동의하고 본인 확인", onClick = {
                MoaRepository.user = MoaRepository.user.copy(name = name.trim(), phone = phone)
                showDone = true
            }, enabled = canSubmit)
            Spacer(Modifier.height(14.dp))
            MoaText(
                "본인 확인 후 금융인증서를 연결해요.",
                size = 12.sp,
                color = MoaTextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDone) {
        AlertDialog(
            onDismissRequest = { showDone = false },
            containerColor = MoaSurface,
            title = { Text("본인 확인 완료", fontWeight = FontWeight.Bold) },
            text = { Text("${name.trim()}님, 본인 확인이 끝났어요.\n금융인증서를 연결하고 모아를 시작해 볼까요?") },
            confirmButton = {
                TextButton(onClick = { showDone = false; onComplete() }) { Text("시작하기", color = MoaBlue) }
            },
        )
    }
}
