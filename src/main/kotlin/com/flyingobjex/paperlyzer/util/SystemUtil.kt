package com.flyingobjex.paperlyzer.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

fun setMongoDbLogsToErrorOnly() {
    (LoggerFactory.getLogger("org.mongodb.driver") as Logger).level = Level.ERROR
}
