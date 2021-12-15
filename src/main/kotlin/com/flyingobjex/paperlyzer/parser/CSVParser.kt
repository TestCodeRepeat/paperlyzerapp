package com.flyingobjex.paperlyzer.parser

import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.repo.isAbbreviation
import com.flyingobjex.paperlyzer.parser.LineParser.cleanOrcId
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

object CSVParser {

    fun csvFileToRawPapers(path: String): List<WosPaper> {
        val res = mutableListOf<WosPaper>()
        csvReader() {
            delimiter = '\t'
            escapeChar = '\\'
//            quoteChar = '"'
//            charset = "ISO_8859_1"
        }.open(path) {
            readAllAsSequence().forEachIndexed { index, row ->
                if (index > 0) {
                    val csvLine = rowToRawPaper(row)
                    val paper = rawCsvLinePaperToPaper(csvLine)
                    res.add(paper)
                }
            }
        }

        return res.toList()
    }

    private fun rowToRawPaper(row: List<String>) = RawCsvPaper(
        row[0],
        row[1],
        row[2],
        row[3],
        row[4],
        row[5],
        row[6],
        row[7],
        row[8],
        row[9],
        row[10],
        rawAuthorsText = row[1]
    )


    fun csvFileToAuthors(path: String): List<Author> {
        val rawAuthors = mutableListOf<Author>()
        csvReader() {
            delimiter = '\t'
            escapeChar = '\\'
//            quoteChar = '"'
//            charset = "ISO_8859_1"
        }.open(path) {
            readAllAsSequence().forEachIndexed { index, row ->
                if (index > 0) {
                    val csvLine = rowToRawPaper(row)
                    val paper = rawCsvLinePaperToPaper(csvLine)
                    rawAuthors.addAll(paper.authors)
                }
            }
        }
        return rawAuthors.toList()
    }

    fun rawCsvLinePaperToPaper(line: RawCsvPaper): WosPaper {
        val orcIds = getOrchidIds(line.orcid)
        val paperMetaData = PaperMetatdata(
            line.shortTitle,
            line.year,
            line.title,
            line.journal,
            line.orcid,
            orcIds,
            line.doi,
            line.topic.split(";"),
            line.authors,
        )
        val authors = authorsCellToAuthors(line.authors, paperMetaData)
        return WosPaper(
            line.shortTitle,
            authors,
            line.year,
            line.title,
            line.journal,
            line.text_type,
            line.keywords,
            line.emails,
            line.orcid,
            orcIds,
            line.doi,
            line.topic.split(";"),
            totalAuthors = authors.size,
            rawAuthorsText = line.authors
        )
    }

    fun getFirstName(byline: String): String? {
        val fullNameSplit = byline.split(",").map { it.trim() }
        fullNameSplit.lastOrNull()?.let { firstNames ->
            if (firstNames.contains(" ")) {
                val firstName = firstNames.split(" ").firstOrNull()
                if (firstName != null) return firstName
            } else {
                return firstNames.trim()
            }
        }
        return null
    }

    fun getLastName(byline: String): String? = byline.split(",").map { it.trim() }.firstOrNull()

    fun authorsCellToAuthors(authorsString: String, metadata: PaperMetatdata): List<Author> {
        val splitAuthors = authorsString.split("/")
        return splitAuthors.map { untrimmedName ->
            val rawName = untrimmedName.trim()

            if (rawName.contains(",")) { // Process name from string

                val fullNameSplit = rawName.split(",")
                val fullName = "${fullNameSplit.lastOrNull()}"


                val splitName = rawName.split(",")
                val lastName = splitName[0].trim()
                val initials = middleInitialFromLastName(lastName)

                val firstMiddleNames = splitName.getOrNull(1)?.trim() ?: "X"
                val firstName = if (firstMiddleNames.contains(" ")) {
                    firstMiddleNames.split(" ")[0]
                } else {
                    firstMiddleNames
                }
                val middleNamesCombinedAfterSplit =
                    listOf(firstMiddleNames.split(" ").getOrNull(1), firstMiddleNames.split(" ").getOrNull(2))
                        .filterNotNull()
                val joinedToString = middleNamesCombinedAfterSplit.joinToString(" ")

                val middleNameFromInitials = initials?.first
                val middleName =
                    middleNameFromInitials ?: if (middleNamesCombinedAfterSplit.isNotEmpty()) joinedToString else null

                val firstNameIsAbbreviated = (isAbbreviation(firstMiddleNames))
                val orcid = metadata.orcidForNames(lastName, firstName)

                Author(
                    initials?.second ?: lastName,
                    if (firstNameIsAbbreviated) orcid?.firstName else firstName,
                    middleName,
                    if (firstNameIsAbbreviated) Gender.initials else Gender.unassigned,
                    mutableListOf(metadata),
                    orcID = orcid,
                    orcIDString = orcid?.id,
                )

            } else { // Else get names from Orc Id

                val orcid = metadata.orcidForNames(rawName.trim(), "")
                return@map Author(
                    rawName,
                    orcid?.firstName,
                    null,
                    Gender.nofirstname,
                    mutableListOf(metadata),
                    orcID = orcid,
                    orcIDString = orcid?.id

                )
            }
        }
    }

    private fun middleInitialFromLastName(lastName: String): Pair<String, String>? {
        if (!lastName.contains(".")) return null
        val initial = lastName.split(".")[0]
        val lastName = lastName.split(".")[1].trim()
        return Pair("${initial}.", lastName)
    }

    fun csvRowToRawPapers(rows: List<List<String>>): List<RawCsvPaper> {
        return rows.mapIndexedNotNull { index, headers ->
            if (index == 0) {
                return@mapIndexedNotNull null
            } else {
                return@mapIndexedNotNull RawCsvPaper(
                    shortTitle = headers[0],
                    authors = headers[1],
                    year = headers[2],
                    title = headers[3],
                    journal = headers[4],
                    text_type = headers[5],
                    keywords = headers[6],
                    emails = headers[7],
                    orcid = headers[8],
                    doi = headers[9],
                    topic = headers[10],
                    rawAuthorsText = headers[1],
                )
            }
        }
    }

    fun csvFileToRows(path: String): List<List<String>> {
        val file = File(path)
        val rows: List<List<String>> = csvReader() {
//            delimiter = '\t'
//            escapeChar = '\\'
//            quoteChar = '"'
        }.readAll(file)
        return rows
    }

    fun getOrchidIds(value: String): List<OrcID> {
        if (value.length < 5) return emptyList()
        val a = value.split("/")
        val orcId1 = toOrcId(a.getOrNull(0), a.getOrNull(1))
        val orcId2 = toOrcId(a.getOrNull(2), a.getOrNull(3))

        return listOfNotNull(orcId1, orcId2)
    }

    private fun toOrcId(fullName: String?, id: String?): OrcID? {
        if (fullName == null || id == null) {
            return null
        }
        val split = fullName.split(",")
        val lastName = split.getOrNull(0) ?: return null
        val firstMiddleNames = split.getOrNull(1) ?: return null
        val firstName = if (firstMiddleNames.trim().contains(" "))
            firstMiddleNames.split(" ").getOrNull(0) ?: return null
        else firstMiddleNames

        val middleName = if (firstMiddleNames.trim().contains(" ")) firstMiddleNames.split(" ").getOrNull(1) else null

        return OrcID(cleanOrcId(id), lastName.trim(), firstName.trim(), middleName?.trim())
    }

}
