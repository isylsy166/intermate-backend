package pj.intermate.client.oauth

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import pj.intermate.domain.auth.dto.OAuthUserInfo
import pj.intermate.entity.Provider
import pj.intermate.global.config.OAuthProperties

/**
 * 소셜 서버와 직접 통신하는 계층. 브라우저를 거치지 않고 서버에서 호출한다.
 */
@Component
class OAuthClient {

    private val restClient: RestClient = RestClient.create()

    /**
     * callback 으로 받은 authorization code 를 access token 으로 교환한다.
     * code 는 1회용이라 재사용하면 invalid_grant 가 떨어진다.
     */
    fun exchangeCodeForAccessToken(client: OAuthProperties.Client, code: String): String {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", client.clientId)
            add("client_secret", client.clientSecret)
            // 인가 요청 때 보낸 값과 정확히 같아야 한다. 다르면 redirect_uri_mismatch.
            add("redirect_uri", client.redirectUri)
            add("code", code)
        }

        val response = restClient.post()
            .uri(client.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(form)
            .retrieve()
            .onStatus({ it.isError }) { _, res -> fail("토큰 교환", res) }
            .body(MAP_TYPE)
            ?: throw IllegalStateException("토큰 응답이 비어 있습니다.")

        return response["access_token"] as? String
            ?: throw IllegalStateException("토큰 응답에 access_token 이 없습니다: ${response.keys}")
    }

    /**
     * access token 으로 사용자 프로필을 조회한다.
     */
    fun fetchUserInfo(provider: Provider, client: OAuthProperties.Client, accessToken: String): OAuthUserInfo {
        val response = restClient.get()
            .uri(client.userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { _, res -> fail("사용자 정보 조회", res) }
            .body(MAP_TYPE)
            ?: throw IllegalStateException("사용자 정보 응답이 비어 있습니다.")

        return when (provider) {
            Provider.GOOGLE -> parseGoogle(response)
            Provider.KAKAO -> parseKakao(response)
            else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $provider")
        }
    }

    /** google: `{ "sub": "...", "email": "...", "name": "...", "picture": "..." }` */
    private fun parseGoogle(body: Map<String, Any?>): OAuthUserInfo {
        val providerId = body["sub"] as? String
            ?: throw IllegalStateException("google 응답에 sub 가 없습니다.")
        val email = body["email"] as? String
            ?: throw IllegalStateException("google 계정 이메일을 가져오지 못했습니다.")

        return OAuthUserInfo(
            providerId = providerId,
            email = email,
            name = body["name"] as? String ?: email.substringBefore('@'),
            profileImageUrl = body["picture"] as? String,
        )
    }

    /** kakao: `{ "id": 123, "kakao_account": { "email": "...", "profile": { "nickname": "...", "profile_image_url": "..." } } }` */
    private fun parseKakao(body: Map<String, Any?>): OAuthUserInfo {
        val providerId = body["id"]?.toString()
            ?: throw IllegalStateException("kakao 응답에 id 가 없습니다.")

        @Suppress("UNCHECKED_CAST")
        val account = body["kakao_account"] as? Map<String, Any?>

        @Suppress("UNCHECKED_CAST")
        val profile = account?.get("profile") as? Map<String, Any?>

        // 이메일은 선택 동의 항목이라 사용자가 거부하면 내려오지 않는다.
        val email = account?.get("email") as? String
            ?: throw IllegalStateException("kakao 계정 이메일 제공에 동의해야 로그인할 수 있습니다.")

        return OAuthUserInfo(
            providerId = providerId,
            email = email,
            name = profile?.get("nickname") as? String ?: email.substringBefore('@'),
            profileImageUrl = profile?.get("profile_image_url") as? String,
        )
    }

    /** 소셜 서버가 내려준 에러 본문을 그대로 붙여서 원인을 바로 볼 수 있게 한다. */
    private fun fail(step: String, response: ClientHttpResponse): Nothing {
        val body = runCatching { response.body.readBytes().decodeToString() }.getOrDefault("")
        throw IllegalStateException("${step}에 실패했습니다: ${response.statusCode} $body")
    }

    companion object {
        private val MAP_TYPE = object : ParameterizedTypeReference<Map<String, Any?>>() {}
    }
}