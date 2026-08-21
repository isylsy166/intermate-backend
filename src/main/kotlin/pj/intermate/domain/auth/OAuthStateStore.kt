package pj.intermate.domain.auth

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component
import pj.intermate.entity.Provider
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * CSRF 방어용 state 를 발급하고 검증한다.
 *
 * 세션에 담는 이유는 "유효한 state 인가" 가 아니라 "이 브라우저가 발급받은 state 인가" 를 물어야 하기 때문이다.
 * 전역 저장소에 담으면 공격자가 자기 계정으로 받아둔 state/code 로 남의 브라우저를 로그인시킬 수 있다.
 */
@Component
class OAuthStateStore {

    private val secureRandom = SecureRandom()

    /**
     * state 를 발급해 세션에 심는다.
     * 같은 키에 덮어쓰기: 재호출 시 기존 세션의 state 는 무효화 된다.
     */
    fun issue(provider: Provider, session: HttpSession): String {
        val state = generateState()
        session.setAttribute(stateSessionKey(provider), state)
        return state
    }

    /**
     * authorize 단계에서 세션에 심어둔 state 와 같은 값인지 확인한다.
     * 성공/실패와 무관하게 1회용으로 지워, 같은 code 를 재전송하는 공격을 막는다.
     */
    fun verifyAndConsume(provider: Provider, state: String?, session: HttpSession) {
        val key = stateSessionKey(provider)
        val saved = session.getAttribute(key) as? String
        session.removeAttribute(key)

        require(saved != null) { "로그인 세션이 만료되었습니다. 처음부터 다시 시도해 주세요." }
        require(!state.isNullOrBlank() && constantTimeEquals(saved, state)) { "유효하지 않은 state 입니다." }
    }

    /**
     * state 생성
     *
     * - 임의의 랜덤 문자열을 생성한다.
     * - ex) `xK9mP2vL8qR4tY7wZ3nB6hJ1sD5fG0aC`
     * */
    private fun generateState(): String {
        val bytes = ByteArray(STATE_BYTE_LENGTH)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** 타이밍 공격을 피하려고 앞부분이 다르다고 조기 종료하지 않는 비교를 쓴다. */
    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(StandardCharsets.UTF_8), b.toByteArray(StandardCharsets.UTF_8))

    companion object {
        private const val STATE_BYTE_LENGTH = 32

        private fun stateSessionKey(provider: Provider) = "OAUTH_STATE_$provider"
    }
}