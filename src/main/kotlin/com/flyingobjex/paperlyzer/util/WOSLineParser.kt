package com.flyingobjex.paperlyzer.util

import com.flyingobjex.paperlyzer.entity.LineItem

/** On text file, bookmark the starting & ending lines  */
data class Bookmark(
    val startingIndex: Int = -1,
    val endingIndex: Int = -1,
)

const val authorFullNameKey = "AF"
const val titleKey = "TI"

class WOSLineParser(rawLines: List<LineItem>) {

    var authorFullNameIndex: Int = -2
    var titleIndex: Int = -2

    /** Process the text chunk on initialization & mark  */
    init {
        rawLines.forEachIndexed { index, lineItem ->
            when (lineItem.key) {
                authorFullNameKey -> {
                    authorFullNameIndex = index
                }
                titleKey -> {
                    titleIndex = index
                }
            }
        }

        /** Cherry pick the lines of text between two wos keys */
//        authors = rawLines.subList(authorFullNameIndex, titleIndex).map{
//            Author.toAuthor(it.body)
//        }
    }
}

object Parser {

    private const val startingKey = "PT"
    private const val endingKey = "ER"

    fun bookmarkToPaper(bookmark: Bookmark, lines: List<LineItem>): WOSLineParser? {
        if (bookmark.startingIndex == -1 || lines.size < bookmark.endingIndex) return null
        val subset = lines.subList(bookmark.startingIndex, bookmark.endingIndex)
        return WOSLineParser(subset)
    }

    fun linesToBookmarks(lines: List<LineItem>): List<Bookmark> {
        val startingKeyIndexes = emptyList<Int>().toMutableList()
        val endingKeyIndexes = emptyList<Int>().toMutableList()

        lines.forEachIndexed { index, lineItem ->
            when (lineItem.key) {
                startingKey -> {
                    startingKeyIndexes.add(index)
                }
                endingKey -> {
                    endingKeyIndexes.add(index + 1)
                }
            }
        }

        return startingKeyIndexes.mapIndexed { index, startingKeyIndex ->
            if (index >= endingKeyIndexes.size) return@mapIndexed Bookmark(-1, -1)
            Bookmark(startingKeyIndex, endingKeyIndexes[index])
        }
    }
}

