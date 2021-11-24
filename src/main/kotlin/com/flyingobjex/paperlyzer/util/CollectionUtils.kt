package com.flyingobjex.paperlyzer.util

class CollectionUtils {

    companion object {
        fun <T> withoutFirst(authors: List<T>): List<T> {
            return if (authors.size > 1) {
                authors.subList(1, authors.size - 1)
            } else {
                emptyList()
            }
        }

        fun <T> withoutLast(authors: List<T>): List<T> {
            return if (authors.size > 1) {
                authors.take(authors.size - 1)
            } else {
                emptyList()
            }
        }
    }
}
