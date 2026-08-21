package pj.intermate.domain.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pj.intermate.entity.Provider
import pj.intermate.entity.UserEntity

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun create() {

    }

    /**
     * 소셜 로그인 사용자를 조회하고, 없으면 가입시킨다.
     *
     * (provider, provider_id) 가 신원의 기준이다. 이메일은 바뀔 수 있어서 식별자로 쓰지 않는다.
     */
    @Transactional
    fun findOrCreateSocialUser(
        provider: Provider,
        providerId: String,
        email: String,
        name: String,
        profileImageUrl: String?,
    ): UserEntity {
        userRepository.findByProviderAndProviderId(provider, providerId)
            ?.let { return it }

        // users.email 에 unique 제약이 있어, 다른 경로로 이미 가입한 이메일이면 저장이 실패한다.
        // 계정 연동 정책이 정해지기 전까지는 원인을 알 수 있는 메시지로 막아둔다.
        userRepository.findByEmail(email)?.let { existing ->
            throw IllegalStateException("이미 ${existing.provider} 로 가입된 이메일입니다: $email")
        }

        return userRepository.save(
            UserEntity(
                name = name,
                email = email,
                provider = provider,
                providerId = providerId,
                profileImageUrl = profileImageUrl,
            )
        )
    }
}
