package com.moa.finance.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.moa.finance.data.Transaction
import com.moa.finance.security.VulnConfig

/**
 * 로컬 SQLite 저장소 — 거래내역 조회의 SQL 인젝션 실습용 (조회 API24).
 *
 * 점검항목: D-19(SQL 인젝션), B-02, C-01 / E-S01, 그리고 날짜 파라미터 변조(D-04,21).
 *
 *  - VULN (SQLI_TRANSACTION ON) : 사용자가 입력한 날짜/계정 ID를 문자열 결합으로 질의.
 *      예) 거래내역 날짜 필드에  `2026-10-01' OR '1'='1`  입력 → 전체 행 노출,
 *          `... UNION SELECT ...` 로 다른 테이블 값 추출 실습.
 *  - SECURE : selectionArgs 바인딩(prepared statement)으로 질의 → 인젝션 불가.
 *
 * 저장 데이터는 디자인의 샘플 값이며 실제 금융정보가 아닙니다. 네트워크로 나가지 않습니다.
 */
class MoaDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                id INTEGER PRIMARY KEY,
                account_id TEXT NOT NULL,
                date_label TEXT NOT NULL,
                occurred_on TEXT NOT NULL,   -- yyyy-MM-dd
                time TEXT NOT NULL,
                title TEXT NOT NULL,
                method TEXT NOT NULL,
                amount INTEGER NOT NULL,
                balance_after INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    private fun seed(db: SQLiteDatabase) {
        val rows = listOf(
            // id, account, dateLabel, occurredOn, time, title, method, amount, balanceAfter
            arrayOf(4, "living", "10월 24일 토요일", "2026-10-24", "09:32", "김하늘", "계좌이체", -50_000, 3_245_800),
            arrayOf(3, "living", "10월 23일 금요일", "2026-10-23", "10:00", "모아컴퍼니", "급여", 2_800_000, 3_295_800),
            arrayOf(2, "living", "10월 23일 금요일", "2026-10-23", "08:45", "오늘커피", "체크카드", -4_500, 495_800),
            arrayOf(1, "living", "10월 22일 목요일", "2026-10-22", "18:20", "모아마트", "체크카드", -32_700, 500_300),
            arrayOf(7, "saving", "10월 10일 금요일", "2026-10-10", "08:10", "생활비 통장", "계좌이체", 300_000, 8_500_000),
            arrayOf(6, "saving", "09월 25일 목요일", "2026-09-25", "12:00", "이자", "이자", 12_300, 8_200_000),
        )
        rows.forEach { r ->
            db.execSQL(
                "INSERT INTO $TABLE (id,account_id,date_label,occurred_on,time,title,method,amount,balance_after) VALUES (?,?,?,?,?,?,?,?,?)",
                r,
            )
        }
    }

    /**
     * 거래내역 날짜 범위 조회.
     * @param accountId 선택된 계좌
     * @param from,to   화면에서 입력받은 조회 시작/종료일 (yyyy-MM-dd) — 공격 표면
     */
    fun searchTransactions(accountId: String, from: String, to: String): List<Transaction> {
        val db = readableDatabase
        val cursor = if (VulnConfig.isVulnerable(VulnConfig.Flag.SQLI_TRANSACTION)) {
            // ── 취약: 입력값을 그대로 SQL 문자열에 결합 ──────────────────────
            val sql =
                "SELECT id,account_id,date_label,time,title,method,amount,balance_after FROM $TABLE " +
                    "WHERE account_id = '$accountId' " +
                    "AND occurred_on >= '$from' AND occurred_on <= '$to' " +
                    "ORDER BY id DESC"
            db.rawQuery(sql, null)
        } else {
            // ── 안전: 파라미터 바인딩 ────────────────────────────────────────
            db.rawQuery(
                "SELECT id,account_id,date_label,time,title,method,amount,balance_after FROM $TABLE " +
                    "WHERE account_id = ? AND occurred_on >= ? AND occurred_on <= ? ORDER BY id DESC",
                arrayOf(accountId, from, to),
            )
        }
        return cursor.use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Transaction(
                            id = c.getLong(0),
                            accountId = c.getString(1),
                            dateLabel = c.getString(2),
                            time = c.getString(3),
                            title = c.getString(4),
                            method = c.getString(5),
                            amount = c.getLong(6),
                            balanceAfter = c.getLong(7),
                        ),
                    )
                }
            }
        }
    }

    companion object {
        private const val DB_NAME = "moa_lab.db"
        private const val DB_VERSION = 1
        private const val TABLE = "transactions"

        @Volatile
        private var instance: MoaDbHelper? = null

        fun get(context: Context): MoaDbHelper =
            instance ?: synchronized(this) {
                instance ?: MoaDbHelper(context).also { instance = it }
            }
    }
}
