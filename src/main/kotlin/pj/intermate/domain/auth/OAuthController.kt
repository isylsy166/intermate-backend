package pj.intermate.domain.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/oauth")
@Tag(name = "인증")
class OAuthController(
    private val oAuthService: OAuthService,
    private val loginRedirect: LoginRedirectUriFactory,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/{provider}/authorize")
    @Operation(
        summary = "소셜 로그인 시작",
        description = "state 를 발급해 세션에 저장하고 소셜 로그인 동의 화면으로 302 리다이렉트한다.",
    )
    fun authorize(
        @PathVariable provider: String,
        session: HttpSession,
    ): ResponseEntity<Void> = redirect(oAuthService.buildAuthorizationUri(provider, session))

    @GetMapping("/{provider}/callback")
    @Operation(
        summary = "소셜 로그인 콜백",
        description = """
            소셜 서버가 authorization code 를 붙여 브라우저를 되돌려보내는 지점.
            code 를 토큰으로 교환해 사용자를 조회/가입시키고, JWT 를 붙여 프론트로 302 리다이렉트한다.
            브라우저가 직접 여는 주소라 실패해도 에러 응답 대신 실패 페이지로 보낸다.
        """,
    )
    fun callback(
        @PathVariable provider: String,
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) error: String?,
        session: HttpSession,
    ): ResponseEntity<Void> {
        // 사용자가 동의 화면에서 취소하면 code 대신 error 가 온다.
        require(error == null) { "소셜 로그인이 취소되었습니다. ($error)" }
        require(!code.isNullOrBlank()) { "authorization code 가 없습니다." }

        val accessToken = oAuthService.socialLogin(provider, code, state, session)

        return redirect(loginRedirect.success(accessToken))
    }

    /**
     * 이 컨트롤러의 엔드포인트는 브라우저가 주소창으로 직접 여는 곳이라
     * 에러 응답(JSON) 대신 프론트의 실패 페이지로 302 리다이렉트한다.
     *
     * 사용자에게 보여줄 의도로 우리가 던진 IllegalArgumentException 만 메시지를 그대로 노출한다.
     * 그 외 예외는 내부 구조가 쿼리 파라미터로 새어나갈 수 있어 일반 문구로 덮는다.
     */
    @ExceptionHandler(Exception::class)
    fun handleLoginFailure(e: Exception, request: HttpServletRequest): ResponseEntity<Void> {
        log.warn("소셜 로그인 실패: {}", request.requestURI, e)

        val message = (e as? IllegalArgumentException)?.message ?: "로그인에 실패했습니다."

        return redirect(loginRedirect.failure(message))
    }

    private fun redirect(location: URI): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.FOUND)
            .location(location)
            .build()
}
