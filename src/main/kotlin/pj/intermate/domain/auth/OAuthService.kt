package pj.intermate.domain.auth

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import pj.intermate.client.oauth.OAuthClient
import pj.intermate.domain.user.UserService
import pj.intermate.entity.Provider
import pj.intermate.global.config.OAuthProperties
import pj.intermate.global.jwt.JwtProvider
import java.net.URI

@Service
class OAuthService(
    private val oAuthProperties: OAuthProperties,
    private val oAuthClient: OAuthClient,
    private val oAuthStateStore: OAuthStateStore,
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
) {

    /**
     * 소셜 로그인 시작 URL 을 만든다.
     * CSRF 방어용 state 를 발급해 세션에 심어두고, callback 에서 같은 값인지 대조한다.
     */
    fun buildAuthorizationUri(providerName: String, session: HttpSession): URI {
        val provider = parseProvider(providerName)
        val client = oAuthProperties.clientOf(provider)
        require(client.configured) { "$provider OAuth 설정이 비어 있습니다." }

        val state = oAuthStateStore.issue(provider, session)

        return UriComponentsBuilder.fromUriString(client.authorizationUri)
            .queryParam("response_type", "code")
            .queryParam("client_id", client.clientId)
            .queryParam("redirect_uri", client.redirectUri)
            .queryParam("scope", client.scope)
            .queryParam("state", state)
            .encode()
            .build()
            .toUri()
    }

    /**
     * 소셜 서버가 되돌려보낸 callback 을 처리해 우리 서비스의 access token 을 만든다.
     *
     * 1. 세션에 심어둔 state 와 대조 (CSRF 방어)
     * 2. authorization code → 소셜 access token 교환
     * 3. 소셜 access token → 사용자 프로필 조회
     * 4. 조회/가입 후 JWT 발급
     */
    fun socialLogin(providerName: String, code: String, state: String?, session: HttpSession): String {
        val provider = parseProvider(providerName)
        val client = oAuthProperties.clientOf(provider)
        require(client.configured) { "$provider OAuth 설정이 비어 있습니다." }

        oAuthStateStore.verifyAndConsume(provider, state, session)

        val socialAccessToken = oAuthClient.exchangeCodeForAccessToken(client, code)
        val userInfo = oAuthClient.fetchUserInfo(provider, client, socialAccessToken)

        val user = userService.findOrCreateSocialUser(
            provider = provider,
            providerId = userInfo.providerId,
            email = userInfo.email,
            name = userInfo.name,
            profileImageUrl = userInfo.profileImageUrl,
        )

        return jwtProvider.createAccessToken(
            userId = requireNotNull(user.id) { "저장된 사용자에 id 가 없습니다." },
            email = user.email,
        )
    }

    private fun parseProvider(providerName: String): Provider {
        val provider = runCatching { Provider.valueOf(providerName.uppercase()) }
            .getOrElse { throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $providerName") }

        require(provider != Provider.LOCAL) { "LOCAL 은 소셜 로그인 대상이 아닙니다." }
        return provider
    }
}
