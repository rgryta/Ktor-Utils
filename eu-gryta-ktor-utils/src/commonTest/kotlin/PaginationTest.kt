import eu.gryta.ktor.utils.PageParams
import eu.gryta.ktor.utils.PaginatedResponse
import eu.gryta.ktor.utils.PaginationParams
import eu.gryta.ktor.utils.hasMore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaginationTest {

    @Test
    fun `PaginationParams validates skip not negative`() {
        assertFailsWith<IllegalArgumentException> {
            PaginationParams(skip = -1)
        }
    }

    @Test
    fun `PaginationParams validates limit positive`() {
        assertFailsWith<IllegalArgumentException> {
            PaginationParams(limit = 0)
        }
    }

    @Test
    fun `PaginationParams allows zero skip`() {
        val params = PaginationParams(skip = 0, limit = 10)
        assertEquals(0, params.skip)
    }

    @Test
    fun `PaginationParams nextPage increments correctly`() {
        val params = PaginationParams(skip = 0, limit = 10)
        val next = params.nextPage
        assertEquals(10, next.skip)
        assertEquals(10, next.limit)
    }

    @Test
    fun `PaginationParams previousPage returns null at start`() {
        val params = PaginationParams(skip = 0, limit = 10)
        assertNull(params.previousPage)
    }

    @Test
    fun `PaginationParams previousPage decrements correctly`() {
        val params = PaginationParams(skip = 20, limit = 10)
        val prev = params.previousPage
        assertNotNull(prev)
        assertEquals(10, prev.skip)
    }

    @Test
    fun `PageParams validates page at least 1`() {
        assertFailsWith<IllegalArgumentException> {
            PageParams(page = 0)
        }
    }

    @Test
    fun `PageParams validates limit positive`() {
        assertFailsWith<IllegalArgumentException> {
            PageParams(limit = 0)
        }
    }

    @Test
    fun `PageParams nextPage increments`() {
        val params = PageParams(page = 1, limit = 20)
        val next = params.nextPage
        assertEquals(2, next.page)
    }

    @Test
    fun `PageParams previousPage returns null at page 1`() {
        val params = PageParams(page = 1, limit = 20)
        assertNull(params.previousPage)
    }

    @Test
    fun `PageParams previousPage decrements`() {
        val params = PageParams(page = 3, limit = 20)
        val prev = params.previousPage
        assertNotNull(prev)
        assertEquals(2, prev.page)
    }

    @Test
    fun `PageParams toSkipLimit converts correctly`() {
        val params = PageParams(page = 3, limit = 20)
        val skipLimit = params.toSkipLimit()
        assertEquals(40, skipLimit.skip)
        assertEquals(20, skipLimit.limit)
    }

    @Test
    fun `PageParams page 1 toSkipLimit is zero skip`() {
        val params = PageParams(page = 1, limit = 20)
        val skipLimit = params.toSkipLimit()
        assertEquals(0, skipLimit.skip)
    }

    @Test
    fun `hasMore returns true when list equals limit`() {
        val params = PaginationParams(limit = 10)
        val list = (1..10).toList()
        assertTrue { list.hasMore(params) }
    }

    @Test
    fun `hasMore returns false when list smaller than limit`() {
        val params = PaginationParams(limit = 10)
        val list = (1..5).toList()
        assertFalse { list.hasMore(params) }
    }

    @Test
    fun `PaginatedResponse totalFetched calculates correctly`() {
        val response = PaginatedResponse(
            items = listOf(1, 2, 3, 4, 5),
            pagination = PaginationParams(skip = 10, limit = 5),
            hasMore = true
        )
        assertEquals(15, response.totalFetched)
    }
}
