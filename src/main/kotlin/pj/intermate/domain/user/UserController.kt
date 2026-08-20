package pj.intermate.domain.user

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pj.intermate.domain.user.dto.CreateUserRequest
import pj.intermate.global.web.wrapData

@RestController()
@RequestMapping("/user")
@Tag(name = "사용자")
class UserController(
    private val userService: UserService
) {

    @PostMapping()
    fun create(
        @RequestBody request: CreateUserRequest
    ) = userService.create().wrapData()

}