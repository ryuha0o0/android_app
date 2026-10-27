package com.moa.finance.security

import android.content.Context
import java.io.File

/**
 * 게시판 첨부파일 저장/다운로드 — 파일 업로드 검증 + 경로추적(Path Traversal) 실습.
 *
 * 점검항목: 업로드 D-20 / B-05,13,  다운로드·경로추적 D-28 / B-14 / C-02 / E-S02.
 *
 *  - UNSAFE_FILE_UPLOAD ON : 확장자/타입 검증 없이 저장 (html·js·sh 등 실행형 허용).
 *  - PATH_TRAVERSAL ON     : 다운로드 파일명을 정규화 없이 baseDir 에 결합 →
 *        `../../databases/moa_lab.db` 같은 입력으로 앱 내부 다른 파일 탈취 실습.
 *  - 둘 다 OFF             : 화이트리스트 확장자만 허용 + canonicalPath 가 baseDir 하위인지 검증.
 *
 * 모두 앱 샌드박스 내부 파일에 한정된 로컬 실습입니다.
 */
class FileVault(context: Context) {

    private val baseDir: File = File(context.filesDir, "board").apply { mkdirs() }

    private val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "pdf", "txt")

    data class SaveResult(val ok: Boolean, val message: String, val storedName: String? = null)

    fun upload(fileName: String, content: ByteArray): SaveResult {
        val name = fileName.trim()
        if (!VulnConfig.isVulnerable(VulnConfig.Flag.UNSAFE_FILE_UPLOAD)) {
            val ext = name.substringAfterLast('.', "").lowercase()
            if (ext !in allowedExtensions) {
                return SaveResult(false, "허용되지 않은 형식이에요(.$ext). 이미지/pdf/txt만 올릴 수 있어요.")
            }
            if (name.contains('/') || name.contains('\\') || name.contains("..")) {
                return SaveResult(false, "파일명에 경로 문자를 쓸 수 없어요.")
            }
        }
        val target = resolve(name)
        return runCatching {
            target.parentFile?.mkdirs()
            target.writeBytes(content)
            SaveResult(true, "업로드됐어요", target.name)
        }.getOrElse { SaveResult(false, "저장 실패: ${it.message}") }
    }

    /** 파일명으로 내용 읽기. 경로추적 취약/안전 분기. 내부 바이너리는 길이만 노출하지 않도록 텍스트로 반환. */
    fun download(fileName: String): Result<ByteArray> {
        val target = resolve(fileName)
        if (!VulnConfig.isVulnerable(VulnConfig.Flag.PATH_TRAVERSAL)) {
            val canonicalBase = baseDir.canonicalPath
            if (!target.canonicalPath.startsWith(canonicalBase + File.separator) &&
                target.canonicalPath != canonicalBase
            ) {
                return Result.failure(SecurityException("허용된 디렉터리를 벗어난 경로예요."))
            }
        }
        return runCatching {
            require(target.exists() && target.isFile) { "파일이 없어요: $fileName" }
            target.readBytes()
        }
    }

    private fun resolve(fileName: String): File =
        if (VulnConfig.isVulnerable(VulnConfig.Flag.PATH_TRAVERSAL)) {
            // 취약: 정규화 없이 그대로 결합 → .. 로 상위 탈출 가능
            File(baseDir.path + File.separator + fileName)
        } else {
            File(baseDir, File(fileName).name) // 경로 요소 제거
        }

    fun list(): List<String> =
        baseDir.listFiles()?.filter { it.isFile }?.map { it.name }?.sorted() ?: emptyList()
}
