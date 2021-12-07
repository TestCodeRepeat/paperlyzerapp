package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.parser.CSVParser
import kotlinx.serialization.Serializable
import org.litote.kmongo.*

@Serializable
data class FirstName(
    val firstName: String,
    val originalFirstName: String,
    val originalLastName: String,
    val done: Boolean = false,
    val _id: Id<FirstName>? = null
)

private val regexAlphaNumeric = Regex("[^A-Za-z0-9 -]")
fun cleanFirstName(firstName: String): String? {
    val res = firstName
        .split(" ")
        .getOrNull(0) ?: return null
    return regexAlphaNumeric.replace(res, "")
}

//class CsvParserRepo(val mongo: Mongo) {
//
//    fun liveCsvFileToAuthorTable(path: String): List<Author> {
//        val authors = CSVParser.csvFileToAuthors(path)
//        mongo.addToRawAuthorsCollection(authors)
//        return authors
//    }
//
//}
