package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.api.GenderApi
import com.flyingobjex.paperlyzer.api.GenderApiRequest
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.api.GENDER_API_KEY
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.GenderDetails
import org.litote.kmongo.*

class GenderRepo(val mongo: Mongo) {

    val genderApi = GenderApi(GENDER_API_KEY)
    private val cutoff = 1

    suspend fun runContinuousGenderApi() {
        val batchSize = 50
        while (remainingRecordsToRun() > cutoff) {
            println("start")
            println(
                "mongo.firstNameTable.remaining records == ${remainingRecordsToRun()}"
            )
            runBatchAgainstGenderApi(batchSize)
            println("end")
        }
    }

    fun remainingRecordsToRun(): Int {
        return mongo.firstNameTable.countDocuments(
            and(
                FirstName::done eq false,
                FirstName::firstName regex ".*-.*",
            )
        ).toInt()
    }

    fun resetHyphenatedInFirstNamesTable(): List<FirstName> {
        mongo.firstNameTable.updateMany(
            FirstName::firstName regex ".*-.*",
            setValue(FirstName::done, false)
        )

        return mongo.firstNameTable.find(
            FirstName::firstName regex ".*-.*"
        ).toList()
    }

    suspend fun runBatchAgainstGenderApi(batchSize: Int) {
        val batch = mongo.firstNameTable.find(
            and(
                FirstName::done eq false,
                FirstName::firstName regex ".*-.*",
            )
        ).limit(batchSize).toMutableList()

        val firstNamesBatch: List<GenderApiRequest> = batch.mapNotNull {
            cleanFirstName(it.firstName)?.let { firstName ->
                GenderApiRequest(firstName)
            } ?: run {
                return@mapNotNull null
            }
        }

        val res = genderApi.fetchGenderMultipleFirstNames(firstNamesBatch)
        val matches = res.mapIndexed { index, apiResponse ->

            GenderDetails(
                apiResponse.first_name.toString(),
                GenderIdentitiy.toType(apiResponse.gender.toString()),
                apiResponse.probability ?: 0.0,
                apiResponse,
                firstNamesBatch[index]
            )
        }
        mongo.genderTable.insertMany(matches)
        mongo.firstNameTable.updateMany(
            FirstName::_id `in` batch.map { it._id },
            setValue(FirstName::done, true)
        )

        println(res.toString())
    }

}
