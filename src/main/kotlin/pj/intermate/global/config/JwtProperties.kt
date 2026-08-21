package pj.intermate.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @property secret HS256 서명 키. 최소 32 bytes 이상이어야 한다.
 * @property accessTokenValidity access token 유효시간 (ms)
 * @property refreshTokenValidity refresh token 유효시간 (ms). 저장소가 아직 없어 발급하지 않는다.
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenValidity: Long,
    val refreshTokenValidity: Long,
)