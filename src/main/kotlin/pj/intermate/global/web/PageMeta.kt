package pj.intermate.global.web

sealed class PageMeta() {

    data class Scroll(
        val size: Int,
        val hasNext: Boolean,
    ) : PageMeta()

    data class Number(
        val page: Int,
        val size: Int,
        val hasNext: Boolean,
        val totalPages: Int,
        val totalElements: Long,
    ) : PageMeta()
}