package com.moa.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueSoft
import com.moa.finance.ui.theme.InstDefaultFg
import com.moa.finance.ui.theme.InstDefaultBg
import com.moa.finance.ui.theme.InstNaraeFg
import com.moa.finance.ui.theme.InstNaraeBg
import com.moa.finance.ui.theme.InstSaebomFg
import com.moa.finance.ui.theme.InstSaebomBg
import com.moa.finance.ui.theme.InstHanbitFg
import com.moa.finance.ui.theme.InstHanbitBg
import com.moa.finance.ui.theme.MoaSuccessSoft
import com.moa.finance.ui.theme.MoaDangerSoft
import com.moa.finance.ui.theme.MoaBlueDark
import com.moa.finance.ui.theme.MoaAmberSoft
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaDivider
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextPrimary
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

/** Shows a short message at the bottom of the screen; provided by MoaApp. */
val LocalShowMessage = staticCompositionLocalOf<(String) -> Unit> { {} }

val ScreenPadding = 20.dp

@Composable
fun MoaText(
    text: String,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = MoaTextPrimary,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = size,
        fontWeight = weight,
        color = color,
        textAlign = align,
        lineHeight = lineHeight,
        maxLines = maxLines,
    )
}

@Composable
fun MoaTopBar(
    title: String,
    onBack: (() -> Unit)?,
    onMore: (() -> Unit)? = null,
) {
    val showMessage = LocalShowMessage.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MoaSurface)
            .statusBarsPadding()
            .height(56.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = MoaTextPrimary)
            }
        }
        MoaText(title, size = 17.sp, weight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        IconButton(
            onClick = onMore ?: { showMessage("더보기 메뉴는 준비 중이에요") },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
        ) {
            Icon(Icons.Filled.MoreHoriz, contentDescription = "더보기", tint = MoaTextPrimary)
        }
    }
}

@Composable
fun MoaTabRow(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = ScreenPadding)) {
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val isSelected = index == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MoaText(
                        label,
                        size = 13.sp,
                        weight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MoaBlue else MoaTextTertiary,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) MoaBlue else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = MoaDivider, thickness = 1.dp)
    }
}

@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(20.dp),
    color: Color = MoaSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .then(if (color == MoaSurface) Modifier.border(1.dp, MoaDivider, RoundedCornerShape(18.dp)) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) MoaBlue else MoaBlue.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MoaText(text, size = 16.sp, weight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MoaBlueLight,
    content: Color = MoaBlue,
    height: Dp = 52.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(8.dp))
        }
        MoaText(text, size = 15.sp, weight = FontWeight.Bold, color = content)
    }
}

/** 은행명 → (배경, 글자) 마크 색. 디자인의 .inst-* 규칙. */
fun instColors(bankName: String): Pair<Color, Color> = when {
    bankName.contains("한빛") -> InstHanbitBg to InstHanbitFg
    bankName.contains("새봄") -> InstSaebomBg to InstSaebomFg
    bankName.contains("늘봄") || bankName.contains("나래") -> InstNaraeBg to InstNaraeFg
    else -> InstDefaultBg to InstDefaultFg
}

@Composable
fun BankBadge(initial: String, bankName: String = "", size: Dp = 40.dp) {
    val (bg, fg) = instColors(bankName.ifEmpty { initial })
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        MoaText(initial, size = 15.sp, weight = FontWeight.Bold, color = fg)
    }
}

/** 안내 박스 (.notice / -warning / -error / -success). */
enum class NoticeKind { Info, Warning, Error, Success }

@Composable
fun Notice(text: String, kind: NoticeKind = NoticeKind.Info, modifier: Modifier = Modifier) {
    val (bg, fg) = when (kind) {
        NoticeKind.Info -> MoaBlueSoft to Color(0xFF2A3F8F)
        NoticeKind.Warning -> MoaAmberSoft to Color(0xFF87500A)
        NoticeKind.Error -> MoaDangerSoft to Color(0xFFA8222F)
        NoticeKind.Success -> MoaSuccessSoft to Color(0xFF0F6847)
    }
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        MoaText(text, size = 13.sp, color = fg, lineHeight = 20.sp)
    }
}

@Composable
fun Tag(text: String, container: Color = MoaBlueLight, content: Color = MoaBlue, onClick: (() -> Unit)? = null) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        MoaText(text, size = 11.sp, weight = FontWeight.SemiBold, color = content)
    }
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    MoaText(text, size = 13.sp, color = MoaTextSecondary, modifier = modifier.padding(bottom = 8.dp))
}

@Composable
fun MoaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interaction,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation('•') else VisualTransformation.None,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MoaTextPrimary, textAlign = textAlign),
        cursorBrush = SolidColor(MoaBlue),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MoaSurface)
                    .border(
                        width = if (focused) 1.5.dp else 0.dp,
                        color = if (focused) MoaBlue else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        MoaText(placeholder, size = 15.sp, color = MoaTextTertiary)
                    }
                    inner()
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
        },
    )
}

@Composable
fun SelectBox(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MoaSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoaText(text, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MoaTextPrimary)
    }
}

@Composable
fun MenuRow(
    title: String,
    icon: ImageVector? = null,
    subtitle: String? = null,
    iconTint: Color = MoaTextPrimary,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            MoaText(title, size = 15.sp, weight = FontWeight.SemiBold)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                MoaText(subtitle, size = 12.sp, color = MoaTextTertiary)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MoaTextTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun MenuGroup(content: @Composable ColumnScope.() -> Unit) {
    WhiteCard(padding = PaddingValues(vertical = 6.dp), content = content)
}

@Composable
fun MenuDivider() {
    HorizontalDivider(color = MoaDivider, modifier = Modifier.padding(horizontal = 20.dp))
}

@Composable
fun InfoRow(label: String, value: String, valueWeight: FontWeight = FontWeight.SemiBold) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        MoaText(label, size = 13.sp, color = MoaTextTertiary, modifier = Modifier.weight(1f))
        MoaText(value, size = 13.sp, weight = valueWeight)
    }
}

@Composable
fun AccountSummary(
    bankName: String,
    name: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BankBadge(bankName.take(1), bankName)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            MoaText(name, size = 15.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            MoaText(subtitle, size = 12.sp, color = MoaTextTertiary)
        }
        trailing?.invoke()
    }
}
