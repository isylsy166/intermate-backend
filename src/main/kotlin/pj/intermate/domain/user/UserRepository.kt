package pj.intermate.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import pj.intermate.entity.Provider
import pj.intermate.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByProviderAndProviderId(provider: Provider, providerId: String): UserEntity?

    fun findByEmail(email: String): UserEntity?
}
