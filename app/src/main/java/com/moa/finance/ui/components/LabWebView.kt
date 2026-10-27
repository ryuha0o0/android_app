package com.moa.finance.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.moa.finance.security.VulnConfig

/**
 * 실습용 WebView. 생성된 HTML을 렌더링하고, 안에서 실행된 스크립트가
 * LabBridge 를 통해 Compose 로 신호를 보냅니다(XSS/CSRF 실행 탐지).
 *
 * WebView 보안 토글 (D-42,58 / B-06 / C-03):
 *  - WEBVIEW_XSS ON  : JS 실행 + 파일 접근 허용(위험 설정)
 *  - OFF             : 파일 접근 차단 등 보수적 설정
 * (입력값 escape 여부는 HTML 생성 단계에서 LabHtml 가 처리)
 */
class LabBridge(
    private val onReport: (tag: String, msg: String) -> Unit,
    private val onAction: (name: String, token: String) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun report(tag: String, msg: String) { main.post { onReport(tag, msg) } }

    @JavascriptInterface
    fun action(name: String, token: String) { main.post { onAction(name, token) } }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LabWebView(html: String, bridge: LabBridge, modifier: Modifier = Modifier) {
    val vulnWebView = VulnConfig.isVulnerable(VulnConfig.Flag.WEBVIEW_XSS)
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true // 브리지/실습 신호 수신용
                settings.allowFileAccess = vulnWebView
                settings.allowContentAccess = vulnWebView
                @Suppress("DEPRECATION")
                settings.allowFileAccessFromFileURLs = vulnWebView
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = vulnWebView
                addJavascriptInterface(bridge, "LabBridge")
            }
        },
        update = { webView ->
            webView.settings.allowFileAccess = vulnWebView
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        },
    )
}

/** HTML 생성기 — WEBVIEW_XSS 플래그에 따라 사용자 입력을 그대로/escape 해서 넣습니다. */
object LabHtml {
    fun escape(raw: String): String = raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    /** 사용자 입력을 상황에 맞게 넣음. 취약 모드면 원문, 안전 모드면 escape. */
    fun field(raw: String): String =
        if (VulnConfig.isVulnerable(VulnConfig.Flag.WEBVIEW_XSS)) raw else escape(raw)

    fun page(bodyHtml: String): String =
        """
        <!doctype html><html lang="ko"><head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
          body{font-family:-apple-system,Roboto,sans-serif;color:#1A2232;margin:16px;font-size:15px;line-height:1.6}
          h3{font-size:17px;margin:0 0 12px}
          .card{background:#F3F5F9;border-radius:12px;padding:14px;margin:10px 0}
          .muted{color:#6B7280;font-size:13px}
          a{color:#2E6BE6;text-decoration:none}
        </style></head><body>
        $bodyHtml
        </body></html>
        """.trimIndent()
}
