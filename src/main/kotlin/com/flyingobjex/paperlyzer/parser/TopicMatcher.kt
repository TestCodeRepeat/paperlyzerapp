package com.flyingobjex.paperlyzer.parser

class TopicMatcher(val plTopics: List<PLTopic>) {


    fun matchToTopic(term: String, topic: PLTopic, index: Int): MatchingCriteria {
        val trimmedTerm = term.trim().toLowerCase()
        val trimmedTopic = topic.name.trim().toLowerCase()
        val exactMatch = trimmedTerm.equals(trimmedTopic, ignoreCase = true)
        val oneKeyword = StringMatching.matchAnyN(trimmedTerm, trimmedTopic, 1)
        val allKeywords = StringMatching.matchAllKeywords(trimmedTerm, trimmedTopic)
        val leadKeyword = leadKeywordMatch(trimmedTerm, trimmedTopic)
        val secondaryKeyword = secondaryKeywordMatch(trimmedTerm, trimmedTopic)
        return MatchingCriteria(
            exactMatch,
            oneKeyword,
            allKeywords,
            leadKeyword,
            secondaryKeyword,
            term.trim(),
            topic,
            index
        )
    }

    fun matchTopicByCriteria(term: String, topic: PLTopic, index: Int): MatchingCriteria {
        val wordCountTerm = StringMatching.clean(term).trim().split(" ").size
        val wordCountTopic = StringMatching.clean(topic.name).trim().split(" ").size
        val exactMatch = term.toLowerCase().contains(topic.name.toLowerCase()) && (wordCountTerm == wordCountTopic)
        val oneKeyword = StringMatching.matchAnyN(term, topic.name, 1)
        val allKeywords = StringMatching.matchAllKeywords(term, topic.name)
        val leadKeyword = leadKeywordMatch(term, topic.name)
        val secondaryKeyword = secondaryKeywordMatch(term, topic.name)
        return MatchingCriteria(
            exactMatch,
            oneKeyword,
            allKeywords,
            leadKeyword,
            secondaryKeyword,
            term,
            topic,
            index
        )
    }

    private fun leadKeywordMatch(term: String, targetValue: String): Boolean {
        val lead = CSVTopicParser.toLeadKeyword(term) ?: return false
        return StringMatching.matchAnyN(lead, targetValue, 1)
    }

    private fun secondaryKeywordMatch(term: String, targetValue: String): Boolean {
        val secondary = CSVTopicParser.toSecondarKeyword(term) ?: return false
        return StringMatching.matchAnyN(secondary, targetValue, 1)
    }

    fun criteriaForTopics(topics: List<String>): List<MatchingCriteria> {
        return topics.mapIndexed { index, topicFromPaper ->
            val matching = plTopics.map { plTopic ->
                matchToTopic(topicFromPaper, plTopic, index)
            }.filter { it.hasOneTrue() }
            matching
        }.flatten()
            .sortedByDescending { it.score }
    }

    private fun hasBothDisciplines(allMatching: List<MatchingCriteria>) {
        val hasStem = allMatching.filter { it.topic.disciplineType == DisciplineType.STEM }.isNotEmpty()
        val hasSSH = allMatching.filter { it.topic.disciplineType == DisciplineType.SSH }.isNotEmpty()
        if (hasSSH && hasStem) {
            println("CSVTopicParser.kt :: criteriaForTopics() ::  = ")
        }
    }
}
