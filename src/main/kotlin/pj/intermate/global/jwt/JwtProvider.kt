package pj.intermate.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import pj.intermate.global.config.JwtProperties
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {

    private val key: SecretKey = run {
        val bytes = jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size >= MIN_SECRET_BYTE_LENGTH) {
            "jwt.secret 은 최소 ${MIN_SECRET_BYTE_LENGTH}바이트 이상이어야 합니다. (현재 ${bytes.size}바이트)"
        }
        Keys.hmacShaKeyFor(bytes)
    }

    /**
     * 로그인에 성공한 사용자에게 발급할 access token 을 만든다.
     * subject 에는 users.id 가 들어가고, 이후 요청에서 이 값으로 사용자를 식별한다.
     */
    fun createAccessToken(userId: Long, email: String): String {
        val now = Date()

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.accessTokenValidity))
            .signWith(key)
            .compact()
    }

    /**
     * 서명과 만료를 검증하고 claim 을 꺼낸다. 유효하지 않으면 JwtException 이 던져진다.
     */
    fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private const val MIN_SECRET_BYTE_LENGTH = 32
    }
}
