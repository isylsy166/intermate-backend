package pj.intermate.domain.user.dto

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val profileImageUrl: String? = null,
)
