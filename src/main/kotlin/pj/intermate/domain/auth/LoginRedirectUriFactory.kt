package pj.intermate.domain.auth

import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import pj.intermate.global.config.FrontendProperties
import java.net.URI

/**
 * 로그인 처리 결과를 프론트로 되돌려보낼 리다이렉트 주소를 만든다.
 *
 * "성공하면 token, 실패하면 message 쿼리 파라미터로 넘긴다" 는 프론트와의 약속을 여기 한 곳에 모아둔다.
 * 컨트롤러/필터 등 여러 곳에서 같은 주소가 필요해질 때 UriComponentsBuilder 체인이 복붙되는 것을 막는다.
 */
@Component
class LoginRedirectUriFactory(
    private val frontendProperties: FrontendProperties,
) {

    fun success(accessToken: String): URI =
        build(frontendProperties.loginSuccessUri, "token", accessToken)

    fun failure(message: String): URI =
        build(frontendProperties.loginFailureUri, "message", message)

    /** 토큰과 메시지 모두 URL 에 그대로 넣을 수 없는 문자를 포함할 수 있어 encode() 는 필수다. */
    private fun build(uri: String, key: String, value: String): URI =
        UriComponentsBuilder.fromUriString(uri)
            .queryParam(key, value)
            .encode()
            .build()
            .toUri()
}