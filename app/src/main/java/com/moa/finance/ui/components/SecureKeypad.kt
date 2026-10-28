package com.moa.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.security.VulnConfig
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextTertiary

/**
 * 보안 키패드 + 6자리 입력 표시.
 *
 * 안전: 숫자 배열을 매 입력 세션마다 섞는다(STATIC_KEYPAD 켜면 고정 → 터치 좌표로 PIN 유추 가능).
 * 입력값은 화면에 ●로만 표시하고 평문을 노출하지 않는다.
 */
@Composable
fun SecurePinPad(
    length: Int = 6,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 배열은 재조합(remember) 시 한 번 섞고, 고정 모드면 0~9 순서 유지
    val digits = remember(VulnConfig.isVulnerable(VulnConfig.Flag.STATIC_KEYPAD)) {
        val base = (0..9).map { it.toString() }
        if (VulnConfig.isVulnerable(VulnConfig.Flag.STATIC_KEYPAD)) base else base.shuffled()
    }

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // 입력 표시(●)
        Row {
            repeat(length) { i ->
                Box(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (i < value.length) MoaBlue else MoaDivider)
                )
            }
        }
        Spacer(Modifier.height(28.dp))

        // 3x4 그리드: 숫자 9개 + 빈칸 + 마지막 숫자 + 지우기
        val cells = digits.take(9) + listOf("", digits[9], "⌫")
        Column(Modifier.fillMaxWidth()) {
            for (r in 0 until 4) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (c in 0 until 3) {
                        val label = cells[r * 3 + c]
                        KeypadKey(label, Modifier.weight(1f)) {
                            when (label) {
                                "" -> {}
                                "⌫" -> if (value.isNotEmpty()) onChange(value.dropLast(1))
                                else -> if (value.length < length) onChange(value + label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .padding(6.dp)
            .aspectRatio(1.6f)
            .clip(CircleShape)
            .then(if (label.isEmpty()) Modifier else Modifier.background(MoaSurface).clickable(onClick = onClick)),
        contentAlignment = Alignment.Center,
    ) {
        when (label) {
            "" -> {}
            "⌫" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "지우기", tint = MoaTextTertiary)
            else -> androidx.compose.material3.Text(label, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = MoaTextPrimary)
        }
    }
}
