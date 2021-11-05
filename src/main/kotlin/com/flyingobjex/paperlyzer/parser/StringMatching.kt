package com.flyingobjex.paperlyzer.parser

object StringMatching {

    fun clean(target: String): String {
        val reg = Regex("[^A-Za-z0-9 ]")
        return reg.replace(target, "").toLowerCase() // works
    }

    fun phrase(offset: Int, t: List<String>, totalWords: Int): String {
        return when (totalWords) {
            1 -> t[0 + offset]
            2 -> "${t[0 + offset]} ${t[1 + offset]}"
            3 -> "${t[0 + offset]} ${t[1 + offset]} ${t[2 + offset]}"
            else -> {
                ""
            }
        }
    }

    fun matchAllKeywords(term: String, targetText: String): Boolean {
        val numOfKeywords = clean(term).trim().split(" ").size
        return matchAnyN(term, targetText, numOfKeywords)
    }

    fun matchingWordPercent(term: String, targetText: String): Double {

        return .99
    }

    fun matchAnyN(term: String, targetText: String, n: Int): Boolean {
        val t = targetText.toLowerCase().split(" ")
        var total = 0
        val lowerCaseTerm = term.toLowerCase()
        t.map {
            if (lowerCaseTerm.contains(it)) {
                total++
            }
        }

        return total >= n
    }

    fun matchAnyTwoSequential(term: String, title: String): Boolean {
        val words = title.split(" ")
        if ((words.size > 1) && term.contains(phrase(0, words, 2))) {
            return true
        }

        if ((words.size > 2) && term.contains(phrase(1, words, 2))) {
            return true
        }

        if ((words.size > 3) && term.contains(phrase(2, words, 2))) {
            return true
        }

        return false
    }

    fun matchAnyThreeSequential(term: String, title: String): Boolean {
        val words = title.split(" ")

        if ((words.size > 2) && term.contains(phrase(0, words, 3))) {
            return true
        }

        if ((words.size > 3) && term.contains(phrase(1, words, 3))) {
            return true
        }

        if ((words.size > 4) && term.contains(phrase(2, words, 3))) {
            return true
        }

        return false
    }

}
