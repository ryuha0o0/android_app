package com.moa.finance.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 자동로그인 자격증명/기기 UUID 저장소.
 *
 *  - VULN (PLAINTEXT_AUTOLOGIN ON) : 일반 SharedPreferences 평문 저장.
 *      → 루팅 기기에서 /data/data/<pkg>/shared_prefs/*.xml 를 그대로 읽어 토큰 탈취 가능.
 *      점검항목: D-26(중요정보 평문 저장), D-27, A-14, A-16 / E-F14,F16
 *  - SECURE : EncryptedSharedPreferences(AES256-GCM) + Android Keystore 마스터키.
 *
 * 저장하는 값은 실제 금융정보가 아니라 실습용 가짜 토큰/식별자입니다.
 */
class SecureStore(private val context: Context) {

    private val plainPrefs: SharedPreferences =
        context.getSharedPreferences("auth_plain", Context.MODE_PRIVATE)

    private val encPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "auth_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun prefs(): SharedPreferences =
        if (VulnConfig.isVulnerable(VulnConfig.Flag.PLAINTEXT_AUTOLOGIN)) plainPrefs else encPrefs

    fun putString(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
    }

    fun getString(key: String): String? = prefs().getString(key, null)

    fun clear() {
        plainPrefs.edit().clear().apply()
        runCatching { encPrefs.edit().clear().apply() }
    }

    companion object {
        const val KEY_AUTOLOGIN_TOKEN = "autologin_token"
        const val KEY_DEVICE_UUID = "device_uuid"
        const val KEY_USER_ID = "user_id"
    }
}
