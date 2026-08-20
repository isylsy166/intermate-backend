package pj.intermate.global.web

import org.springframework.data.domain.Page
import org.springframework.data.domain.Slice

class ApiResponse<T> (
    val data: T,
    val meta: PageMeta? = null,
)

// 기본 응답
fun <T> T.wrapData() = ApiResponse(this)

// 숫자 기반 페이지네이션
fun <T : Any> Page<T>.wrapDataPageNumber() = ApiResponse(
    this.content,
    PageMeta.Number(
        page = this.number,
        size = this.size,
        hasNext = this.hasNext(),
        totalPages = this.totalPages,
        totalElements = this.totalElements,
    )
)

// 무한스크롤 페이지네이션
fun <T : Any> Slice<T>.wrapDataPageScroll() = ApiResponse(
    this.content,
    PageMeta.Scroll(
        size = this.size,
        hasNext = this.hasNext(),
    )
)
