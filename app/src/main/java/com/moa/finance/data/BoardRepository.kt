package com.moa.finance.data

import androidx.compose.runtime.mutableStateListOf

/**
 * 메인창(커뮤니티) 데이터 — 공지사항·게시판·고객센터 문의.
 * 모든 입력은 사용자 제공 값이며, WebView 렌더링 시 XSS 실습의 입력원이 됩니다.
 */
object BoardRepository {

    data class Notice(val id: Int, val title: String, val body: String, val date: String)
    data class Post(val id: Int, val author: String, val title: String, val body: String, val attachment: String?)
    data class Inquiry(val id: Int, val title: String, val body: String, val answered: Boolean)

    val notices = mutableStateListOf(
        Notice(1, "서비스 점검 안내", "10월 30일 02:00~04:00 정기 점검이 있어요.", "2026.10.20"),
        Notice(2, "이체 수수료 무료 이벤트", "연말까지 타행 이체 수수료가 무료예요.", "2026.10.15"),
    )

    val posts = mutableStateListOf(
        Post(1, "김하늘", "적금 추천 부탁해요", "차곡차곡 적금 쓰는 분 계신가요?", null),
    )

    val inquiries = mutableStateListOf<Inquiry>()

    private var postSeq = 2
    private var inquirySeq = 1

    fun addPost(author: String, title: String, body: String, attachment: String?) {
        posts.add(0, Post(postSeq++, author, title, body, attachment))
    }

    fun addInquiry(title: String, body: String) {
        inquiries.add(0, Inquiry(inquirySeq++, title, body, answered = false))
    }
}
