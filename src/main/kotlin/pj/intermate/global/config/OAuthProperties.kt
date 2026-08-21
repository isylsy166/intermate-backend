package pj.intermate.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import pj.intermate.entity.Provider

@ConfigurationProperties(prefix = "oauth")
data class OAuthProperties(
    val google: Client,
    val kakao: Client,
) {

    data class Client(
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String,
        val scope: String,
        val authorizationUri: String,
        val tokenUri: String,
        val userInfoUri: String,
    ) {
        // client-id 가 비어 있으면 아직 발급받지 않은 provider 로 본다
        val configured: Boolean
            get() = clientId.isNotBlank()
    }

    fun clientOf(provider: Provider): Client = when (provider) {
        Provider.GOOGLE -> google
        Provider.KAKAO -> kakao
        else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $provider")
    }
}
