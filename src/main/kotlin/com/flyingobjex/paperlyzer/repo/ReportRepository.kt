package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.AuthorReportLine
import com.flyingobjex.paperlyzer.process.reports.AuthorReportStats

class ReportRepository(val mongo: Mongo) {

    fun addAuthorReportLines(reportLines: List<AuthorReportLine>) {
        mongo.authorReport.insertMany(reportLines)
    }

    fun addAuthorReportLine(line:AuthorReportLine){
        mongo.authorReport.insertOne(line)
    }

    fun resetAuthorReport() {
        mongo.authorReport.drop()
    }

    fun getAuthorReportStats(): AuthorReportStats {
        val processed = mongo.authorReport.countDocuments()
        val totalAuthors = mongo.genderedAuthors.countDocuments()
        return AuthorReportStats(
            totalReportsProcessed = processed,
            totalUnprocessed = totalAuthors - processed
        )
    }

}
