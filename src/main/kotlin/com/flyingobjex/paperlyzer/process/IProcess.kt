package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.ProcessType
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel

/**
 * Defines common behavior for long-running, cloud-deployed processes
 *
 *  - stop / start remote application via websockets
 *  - get statistics from specific process
 *
 * */
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
