package com.flyingobjex.draft

import com.flyingobjex.paperlyzer.parser.CSVParser
import org.junit.Test
import kotlin.test.assertEquals

class OrchidIDParserTest {


    @Test
    fun `should extract name from orchidID`() {
        val res = CSVParser.getOrchidIds(orchidsLine)
        println(res)
        assertEquals(res[0].firstName, "Paul")
        assertEquals(res[1].id, "0000-0003-3598-3767")
    }
}

const val orchidsLine = "Gurr, Paul/0000-0001-5246-9845/Webley, Paul/0000-0003-3598-3767/Min,"

const val authorsLine = "Liu, Min/Gurr, Paul A./Fu, Qiang/Webley, Paul A./Qiao, Greg G."
const val rawLine =
    """LiuGurr2018Twodimensionalnanosheetbasedgasseparationmembranes	"Liu, Min/Gurr, Paul A./Fu, Qiang/Webley, Paul A./Qiao, Greg G."	2018	Two-dimensional nanosheet-based gas separation membranes	JOURNAL OF MATERIALS CHEMISTRY A	Review	NA	qiangf@unimelb.edu.au/webley@unimelb.edu.au/gregghq@unimelb.edu.au	"Gurr, Paul/0000-0001-5246-9845/Webley, Paul/0000-0003-3598-3767/Min,"	10.1039/c8ta09070j	"Chemistry, Physical; Energy & Fuels; Materials Science,""""