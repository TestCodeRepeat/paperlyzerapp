package com.flyingobjex.paperlyzer.util

import co.touchlab.kermit.Kermit

class KermitLogger {

    val log = Kermit()

    init {
        log.i { "Message" }
    }
}
