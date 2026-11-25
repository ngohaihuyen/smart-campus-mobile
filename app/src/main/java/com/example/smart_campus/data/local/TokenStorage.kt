package com.example.smart_campus.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.smart_campus.model.Role
import com.example.smart_campus.model.User
import com.example.smart_campus.model.dto.AuthResponse
import java.time.Instant

/** lưu JWT + thông tin user, mã hóa bằng Android Keystore. Chỉ làm việc đọc/ghi, không có logic phiên. */
class TokenStorage(context: Context) {

    private val prefs: SharedPreferences = createPrefs(context.applicationContext)

    fun save(auth: AuthResponse, nowMillis: Long = System.currentTimeMillis()) {
        prefs.edit {
            putString(KEY_TOKEN, auth.accessToken)
            putLong(KEY_EXPIRES_AT, nowMillis + auth.expiresIn * 1000)
            putString(KEY_USER_ID, auth.user.userId)
            putString(KEY_FULL_NAME, auth.user.fullName)
            putString(KEY_EMAIL, auth.user.email)
            putString(KEY_ROLE, auth.user.role)
            putString(KEY_CREATED_AT, auth.user.createdAt)
        }
    }

    fun accessToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        nowMillis >= prefs.getLong(KEY_EXPIRES_AT, 0L)

    /** trả null nếu thiếu field hoặc createdAt không parse được -> coi như chưa đăng nhập. */
    fun readUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_FULL_NAME, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val createdAt = prefs.getString(KEY_CREATED_AT, null)
            ?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return null
        return User(id, name, email, Role.from(prefs.getString(KEY_ROLE, null)), createdAt)
    }

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val FILE = "smart_campus_session"
        const val KEY_TOKEN = "access_token"
        const val KEY_EXPIRES_AT = "expires_at_millis"
        const val KEY_USER_ID = "user_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_CREATED_AT = "created_at"

        fun createPrefs(context: Context): SharedPreferences {
            fun build(): SharedPreferences {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                return EncryptedSharedPreferences.create(
                    context, FILE, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            // Keystore hỏng (đổi máy, restore backup...) -> xóa file cũ và tạo lại, user chỉ cần đăng nhập lại
            return try {
                build()
            } catch (e: Exception) {
                context.deleteSharedPreferences(FILE)
                build()
            }
        }
    }
}