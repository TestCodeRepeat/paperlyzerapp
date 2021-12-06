package com.flyingobjex.process.discipline

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.CSVTopicParser
import com.flyingobjex.paperlyzer.parser.PLTopic
import com.flyingobjex.paperlyzer.parser.TopicMatcher
import com.flyingobjex.paperlyzer.process.DisciplineProcess
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.util.setMongoDbLogsToErrorOnly
import data.testPaper
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import kotlin.test.Test
import kotlinx.serialization.encodeToString

class DisciplineProcessTest {

    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)

    private val samplePath = "./ignore/topics.tsv"
    private val topics: List<PLTopic> = CSVTopicParser.csvFileToTopicList(samplePath)
    private val testTopicA = "Metallurgy & Metallurgical Engineering"
    private val testTopicB = "Geochemistry & Geophysics"

    private val format = Json { prettyPrint = false }
    private val matcher = TopicMatcher(topics)

    private val app = PaperlyzerApp(mongo)
    private val process = DisciplineProcess(mongo, matcher)


    //    @Test
    fun `print stats`() {
        app.process.printStats()
    }

    //    @Before
    fun before() {
        setMongoDbLogsToErrorOnly()
    }

    init {
        setMongoDbLogsToErrorOnly()
    }


    //    @Test
    fun `app should start Discipline process`() {
        wosRepo.quickResetDisciplineProcessed()
        setMongoDbLogsToErrorOnly()
        val r1 = app.start()
        app.process.printStats()
        println("DisciplineTest.kt :: app should start Discipline process :: DONE!")
    }

//    @Test
    fun `should start process to apply disciplines to all papers`() {
        wosRepo.resetDisciplineProcessed()

        process.runProcess()

        val processed = mongo.genderedPapers.find(WosPaper::discipline ne null).toList()
        assertEquals(500, processed.size)

        println("DisciplineTest.kt :: should start process to apply disciplines to all papers :: ")
    }

//    @Test
    fun `process should print stats`() {
        app.process.printStats()
    }

//    @Test
    fun `should count unprocessed papers`() {
        val res = mongo.genderedPapers.countDocuments(WosPaper::discipline eq null).toInt()
        assertTrue(res > 300000)
    }

//    @Test
    fun `should apply disciplines and matching criteria to WoSPaper`() {
        wosRepo.resetDisciplineProcessed()
        setMongoDbLogsToErrorOnly()
        val paper = format.decodeFromString<WosPaper>(testPaper)
        val matchingCriteriaForTopics = matcher.criteriaForTopics(paper.topics)
        val updated = wosRepo.applyMatchingCriteria(paper, matchingCriteriaForTopics)
        val res = mongo.genderedPapers.insertOne(updated)
        println("DisciplineTest.kt :: should apply disciplines and matching criteria to WoSPaper() :: res = " + res)

    }

    @Test
    fun `should map a paper to a list of PLTopics`() {
        val paper = format.decodeFromString<WosPaper>(testPaper)
        val matchingCriteriaForTopics = matcher.criteriaForTopics(paper.topics)
        assertEquals(2, matchingCriteriaForTopics.size)
        println("DisciplineTest.kt :: should map a paper to a list of PLTopics :: ")
    }

    @Test
    fun `it should return matching criteria for two terms`() {
        val res = matcher.matchToTopic(testTopicA, PLTopic.blank().copy(name = "Metallurgy"), 0)
        assertTrue(res.oneKeyword)
        println("DisciplineTest.kt :: should map a paper to a list of PLTopics :: ")
    }

    @Test
    fun `should import topic csv`() {
        val topics = CSVTopicParser.csvFileToTopicList(samplePath)
        File("topics.json").writeText(format.encodeToString(topics))
        assertEquals(374, topics.size)
    }
}
