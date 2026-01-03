package io.github.ladder.backend.listings.api

object ListingControllerUtils {

    fun normalizeString(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun nonNegativeOrNull(v: Long?): Long? {
        return if (v != null && v >= 0) v else null
    }
}
