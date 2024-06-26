package com.flyingobjex.paperlyzer

import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.process.reports.AuthorReportLine
import com.flyingobjex.paperlyzer.process.reports.PaperReportLine
import com.flyingobjex.paperlyzer.repo.FirstName
import com.flyingobjex.paperlyzer.util.setMongoDbLogsToErrorOnly
import org.litote.kmongo.*


data class OrcIdDuplicate(val firstName: String, val lastName: String, val orcIds: List<OrcID>)

fun isLocalDb(dbName: String): Boolean = dbName == "mongodb://localhost:27017"


class Mongo(useLiveDatabase: Boolean = false) {

    val mongoDbUri: String = System.getenv("MONGODB_URI") ?: "mongodb://localhost:27017"

    private val dbName = if (isLocalDb(mongoDbUri)) "paperlyzer" else "paperlyzer"

    private val client = KMongo.createClient(mongoDbUri) //get com.mongodb.MongoClient new instance
    private val database = client.getDatabase(dbName) //normal java driver usage

    val rawPaperFullDetails = database.getCollection<WosPaper>("rawPaperFullDetails")
    private val rawAuthorsCollection = database.getCollection<Author>("rawAuthors")
    private val paperCollection = database.getCollection<WosPaper>("papers")
    private val semanticScholarPaperCollection = database.getCollection<SemanticScholarPaper>("sspapers")
    private val authorsCollection = database.getCollection<Author>("authors")
    private val journalCollection = database.getCollection<Journal>("journals")
    private val reportCollection = database.getCollection<PaperReportLine>("reports")
    private val authorReportCollection = database.getCollection<AuthorReportLine>("authorReportCollection")
    private val semanticScholarAuthorCollection = database.getCollection<SemanticScholarAuthor>("semanticScholarAuthorCollection")

    private val genderedAuthorsCollection = database.getCollection<Author>("genderedAuthors")
    private val genderedPapersCollection = database.getCollection<WosPaper>("genderedPapersCollection")
    val orcidDuplicates = database.getCollection<OrcIdDuplicate>("orcidDuplicates")
    val genderedNameDetails = database.getCollection<GenderedNameDetails>("genderedNameDetails")
    val firstNameTable = database.getCollection<FirstName>("firstNameTable")



    val rawAuthors = rawAuthorsCollection
    val authors = authorsCollection
    val journals = journalCollection
    val genderedAuthors = genderedAuthorsCollection
    val genderedPapers = genderedPapersCollection
    val ssPapers = semanticScholarPaperCollection
    val reports = reportCollection
    val authorReport = authorReportCollection
    val ssAuthors = semanticScholarAuthorCollection

    init {
        setMongoDbLogsToErrorOnly()
        System.setProperty("DEBUG.MONGO", "false")
        System.setProperty("DB.TRACE", "false")

//        println("Mongo.kt :: Mongo :: mongoDbUri = " + mongoDbUri)
//        println("Mongo.kt :: Mongo :: databaseName = " + dbName)
        initIndexes()
    }

    fun resetIndexes() {
        initIndexes()
    }

    @Suppress("DuplicatedCode")
    private fun initIndexes() {

        ssAuthors.ensureIndex(SemanticScholarAuthor::authorId)
        ssAuthors.ensureIndex(SemanticScholarAuthor::firstName)
        ssAuthors.ensureIndex(SemanticScholarAuthor::lastName)
        ssAuthors.ensureIndex(SemanticScholarAuthor::firstNameProcessed)

        ssPapers.ensureIndex(SemanticScholarPaper::authors / SsAuthorDetails::authorId)
        ssPapers.ensureIndex(SemanticScholarPaper::title)
        ssPapers.ensureIndex(SemanticScholarPaper::wosDoi)

        firstNameTable.ensureIndex(FirstName::firstName)
        firstNameTable.ensureIndex(FirstName::firstName, FirstName::done)
        rawAuthorsCollection.ensureIndex(Author::duplicateCheck)

        rawPaperFullDetails.ensureIndex(WosPaper::processed)
        rawPaperFullDetails.ensureIndex(WosPaper::ssAuthorProcessedStep1)
        rawPaperFullDetails.ensureIndex(WosPaper::ssProcessed)

        genderedPapersCollection.ensureIndex(WosPaper::doi)
        genderedPapersCollection.ensureUniqueIndex(WosPaper::shortTitle)
        genderedPapersCollection.ensureIndex(WosPaper::sjrRank)
        genderedPapersCollection.ensureIndex(WosPaper::hIndex)

        journals.ensureIndex(Journal::journalName)
        journals.ensureIndex(Journal::doi)
        journals.ensureIndex(Journal::journalName, Journal::doi)
        journals.ensureIndex(Journal::textType)

        genderedAuthors.ensureIndex(Author::firstName)
        genderedAuthors.ensureIndex(Author::lastName)
        genderedAuthors.ensureIndex(Author::lastName, Author::firstName)
        genderedAuthors.ensureIndex(Author::orcIDString)
        genderedAuthors.ensureIndex(Author::firstName, Author::lastName, Author::orcIDString)
        genderedAuthors.ensureIndex(Author::gender)
        genderedAuthors.ensureIndex(Author::gender / Gender::gender)
        genderedAuthors.ensureIndex(Author::gender / Gender::gender, Author::duplicateCheck)
        genderedAuthors.ensureIndex(Author::averageCoAuthors)
        genderedAuthors.ensureIndex(Author::totalPapers)
        genderedAuthors.ensureIndex(Author::authorReportUnprocessed)
        genderedAuthors.ensureIndex(Author::discipline)
        genderedAuthors.ensureIndex(Author::disciplineScore)

        authorsCollection.ensureIndex(Author::firstName)
        authorsCollection.ensureIndex(Author::lastName)
        authorsCollection.ensureIndex(Author::lastName, Author::firstName)
        authorsCollection.ensureIndex(Author::orcIDString)
        authorsCollection.ensureIndex(Author::firstName, Author::lastName, Author::orcIDString)
        authorsCollection.ensureIndex(Author::gender)
        authorsCollection.ensureIndex(Author::gender / Gender::gender)
        authorsCollection.ensureIndex(Author::gender / Gender::gender, Author::duplicateCheck)

        genderedNameDetails.ensureIndex(GenderedNameDetails::firstName)
        genderedNameDetails.ensureIndex(GenderedNameDetails::firstName, GenderedNameDetails::genderIdentity)
        genderedNameDetails.ensureIndex(GenderedNameDetails::probability)
        genderedNameDetails.ensureIndex(GenderedNameDetails::probability, GenderedNameDetails::genderIdentity)
    }


    fun addToRawAuthorsCollection(authors: List<Author>) {
        rawAuthorsCollection.insertMany(authors)
    }

    fun addRawAuthor(author: Author) {
        rawAuthorsCollection.insertOne(author)
    }

    fun addPaper(paper: WosPaper) {
        paperCollection.insertOne(paper)
    }

    fun resetFirstNameTable() {
        firstNameTable.updateMany(
            FirstName::done eq true,
            setValue(FirstName::done, false)
        )
    }

    fun clearFirstNameTable() {
        firstNameTable.drop()
        firstNameTable.ensureIndex(FirstName::firstName)
        firstNameTable.ensureIndex(FirstName::firstName, FirstName::done)
    }

    fun clearGenderedAuthorsTable() = genderedAuthors.drop()

    fun clearAuthors() = authors.drop()

    fun clearRawPapers() = rawPaperFullDetails.drop()

    fun clearRawAuthors() = rawAuthorsCollection.drop()

    fun clearJournals() = journals.drop()
}
