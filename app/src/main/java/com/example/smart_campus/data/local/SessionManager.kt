package com.example.smart_campus.data.local

import com.example.smart_campus.model.User
import com.example.smart_campus.model.dto.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SessionState {
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

/** nguồn duy nhất về phiên đăng nhập. UI quan sát [state]; AuthInterceptor dùng [accessToken] và [onUnauthorized]. */
class SessionManager(private val storage: TokenStorage) {

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /** Gọi sau khi login thành công. */
    fun saveLogin(auth: AuthResponse) {
        storage.save(auth)
        _state.value = storage.readUser()?.let { SessionState.LoggedIn(it) } ?: SessionState.LoggedOut
    }

    /** Đồng bộ, an toàn gọi từ luồng OkHttp. */
    fun accessToken(): String? = storage.accessToken()

    fun logout() {
        storage.clear()
        _state.value = SessionState.LoggedOut
    }

    /** AuthInterceptor gọi khi một API (trừ login) trả 401. */
    fun onUnauthorized() = logout()

    // mở app: token còn hạn mới coi là đã đăng nhập. Server vẫn là nơi quyết định cuối cùng (có thể 401)
    private fun restore(): SessionState {
        val user = storage.readUser()
        return if (storage.accessToken() != null && !storage.isExpired() && user != null) {
            SessionState.LoggedIn(user)
        } else {
            storage.clear()
            SessionState.LoggedOut
        }
    }
}