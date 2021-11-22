package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.ProcessType
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel

/** Defines common behavior long-running cloud processes */
interface IProcess {
    fun init()
    fun name():String
    fun runProcess()
    fun shouldContinueProcess(): Boolean
    fun printStats(outgoing: SendChannel<Frame>? = null): String
    fun cancelJobs()
    fun reset()
    fun type():ProcessType
}
