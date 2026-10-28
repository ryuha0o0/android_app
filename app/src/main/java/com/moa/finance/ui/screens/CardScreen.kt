package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.Notice
import com.moa.finance.ui.components.NoticeKind
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.Tag
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueSoft
import com.moa.finance.ui.theme.MoaCardNavy
import com.moa.finance.ui.theme.MoaTextTertiary

/**
 * 카드 — 정적 소개 페이지. (회의 결정: 카드 신청·관리 기능 제거, 소개만 유지)
 */
@Composable
fun CardScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "카드", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(22.dp))
            Tag("일상에 꼭 맞는 체크카드", container = MoaBlueSoft)
            Spacer(Modifier.height(14.dp))
            MoaText(
                "매일 쓰는 만큼,\n차곡차곡 돌아오는 혜택",
                size = 23.sp, weight = FontWeight.ExtraBold, lineHeight = 33.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(26.dp))
            CardVisual(Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(18.dp))
            MoaText("Labbit 데일리 체크", size = 17.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            MoaText("연회비 없음 · 국내전용 · 후불교통 선택", size = 12.sp, color = MoaTextTertiary)

            Spacer(Modifier.height(22.dp))
            WhiteCard(padding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)) {
                Benefit(Icons.Outlined.LocalCafe, "커피 · 편의점 5% 캐시백", "자주 가는 곳에서 쏠쏠하게")
                Benefit(Icons.Outlined.DirectionsBus, "대중교통 3% 캐시백", "출퇴근길에도 챙기는 혜택")
                Benefit(Icons.Outlined.ShoppingBag, "온라인 쇼핑 1% 캐시백", "일상 속 쇼핑을 더 가볍게")
            }

            Spacer(Modifier.height(16.dp))
            Notice(
                "카드 신청·관리 기능은 준비 중이에요. 상품 안내만 제공합니다.",
                kind = NoticeKind.Info,
            )
            Spacer(Modifier.height(10.dp))
            MoaText(
                "전월 이용금액 20만원 이상 · 월 통합 최대 1만원\n혜택별 대상 가맹점과 제공 조건을 확인해 주세요.",
                size = 11.sp, color = MoaTextTertiary, lineHeight = 17.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CardVisual(modifier: Modifier = Modifier) {
    Column(
        modifier
            .width(260.dp)
            .height(158.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF4561E6), MoaCardNavy)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("Labbit", size = 16.sp, weight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Contactless, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(width = 32.dp, height = 24.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFDCE2EC)))
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoaText("데일리 체크", size = 13.sp, weight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
            MoaText("국내전용", size = 11.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun Benefit(icon: ImageVector, title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MoaBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            MoaText(title, size = 14.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            MoaText(desc, size = 12.sp, color = MoaTextTertiary)
        }
    }
}
