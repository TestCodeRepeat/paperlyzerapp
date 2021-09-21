package com.flyingobjex.paperlyzer.parser

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.FileWriter

object CSVHelper {

    val mapper: CsvMapper = CsvMapper().apply {
        registerModules(KotlinModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun <C> writer(javaClass: Class<C>, withHeader: Boolean = true): ObjectWriter {
        if (withHeader) {

            return mapper.writer(mapper.schemaFor(javaClass).withHeader())
        }
        return mapper.writer(mapper.schemaFor(javaClass).withoutHeader())
    }


    inline fun <reified T> writeCsvFile(data: Collection<T>, fileName: String): File {

        val tempFile = createTempFile(prefix = fileName, suffix = ".csv")

        FileWriter(tempFile).use { writer ->
            writer(T::class.java, true)
                .writeValues(writer)
                .writeAll(data)
                .close()
        }

        return tempFile
    }


    inline fun <reified T> writeCsvString(data: T, withHeader: Boolean = true): String {
        return writer(data!!::class.java, withHeader).writeValueAsString(data)
    }

}

inline fun <reified T> T.toCSV(withHeader: Boolean = true): String = CSVHelper.writeCsvString(this, withHeader)

inline fun <reified T> Collection<T>.toCSV(withHeader: Boolean = true): String {
    if (this.isEmpty()) {
        return ""
    }
    val firstEntry = this.first().toCSV(withHeader)
    if (this.size == 1) {
        return firstEntry
    }
    val rest = this.drop(1).joinToString("\n") { it.toCSV(false) }

    return "$firstEntry\n$rest"
}
