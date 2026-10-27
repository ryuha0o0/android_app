package com.moa.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moa.finance.data.BoardRepository
import com.moa.finance.security.FileVault
import com.moa.finance.security.VulnConfig
import com.moa.finance.ui.components.FieldLabel
import com.moa.finance.ui.components.LabBridge
import com.moa.finance.ui.components.LabHtml
import com.moa.finance.ui.components.LabWebView
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.components.MoaTabRow
import com.moa.finance.ui.components.MoaText
import com.moa.finance.ui.components.MoaTextField
import com.moa.finance.ui.components.MoaTopBar
import com.moa.finance.ui.components.PrimaryButton
import com.moa.finance.ui.components.ScreenPadding
import com.moa.finance.ui.components.WhiteCard
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaBlueLight
import com.moa.finance.ui.theme.MoaDanger
import com.moa.finance.ui.theme.MoaSuccess
import com.moa.finance.ui.theme.MoaTextSecondary
import com.moa.finance.ui.theme.MoaTextTertiary

/**
 * 메인창(커뮤니티) — 검색·공지사항·게시판·고객센터.
 * XSS, CSRF, 파일 업로드/다운로드(Path Traversal) 실습 표면이 모여 있습니다.
 */
@Composable
fun MainBoardScreen(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    // 세션별 CSRF 토큰(안전 모드에서 상태변경 요청에 요구)
    val csrfToken = remember { "csrf-" + (100000..999999).random() }
    var alert by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // msg, isDanger

    Column(Modifier.fillMaxSize().background(MoaBackground)) {
        MoaTopBar(title = "메인", onBack = onBack)
        MoaTabRow(listOf("검색", "공지사항", "게시판", "고객센터"), tab, onSelect = { tab = it })

        alert?.let { (msg, danger) ->
            WhiteCard(
                color = if (danger) MoaBlueLight else MoaBlueLight,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
            ) {
                MoaText(
                    msg, size = 13.sp, weight = FontWeight.Bold,
                    color = if (danger) MoaDanger else MoaSuccess,
                )
            }
        }

        val bridge = remember {
            LabBridge(
                onReport = { tag, msg -> alert = "⚠ $tag 페이로드가 실행됐어요: $msg" to true },
                onAction = { name, token ->
                    val secure = !VulnConfig.isVulnerable(VulnConfig.Flag.NO_CSRF_TOKEN)
                    if (secure && token != csrfToken) {
                        alert = "✓ CSRF 토큰이 없어 '$name' 요청을 차단했어요" to false
                    } else {
                        alert = "⚠ 토큰 검증 없이 '$name' 상태변경이 실행됐어요 (CSRF)" to true
                    }
                },
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            when (tab) {
                0 -> SearchTab(bridge)
                1 -> NoticeTab(bridge, csrfToken)
                2 -> BoardTab()
                else -> SupportTab()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColumnScope.SearchTab(bridge: LabBridge) {
    var query by remember { mutableStateOf("") }
    var html by remember { mutableStateOf(LabHtml.page("<p class='muted'>계좌·이체·카드를 검색해 보세요.</p>")) }

    Spacer(Modifier.height(20.dp))
    FieldLabel("통합검색")
    MoaTextField(query, { query = it }, placeholder = "검색어 입력")
    Spacer(Modifier.height(10.dp))
    PrimaryButton("검색", onClick = {
        val q = LabHtml.field(query)
        html = LabHtml.page("<h3>'$q' 검색 결과</h3><p class='muted'>일치하는 항목이 없어요.</p>")
    })
    Spacer(Modifier.height(8.dp))
    MoaText(
        "실습: 검색어에  <img src=x onerror=\"LabBridge.report('XSS','검색창')\">  같은 입력을 넣어 보세요.",
        size = 11.sp, color = MoaTextTertiary, lineHeight = 16.sp,
    )
    Spacer(Modifier.height(12.dp))
    LabWebView(html, bridge, Modifier.fillMaxWidth().height(220.dp))
}

@Composable
private fun ColumnScope.NoticeTab(bridge: LabBridge, csrfToken: String) {
    val notices = BoardRepository.notices
    var selected by remember { mutableStateOf(notices.firstOrNull()) }
    val body = selected?.let {
        LabHtml.page(
            "<h3>${LabHtml.field(it.title)}</h3>" +
                "<p class='muted'>${it.date}</p>" +
                "<div class='card'>${LabHtml.field(it.body)}</div>",
        )
    } ?: LabHtml.page("<p class='muted'>공지가 없어요.</p>")

    Spacer(Modifier.height(20.dp))
    notices.forEach { n ->
        WhiteCard(onClick = { selected = n }) {
            MoaText(n.title, size = 15.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            MoaText(n.date, size = 12.sp, color = MoaTextTertiary)
        }
        Spacer(Modifier.height(10.dp))
    }
    Spacer(Modifier.height(6.dp))
    MoaText(
        "실습: 공지 본문에 저장된 스크립트가 LabBridge.action('회원탈퇴','') 를 호출하면 " +
            "CSRF 토큰 없이 상태변경이 되는지 확인해 보세요.",
        size = 11.sp, color = MoaTextTertiary, lineHeight = 16.sp,
    )
    Spacer(Modifier.height(10.dp))
    LabWebView(body, bridge, Modifier.fillMaxWidth().height(240.dp))
}

@Composable
private fun ColumnScope.BoardTab() {
    val context = LocalContext.current
    val vault = remember { FileVault(context) }
    val showMessage = LocalShowMessage.current
    var fileName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var downloadName by remember { mutableStateOf("") }
    var downloadResult by remember { mutableStateOf<String?>(null) }
    var files by remember { mutableStateOf(vault.list()) }

    Spacer(Modifier.height(20.dp))
    MoaText("게시판 첨부파일", size = 16.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    FieldLabel("파일명")
    MoaTextField(fileName, { fileName = it }, placeholder = "예: memo.txt / payload.html")
    Spacer(Modifier.height(12.dp))
    FieldLabel("내용")
    MoaTextField(content, { content = it }, placeholder = "파일 내용")
    Spacer(Modifier.height(10.dp))
    PrimaryButton("업로드", onClick = {
        val r = vault.upload(fileName, content.toByteArray())
        showMessage(r.message)
        if (r.ok) { files = vault.list(); fileName = ""; content = "" }
    })
    Spacer(Modifier.height(8.dp))
    MoaText(
        "실습: 안전 모드는 이미지/pdf/txt만 허용해요. 취약 모드에선 payload.html 같은 실행형도 올라가요.",
        size = 11.sp, color = MoaTextTertiary, lineHeight = 16.sp,
    )

    if (files.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        WhiteCard {
            MoaText("업로드된 파일", size = 13.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            files.forEach { MoaText("· $it", size = 13.sp, color = MoaTextSecondary) }
        }
    }

    Spacer(Modifier.height(20.dp))
    FieldLabel("다운로드 (파일명)")
    MoaTextField(downloadName, { downloadName = it }, placeholder = "예: memo.txt")
    Spacer(Modifier.height(10.dp))
    PrimaryButton("다운로드", onClick = {
        downloadResult = vault.download(downloadName).fold(
            onSuccess = { bytes -> "읽기 성공 (${bytes.size} bytes):\n" + bytes.decodeToString().take(400) },
            onFailure = { "차단/실패: ${it.message}" },
        )
    })
    Spacer(Modifier.height(8.dp))
    MoaText(
        "실습: 다운로드 파일명에  ../databases/moa_lab.db  를 넣어 샌드박스 내부 파일이 새는지 확인해 보세요.",
        size = 11.sp, color = MoaTextTertiary, lineHeight = 16.sp,
    )
    downloadResult?.let {
        Spacer(Modifier.height(12.dp))
        WhiteCard {
            MoaText(it, size = 12.sp, color = MoaTextSecondary, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ColumnScope.SupportTab() {
    val showMessage = LocalShowMessage.current
    val inquiries = BoardRepository.inquiries
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Spacer(Modifier.height(20.dp))
    MoaText("고객센터 문의", size = 16.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    MoaText("1588-0000 · 평일 09:00~18:00", size = 12.sp, color = MoaTextTertiary)
    Spacer(Modifier.height(16.dp))
    FieldLabel("제목")
    MoaTextField(title, { title = it }, placeholder = "문의 제목")
    Spacer(Modifier.height(12.dp))
    FieldLabel("내용")
    MoaTextField(body, { body = it }, placeholder = "문의 내용")
    Spacer(Modifier.height(10.dp))
    PrimaryButton("문의 등록", enabled = title.isNotBlank() && body.isNotBlank(), onClick = {
        BoardRepository.addInquiry(title.trim(), body.trim())
        title = ""; body = ""
        showMessage("문의가 등록됐어요")
    })

    if (inquiries.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        MoaText("내 문의", size = 14.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        inquiries.forEach { q ->
            WhiteCard {
                MoaText(q.title, size = 14.sp, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                MoaText(q.body, size = 12.sp, color = MoaTextSecondary)
                Spacer(Modifier.height(6.dp))
                MoaText(if (q.answered) "답변 완료" else "접수됨", size = 11.sp, color = MoaBlue)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
