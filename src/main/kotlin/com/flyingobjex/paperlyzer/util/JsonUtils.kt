package com.flyingobjex.paperlyzer.util

import java.io.File
import kotlinx.serialization.json.Json

object JsonUtils {

    val json = Json { prettyPrint = true }

    fun loadFile(path:String): String {
        return File(path).readText(Charsets.UTF_8)
    }


}
