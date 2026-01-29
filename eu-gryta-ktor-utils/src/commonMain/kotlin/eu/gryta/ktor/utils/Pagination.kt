package eu.gryta.ktor.utils

import io.ktor.client.request.HttpRequestBuilder

data class PaginationParams(
    val skip: Int = 0,
    val limit: Int = 50
) {
    init {
        require(skip >= 0) { "skip must be >= 0, got $skip" }
        require(limit > 0) { "limit must be > 0, got $limit" }
    }

    val nextPage: PaginationParams get() = copy(skip = skip + limit)
    val previousPage: PaginationParams? get() = if (skip >= limit) copy(skip = skip - limit) else null
}

data class PageParams(
    val page: Int = 1,
    val limit: Int = 20
) {
    init {
        require(page >= 1) { "page must be >= 1, got $page" }
        require(limit > 0) { "limit must be > 0, got $limit" }
    }

    val nextPage: PageParams get() = copy(page = page + 1)
    val previousPage: PageParams? get() = if (page > 1) copy(page = page - 1) else null

    fun toSkipLimit(): PaginationParams = PaginationParams(
        skip = (page - 1) * limit,
        limit = limit
    )
}

fun HttpRequestBuilder.paginate(params: PaginationParams) {
    url.parameters.append("skip", params.skip.toString())
    url.parameters.append("limit", params.limit.toString())
}

fun HttpRequestBuilder.paginate(params: PageParams) {
    url.parameters.append("page", params.page.toString())
    url.parameters.append("limit", params.limit.toString())
}

fun <T> List<T>.hasMore(params: PaginationParams): Boolean = size >= params.limit

data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: PaginationParams,
    val hasMore: Boolean
) {
    val totalFetched: Int get() = pagination.skip + items.size
}
