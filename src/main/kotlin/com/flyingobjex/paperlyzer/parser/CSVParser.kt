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
                    val csvLine = RawCsvPaper(
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
                    )
                    val paper = rawCsvLinePaperToPaper(csvLine)
                    res.add(paper)
                }
            }
        }

        return res.toList()
    }

    fun csvFileToAuthors(path: String): List<Author> {
        val res = mutableListOf<Author>()
        csvReader() {
            delimiter = '\t'
            escapeChar = '\\'
//            quoteChar = '"'
//            charset = "ISO_8859_1"
        }.open(path) {
            readAllAsSequence().forEachIndexed { index, row ->
                if (index > 0) {
                    val csvLine = RawCsvPaper(
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
                    )
                    val paper = rawCsvLinePaperToPaper(csvLine)

                    res.addAll(paper.authors)
//                    res.addAll(paper.authors.filter{it.gender.gender != GenderIdentitiy.NOFIRSTNAME})
                }
            }
        }

        return res.toList()
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
        )
    }

    fun authorsCellToAuthors(authorsString: String, metadata: PaperMetatdata): List<Author> {
        val splitAuthors = authorsString.split("/")
        return splitAuthors.map { untrimmedName ->
            val rawName = untrimmedName.trim()
            if (rawName.contains(",")) {
                val splitName = rawName.split(",")
                val lastName = splitName[0].trim()
                val firstMiddleNames = splitName.getOrNull(1)?.trim() ?: "X"
                val firstName = if (firstMiddleNames.contains(" ")) {
                    firstMiddleNames.split(" ")[0]
                } else {
                    firstMiddleNames
                }

                val middleName = firstMiddleNames.split(" ").getOrNull(1).toString()
                val firstNameIsAbbreviated = (isAbbreviation(firstMiddleNames))

                val orcid = metadata.orcidForNames(lastName, firstName)

                Author(
                    lastName,
                    if (firstNameIsAbbreviated) orcid?.firstName else firstName,
                    middleName,
                    if (firstNameIsAbbreviated) Gender.initials else Gender.unassigned,
                    mutableListOf(metadata),
                    orcID = orcid,
                    orcIDString = orcid?.id,
                )
            } else {
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

    fun csvRowToRawPapers(rows: List<List<String>>): List<RawCsvPaper> {
        return rows.mapIndexedNotNull { index, list ->
            if (index == 0) {
                return@mapIndexedNotNull null
            } else {
                return@mapIndexedNotNull RawCsvPaper(
                    list[0],
                    list[1],
                    list[2],
                    list[3],
                    list[4],
                    list[5],
                    list[6],
                    list[7],
                    list[8],
                    list[9],
                    list[10],
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
