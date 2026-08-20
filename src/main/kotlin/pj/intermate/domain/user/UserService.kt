package pj.intermate.domain.user

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun create() {

    }
}