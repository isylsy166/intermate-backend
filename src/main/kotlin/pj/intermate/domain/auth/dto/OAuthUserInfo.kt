package pj.intermate.domain.auth.dto

/**
 * provider 마다 응답 모양이 달라서, 공통 형태로 변환해 담는다.
 *
 * @property providerId provider 안에서 유일한 사용자 식별자 (google: sub, kakao: id)
 */
data class OAuthUserInfo(
    val providerId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
)
