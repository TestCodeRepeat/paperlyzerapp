package com.flyingobjex.draft

import com.flyingobjex.paperlyzer.parser.LineParser
import com.flyingobjex.paperlyzer.util.Parser
import data.MockData
import org.junit.Test
import kotlin.test.assertEquals

class WOSPaperBuilderTest {

    private val lines = LineParser.stringToLines(MockData.file1snippet)
    private val bookmarks = Parser.linesToBookmarks(lines)
    private val papers = bookmarks.mapNotNull { Parser.bookmarkToPaper(it, lines) }

    @Test
    fun `should extract all authors full names`(){
        val paper = papers[0]
        val res = paper // Parser.authorLinesToAuthors(paper.authorLines)
        println("PaperBuilderTest.kt :: PaperBuilderTest :: res = " + res)
    }

    @Test
    fun `should map lines to pages using bookmarks`() {
        val bookmarks = Parser.linesToBookmarks(lines)
        val papers = bookmarks.mapNotNull { Parser.bookmarkToPaper(it, lines) }
        println("PaperBuilderTest.kt :: PaperBuilderTest :: papers.size = " + papers.size)
        assertEquals(bookmarks.size, 3)
        assertEquals(papers.size, 2)
    }

    @Test
    fun `should parse into bookmarks`() {
        val bookmarks = Parser.linesToBookmarks(lines)
        println("PaperBuilderTest.kt :: PaperBuilderTest :: res.size = " + bookmarks.toString())
    }
}