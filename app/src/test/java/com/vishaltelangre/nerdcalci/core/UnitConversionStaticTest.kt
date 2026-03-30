package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class UnitConversionStaticTest {

    private fun createLine(expression: String, fileId: Long = 1L, sortOrder: Int = 0): LineEntity {
        return LineEntity(id = sortOrder.toLong(), fileId = fileId, expression = expression, result = "", sortOrder = sortOrder)
    }

    @Test
    fun `unit conversion static time`() = runBlocking {
        val lines = listOf(
            createLine("10 ns in s", sortOrder = 0),
            createLine("10 s in ns", sortOrder = 1),
            createLine("10 nanosecond in s", sortOrder = 2),
            createLine("10 s in nanosecond", sortOrder = 3),
            createLine("10 nanoseconds in s", sortOrder = 4),
            createLine("10 s in nanoseconds", sortOrder = 5),
            createLine("10 µs in s", sortOrder = 6),
            createLine("10 s in µs", sortOrder = 7),
            createLine("10 us in s", sortOrder = 8),
            createLine("10 s in us", sortOrder = 9),
            createLine("10 microsecond in s", sortOrder = 10),
            createLine("10 s in microsecond", sortOrder = 11),
            createLine("10 microseconds in s", sortOrder = 12),
            createLine("10 s in microseconds", sortOrder = 13),
            createLine("10 ms in s", sortOrder = 14),
            createLine("10 s in ms", sortOrder = 15),
            createLine("10 millisecond in s", sortOrder = 16),
            createLine("10 s in millisecond", sortOrder = 17),
            createLine("10 milliseconds in s", sortOrder = 18),
            createLine("10 s in milliseconds", sortOrder = 19),
            createLine("10 s in s", sortOrder = 20),
            createLine("10 s in s", sortOrder = 21),
            createLine("10 sec in s", sortOrder = 22),
            createLine("10 s in sec", sortOrder = 23),
            createLine("10 secs in s", sortOrder = 24),
            createLine("10 s in secs", sortOrder = 25),
            createLine("10 second in s", sortOrder = 26),
            createLine("10 s in second", sortOrder = 27),
            createLine("10 seconds in s", sortOrder = 28),
            createLine("10 s in seconds", sortOrder = 29),
            createLine("10 min in s", sortOrder = 30),
            createLine("10 s in min", sortOrder = 31),
            createLine("10 mins in s", sortOrder = 32),
            createLine("10 s in mins", sortOrder = 33),
            createLine("10 minute in s", sortOrder = 34),
            createLine("10 s in minute", sortOrder = 35),
            createLine("10 minutes in s", sortOrder = 36),
            createLine("10 s in minutes", sortOrder = 37),
            createLine("10 h in s", sortOrder = 38),
            createLine("10 s in h", sortOrder = 39),
            createLine("10 hr in s", sortOrder = 40),
            createLine("10 s in hr", sortOrder = 41),
            createLine("10 hrs in s", sortOrder = 42),
            createLine("10 s in hrs", sortOrder = 43),
            createLine("10 hour in s", sortOrder = 44),
            createLine("10 s in hour", sortOrder = 45),
            createLine("10 hours in s", sortOrder = 46),
            createLine("10 s in hours", sortOrder = 47),
            createLine("10 d in s", sortOrder = 48),
            createLine("10 s in d", sortOrder = 49),
            createLine("10 day in s", sortOrder = 50),
            createLine("10 s in day", sortOrder = 51),
            createLine("10 days in s", sortOrder = 52),
            createLine("10 s in days", sortOrder = 53),
            createLine("10 wk in s", sortOrder = 54),
            createLine("10 s in wk", sortOrder = 55),
            createLine("10 wks in s", sortOrder = 56),
            createLine("10 s in wks", sortOrder = 57),
            createLine("10 week in s", sortOrder = 58),
            createLine("10 s in week", sortOrder = 59),
            createLine("10 weeks in s", sortOrder = 60),
            createLine("10 s in weeks", sortOrder = 61),
            createLine("10 mo in s", sortOrder = 62),
            createLine("10 s in mo", sortOrder = 63),
            createLine("10 mnth in s", sortOrder = 64),
            createLine("10 s in mnth", sortOrder = 65),
            createLine("10 mnths in s", sortOrder = 66),
            createLine("10 s in mnths", sortOrder = 67),
            createLine("10 month in s", sortOrder = 68),
            createLine("10 s in month", sortOrder = 69),
            createLine("10 months in s", sortOrder = 70),
            createLine("10 s in months", sortOrder = 71),
            createLine("10 y in s", sortOrder = 72),
            createLine("10 s in y", sortOrder = 73),
            createLine("10 yr in s", sortOrder = 74),
            createLine("10 s in yr", sortOrder = 75),
            createLine("10 yrs in s", sortOrder = 76),
            createLine("10 s in yrs", sortOrder = 77),
            createLine("10 year in s", sortOrder = 78),
            createLine("10 s in year", sortOrder = 79),
            createLine("10 years in s", sortOrder = 80),
            createLine("10 s in years", sortOrder = 81),
            createLine("10 lustrum in s", sortOrder = 82),
            createLine("10 s in lustrum", sortOrder = 83),
            createLine("10 lustrums in s", sortOrder = 84),
            createLine("10 s in lustrums", sortOrder = 85),
            createLine("10 decade in s", sortOrder = 86),
            createLine("10 s in decade", sortOrder = 87),
            createLine("10 decades in s", sortOrder = 88),
            createLine("10 s in decades", sortOrder = 89),
            createLine("10 century in s", sortOrder = 90),
            createLine("10 s in century", sortOrder = 91),
            createLine("10 centuries in s", sortOrder = 92),
            createLine("10 s in centuries", sortOrder = 93),
            createLine("10 millennium in s", sortOrder = 94),
            createLine("10 s in millennium", sortOrder = 95),
            createLine("10 millennia in s", sortOrder = 96),
            createLine("10 s in millennia", sortOrder = 97),
            createLine("10 millenniums in s", sortOrder = 98),
            createLine("10 s in millenniums", sortOrder = 99),
            createLine("10 ds in s", sortOrder = 100),
            createLine("10 s in ds", sortOrder = 101),
            createLine("10 decisecond in s", sortOrder = 102),
            createLine("10 s in decisecond", sortOrder = 103),
            createLine("10 deciseconds in s", sortOrder = 104),
            createLine("10 s in deciseconds", sortOrder = 105),
            createLine("10 cs in s", sortOrder = 106),
            createLine("10 s in cs", sortOrder = 107),
            createLine("10 centisecond in s", sortOrder = 108),
            createLine("10 s in centisecond", sortOrder = 109),
            createLine("10 centiseconds in s", sortOrder = 110),
            createLine("10 s in centiseconds", sortOrder = 111),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("ns to s", 1.0E-8, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("s to ns", 1.0E10, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("nanosecond to s", 1.0E-8, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("s to nanosecond", 1.0E10, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("nanoseconds to s", 1.0E-8, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("s to nanoseconds", 1.0E10, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("µs to s", 9.999999999999999E-6, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("s to µs", 1.0E7, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("us to s", 9.999999999999999E-6, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("s to us", 1.0E7, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("microsecond to s", 9.999999999999999E-6, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("s to microsecond", 1.0E7, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("microseconds to s", 9.999999999999999E-6, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("s to microseconds", 1.0E7, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("ms to s", 0.01, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("s to ms", 10000.0, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("millisecond to s", 0.01, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("s to millisecond", 10000.0, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("milliseconds to s", 0.01, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("s to milliseconds", 10000.0, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("s to s", 10.0, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("s to s", 10.0, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("sec to s", 10.0, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("s to sec", 10.0, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("secs to s", 10.0, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("s to secs", 10.0, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("second to s", 10.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("s to second", 10.0, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("seconds to s", 10.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("s to seconds", 10.0, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("min to s", 600.0, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("s to min", 0.16666666666666666, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("mins to s", 600.0, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("s to mins", 0.16666666666666666, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("minute to s", 600.0, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("s to minute", 0.16666666666666666, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("minutes to s", 600.0, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("s to minutes", 0.16666666666666666, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("h to s", 36000.0, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("s to h", 0.002777777777777778, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("hr to s", 36000.0, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("s to hr", 0.002777777777777778, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("hrs to s", 36000.0, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("s to hrs", 0.002777777777777778, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("hour to s", 36000.0, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("s to hour", 0.002777777777777778, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("hours to s", 36000.0, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("s to hours", 0.002777777777777778, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("d to s", 864000.0, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("s to d", 1.1574074074074075E-4, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("day to s", 864000.0, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("s to day", 1.1574074074074075E-4, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("days to s", 864000.0, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("s to days", 1.1574074074074075E-4, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("wk to s", 6048000.0, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("s to wk", 1.6534391534391536E-5, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("wks to s", 6048000.0, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("s to wks", 1.6534391534391536E-5, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("week to s", 6048000.0, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("s to week", 1.6534391534391536E-5, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("weeks to s", 6048000.0, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("s to weeks", 1.6534391534391536E-5, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("mo to s", 2.629746E7, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("s to mo", 3.8026486208173715E-6, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("mnth to s", 2.629746E7, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("s to mnth", 3.8026486208173715E-6, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("mnths to s", 2.629746E7, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("s to mnths", 3.8026486208173715E-6, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("month to s", 2.629746E7, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("s to month", 3.8026486208173715E-6, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("months to s", 2.629746E7, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("s to months", 3.8026486208173715E-6, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("y to s", 3.1556952E8, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("s to y", 3.1688738506811433E-7, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("yr to s", 3.1556952E8, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("s to yr", 3.1688738506811433E-7, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("yrs to s", 3.1556952E8, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("s to yrs", 3.1688738506811433E-7, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("year to s", 3.1556952E8, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("s to year", 3.1688738506811433E-7, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("years to s", 3.1556952E8, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("s to years", 3.1688738506811433E-7, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("lustrum to s", 1.5778476E9, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("s to lustrum", 6.337747701362287E-8, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("lustrums to s", 1.5778476E9, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("s to lustrums", 6.337747701362287E-8, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("decade to s", 3.1556952E9, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("s to decade", 3.168873850681143E-8, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("decades to s", 3.1556952E9, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("s to decades", 3.168873850681143E-8, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("century to s", 3.1556952E10, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("s to century", 3.168873850681143E-9, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("centuries to s", 3.1556952E10, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("s to centuries", 3.168873850681143E-9, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("millennium to s", 3.1556952E11, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("s to millennium", 3.168873850681143E-10, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("millennia to s", 3.1556952E11, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("s to millennia", 3.168873850681143E-10, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("millenniums to s", 3.1556952E11, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("s to millenniums", 3.168873850681143E-10, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("ds to s", 1.0, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("s to ds", 100.0, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("decisecond to s", 1.0, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("s to decisecond", 100.0, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("deciseconds to s", 1.0, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("s to deciseconds", 100.0, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("cs to s", 0.1, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("s to cs", 1000.0, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("centisecond to s", 0.1, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("s to centisecond", 1000.0, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("centiseconds to s", 0.1, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("s to centiseconds", 1000.0, val111!!, 0.05)
    }

    @Test
    fun `unit conversion static length`() = runBlocking {
        val lines = listOf(
            createLine("10 nm in m", sortOrder = 0),
            createLine("10 m in nm", sortOrder = 1),
            createLine("10 nanometer in m", sortOrder = 2),
            createLine("10 m in nanometer", sortOrder = 3),
            createLine("10 nanometers in m", sortOrder = 4),
            createLine("10 m in nanometers", sortOrder = 5),
            createLine("10 µm in m", sortOrder = 6),
            createLine("10 m in µm", sortOrder = 7),
            createLine("10 um in m", sortOrder = 8),
            createLine("10 m in um", sortOrder = 9),
            createLine("10 micrometer in m", sortOrder = 10),
            createLine("10 m in micrometer", sortOrder = 11),
            createLine("10 micrometers in m", sortOrder = 12),
            createLine("10 m in micrometers", sortOrder = 13),
            createLine("10 mm in m", sortOrder = 14),
            createLine("10 m in mm", sortOrder = 15),
            createLine("10 millimeter in m", sortOrder = 16),
            createLine("10 m in millimeter", sortOrder = 17),
            createLine("10 millimeters in m", sortOrder = 18),
            createLine("10 m in millimeters", sortOrder = 19),
            createLine("10 cm in m", sortOrder = 20),
            createLine("10 m in cm", sortOrder = 21),
            createLine("10 centimeter in m", sortOrder = 22),
            createLine("10 m in centimeter", sortOrder = 23),
            createLine("10 centimeters in m", sortOrder = 24),
            createLine("10 m in centimeters", sortOrder = 25),
            createLine("10 dm in m", sortOrder = 26),
            createLine("10 m in dm", sortOrder = 27),
            createLine("10 decimeter in m", sortOrder = 28),
            createLine("10 m in decimeter", sortOrder = 29),
            createLine("10 decimeters in m", sortOrder = 30),
            createLine("10 m in decimeters", sortOrder = 31),
            createLine("10 m in m", sortOrder = 32),
            createLine("10 m in m", sortOrder = 33),
            createLine("10 meter in m", sortOrder = 34),
            createLine("10 m in meter", sortOrder = 35),
            createLine("10 meters in m", sortOrder = 36),
            createLine("10 m in meters", sortOrder = 37),
            createLine("10 km in m", sortOrder = 38),
            createLine("10 m in km", sortOrder = 39),
            createLine("10 kms in m", sortOrder = 40),
            createLine("10 m in kms", sortOrder = 41),
            createLine("10 kilometer in m", sortOrder = 42),
            createLine("10 m in kilometer", sortOrder = 43),
            createLine("10 kilometers in m", sortOrder = 44),
            createLine("10 m in kilometers", sortOrder = 45),
            createLine("10 inch in m", sortOrder = 46),
            createLine("10 m in inch", sortOrder = 47),
            createLine("10 inch in m", sortOrder = 48),
            createLine("10 m in inch", sortOrder = 49),
            createLine("10 inches in m", sortOrder = 50),
            createLine("10 m in inches", sortOrder = 51),
            createLine("10 ft in m", sortOrder = 52),
            createLine("10 m in ft", sortOrder = 53),
            createLine("10 foot in m", sortOrder = 54),
            createLine("10 m in foot", sortOrder = 55),
            createLine("10 feet in m", sortOrder = 56),
            createLine("10 m in feet", sortOrder = 57),
            createLine("10 yd in m", sortOrder = 58),
            createLine("10 m in yd", sortOrder = 59),
            createLine("10 yard in m", sortOrder = 60),
            createLine("10 m in yard", sortOrder = 61),
            createLine("10 yards in m", sortOrder = 62),
            createLine("10 m in yards", sortOrder = 63),
            createLine("10 mi in m", sortOrder = 64),
            createLine("10 m in mi", sortOrder = 65),
            createLine("10 mile in m", sortOrder = 66),
            createLine("10 m in mile", sortOrder = 67),
            createLine("10 miles in m", sortOrder = 68),
            createLine("10 m in miles", sortOrder = 69),
            createLine("10 fur in m", sortOrder = 70),
            createLine("10 m in fur", sortOrder = 71),
            createLine("10 furlong in m", sortOrder = 72),
            createLine("10 m in furlong", sortOrder = 73),
            createLine("10 ftm in m", sortOrder = 74),
            createLine("10 m in ftm", sortOrder = 75),
            createLine("10 fathom in m", sortOrder = 76),
            createLine("10 m in fathom", sortOrder = 77),
            createLine("10 NM in m", sortOrder = 78),
            createLine("10 m in NM", sortOrder = 79),
            createLine("10 nmi in m", sortOrder = 80),
            createLine("10 m in nmi", sortOrder = 81),
            createLine("10 ly in m", sortOrder = 82),
            createLine("10 m in ly", sortOrder = 83),
            createLine("10 Å in m", sortOrder = 84),
            createLine("10 m in Å", sortOrder = 85),
            createLine("10 angstrom in m", sortOrder = 86),
            createLine("10 m in angstrom", sortOrder = 87),
            createLine("10 angstroms in m", sortOrder = 88),
            createLine("10 m in angstroms", sortOrder = 89),
            createLine("10 pm in m", sortOrder = 90),
            createLine("10 m in pm", sortOrder = 91),
            createLine("10 picometer in m", sortOrder = 92),
            createLine("10 m in picometer", sortOrder = 93),
            createLine("10 picometers in m", sortOrder = 94),
            createLine("10 m in picometers", sortOrder = 95),
            createLine("10 au in m", sortOrder = 96),
            createLine("10 m in au", sortOrder = 97),
            createLine("10 AU in m", sortOrder = 98),
            createLine("10 m in AU", sortOrder = 99),
            createLine("10 astronomical unit in m", sortOrder = 100),
            createLine("10 m in astronomical unit", sortOrder = 101),
            createLine("10 astronomical units in m", sortOrder = 102),
            createLine("10 m in astronomical units", sortOrder = 103),
            createLine("10 px in m", sortOrder = 104),
            createLine("10 m in px", sortOrder = 105),
            createLine("10 pixel in m", sortOrder = 106),
            createLine("10 m in pixel", sortOrder = 107),
            createLine("10 pixels in m", sortOrder = 108),
            createLine("10 m in pixels", sortOrder = 109),
            createLine("10 pt in m", sortOrder = 110),
            createLine("10 m in pt", sortOrder = 111),
            createLine("10 em in m", sortOrder = 112),
            createLine("10 m in em", sortOrder = 113),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("nm to m", 1.0E-8, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("m to nm", 1.0E10, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("nanometer to m", 1.0E-8, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("m to nanometer", 1.0E10, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("nanometers to m", 1.0E-8, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("m to nanometers", 1.0E10, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("µm to m", 9.999999999999999E-6, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("m to µm", 1.0E7, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("um to m", 9.999999999999999E-6, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("m to um", 1.0E7, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("micrometer to m", 9.999999999999999E-6, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("m to micrometer", 1.0E7, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("micrometers to m", 9.999999999999999E-6, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("m to micrometers", 1.0E7, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("mm to m", 0.01, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("m to mm", 10000.0, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("millimeter to m", 0.01, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("m to millimeter", 10000.0, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("millimeters to m", 0.01, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("m to millimeters", 10000.0, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("cm to m", 0.1, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("m to cm", 1000.0, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("centimeter to m", 0.1, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("m to centimeter", 1000.0, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("centimeters to m", 0.1, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("m to centimeters", 1000.0, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("dm to m", 1.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("m to dm", 100.0, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("decimeter to m", 1.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("m to decimeter", 100.0, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("decimeters to m", 1.0, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("m to decimeters", 100.0, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("m to m", 10.0, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("m to m", 10.0, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("meter to m", 10.0, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("m to meter", 10.0, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("meters to m", 10.0, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("m to meters", 10.0, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("km to m", 10000.0, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("m to km", 0.01, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("kms to m", 10000.0, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("m to kms", 0.01, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("kilometer to m", 10000.0, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("m to kilometer", 0.01, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("kilometers to m", 10000.0, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("m to kilometers", 0.01, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("inch to m", 0.254, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("m to inch", 393.7007874015748, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("inch to m", 0.254, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("m to inch", 393.7007874015748, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("inches to m", 0.254, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("m to inches", 393.7007874015748, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("ft to m", 3.048, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("m to ft", 32.808398950131235, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("foot to m", 3.048, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("m to foot", 32.808398950131235, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("feet to m", 3.048, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("m to feet", 32.808398950131235, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("yd to m", 9.144, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("m to yd", 10.936132983377078, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("yard to m", 9.144, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("m to yard", 10.936132983377078, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("yards to m", 9.144, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("m to yards", 10.936132983377078, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("mi to m", 16093.44, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("m to mi", 0.006213711922373339, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("mile to m", 16093.44, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("m to mile", 0.006213711922373339, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("miles to m", 16093.44, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("m to miles", 0.006213711922373339, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("fur to m", 2011.68, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("m to fur", 0.049709695378986715, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("furlong to m", 2011.68, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("m to furlong", 0.049709695378986715, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("ftm to m", 18.288, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("m to ftm", 5.468066491688539, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("fathom to m", 18.288, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("m to fathom", 5.468066491688539, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("NM to m", 18520.0, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("m to NM", 0.005399568034557235, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("nmi to m", 18520.0, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("m to nmi", 0.005399568034557235, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("ly to m", 9.4607304725808E16, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("m to ly", 1.0570008340246156E-15, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("Å to m", 1.0E-9, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("m to Å", 1.0E11, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("angstrom to m", 1.0E-9, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("m to angstrom", 1.0E11, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("angstroms to m", 1.0E-9, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("m to angstroms", 1.0E11, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("pm to m", 1.0E-11, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("m to pm", 1.0E13, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("picometer to m", 1.0E-11, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("m to picometer", 1.0E13, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("picometers to m", 1.0E-11, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("m to picometers", 1.0E13, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("au to m", 1.495978707E12, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("m to au", 6.684587122268446E-11, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("AU to m", 1.495978707E12, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("m to AU", 6.684587122268446E-11, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("astronomical unit to m", 1.495978707E12, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("m to astronomical unit", 6.684587122268446E-11, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("astronomical units to m", 1.495978707E12, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("m to astronomical units", 6.684587122268446E-11, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("px to m", 0.002645833333333333, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("m to px", 37795.27559055119, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("pixel to m", 0.002645833333333333, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("m to pixel", 37795.27559055119, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("pixels to m", 0.002645833333333333, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("m to pixels", 37795.27559055119, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("pt to m", 0.0035277777777777777, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("m to pt", 28346.456692913387, val111!!, 0.05)
        val val112 = result[112].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val112)
        assertEquals("em to m", 0.04233333333333333, val112!!, 0.05)
        val val113 = result[113].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val113)
        assertEquals("m to em", 2362.204724409449, val113!!, 0.05)
    }

    @Test
    fun `unit conversion static area`() = runBlocking {
        val lines = listOf(
            createLine("10 nm² in m²", sortOrder = 0),
            createLine("10 m² in nm²", sortOrder = 1),
            createLine("10 nm2 in m²", sortOrder = 2),
            createLine("10 m² in nm2", sortOrder = 3),
            createLine("10 sqnm in m²", sortOrder = 4),
            createLine("10 m² in sqnm", sortOrder = 5),
            createLine("10 square nanometer in m²", sortOrder = 6),
            createLine("10 m² in square nanometer", sortOrder = 7),
            createLine("10 square nanometers in m²", sortOrder = 8),
            createLine("10 m² in square nanometers", sortOrder = 9),
            createLine("10 µm² in m²", sortOrder = 10),
            createLine("10 m² in µm²", sortOrder = 11),
            createLine("10 µm2 in m²", sortOrder = 12),
            createLine("10 m² in µm2", sortOrder = 13),
            createLine("10 um2 in m²", sortOrder = 14),
            createLine("10 m² in um2", sortOrder = 15),
            createLine("10 squm in m²", sortOrder = 16),
            createLine("10 m² in squm", sortOrder = 17),
            createLine("10 square micrometer in m²", sortOrder = 18),
            createLine("10 m² in square micrometer", sortOrder = 19),
            createLine("10 square micrometers in m²", sortOrder = 20),
            createLine("10 m² in square micrometers", sortOrder = 21),
            createLine("10 mm² in m²", sortOrder = 22),
            createLine("10 m² in mm²", sortOrder = 23),
            createLine("10 mm2 in m²", sortOrder = 24),
            createLine("10 m² in mm2", sortOrder = 25),
            createLine("10 sqmm in m²", sortOrder = 26),
            createLine("10 m² in sqmm", sortOrder = 27),
            createLine("10 square millimeter in m²", sortOrder = 28),
            createLine("10 m² in square millimeter", sortOrder = 29),
            createLine("10 square millimeters in m²", sortOrder = 30),
            createLine("10 m² in square millimeters", sortOrder = 31),
            createLine("10 cm² in m²", sortOrder = 32),
            createLine("10 m² in cm²", sortOrder = 33),
            createLine("10 cm2 in m²", sortOrder = 34),
            createLine("10 m² in cm2", sortOrder = 35),
            createLine("10 sqcm in m²", sortOrder = 36),
            createLine("10 m² in sqcm", sortOrder = 37),
            createLine("10 square centimeter in m²", sortOrder = 38),
            createLine("10 m² in square centimeter", sortOrder = 39),
            createLine("10 square centimeters in m²", sortOrder = 40),
            createLine("10 m² in square centimeters", sortOrder = 41),
            createLine("10 m² in m²", sortOrder = 42),
            createLine("10 m² in m²", sortOrder = 43),
            createLine("10 m2 in m²", sortOrder = 44),
            createLine("10 m² in m2", sortOrder = 45),
            createLine("10 sqm in m²", sortOrder = 46),
            createLine("10 m² in sqm", sortOrder = 47),
            createLine("10 square meter in m²", sortOrder = 48),
            createLine("10 m² in square meter", sortOrder = 49),
            createLine("10 square meters in m²", sortOrder = 50),
            createLine("10 m² in square meters", sortOrder = 51),
            createLine("10 km² in m²", sortOrder = 52),
            createLine("10 m² in km²", sortOrder = 53),
            createLine("10 km2 in m²", sortOrder = 54),
            createLine("10 m² in km2", sortOrder = 55),
            createLine("10 sqkm in m²", sortOrder = 56),
            createLine("10 m² in sqkm", sortOrder = 57),
            createLine("10 square kilometer in m²", sortOrder = 58),
            createLine("10 m² in square kilometer", sortOrder = 59),
            createLine("10 square kilometers in m²", sortOrder = 60),
            createLine("10 m² in square kilometers", sortOrder = 61),
            createLine("10 in² in m²", sortOrder = 62),
            createLine("10 m² in in²", sortOrder = 63),
            createLine("10 in2 in m²", sortOrder = 64),
            createLine("10 m² in in2", sortOrder = 65),
            createLine("10 sqin in m²", sortOrder = 66),
            createLine("10 m² in sqin", sortOrder = 67),
            createLine("10 square inch in m²", sortOrder = 68),
            createLine("10 m² in square inch", sortOrder = 69),
            createLine("10 square inches in m²", sortOrder = 70),
            createLine("10 m² in square inches", sortOrder = 71),
            createLine("10 ft² in m²", sortOrder = 72),
            createLine("10 m² in ft²", sortOrder = 73),
            createLine("10 ft2 in m²", sortOrder = 74),
            createLine("10 m² in ft2", sortOrder = 75),
            createLine("10 sqft in m²", sortOrder = 76),
            createLine("10 m² in sqft", sortOrder = 77),
            createLine("10 square foot in m²", sortOrder = 78),
            createLine("10 m² in square foot", sortOrder = 79),
            createLine("10 square feet in m²", sortOrder = 80),
            createLine("10 m² in square feet", sortOrder = 81),
            createLine("10 yd² in m²", sortOrder = 82),
            createLine("10 m² in yd²", sortOrder = 83),
            createLine("10 yd2 in m²", sortOrder = 84),
            createLine("10 m² in yd2", sortOrder = 85),
            createLine("10 sqyd in m²", sortOrder = 86),
            createLine("10 m² in sqyd", sortOrder = 87),
            createLine("10 square yard in m²", sortOrder = 88),
            createLine("10 m² in square yard", sortOrder = 89),
            createLine("10 square yards in m²", sortOrder = 90),
            createLine("10 m² in square yards", sortOrder = 91),
            createLine("10 mi² in m²", sortOrder = 92),
            createLine("10 m² in mi²", sortOrder = 93),
            createLine("10 mi2 in m²", sortOrder = 94),
            createLine("10 m² in mi2", sortOrder = 95),
            createLine("10 sqmi in m²", sortOrder = 96),
            createLine("10 m² in sqmi", sortOrder = 97),
            createLine("10 square mile in m²", sortOrder = 98),
            createLine("10 m² in square mile", sortOrder = 99),
            createLine("10 square miles in m²", sortOrder = 100),
            createLine("10 m² in square miles", sortOrder = 101),
            createLine("10 ac in m²", sortOrder = 102),
            createLine("10 m² in ac", sortOrder = 103),
            createLine("10 acre in m²", sortOrder = 104),
            createLine("10 m² in acre", sortOrder = 105),
            createLine("10 acres in m²", sortOrder = 106),
            createLine("10 m² in acres", sortOrder = 107),
            createLine("10 ha in m²", sortOrder = 108),
            createLine("10 m² in ha", sortOrder = 109),
            createLine("10 hectare in m²", sortOrder = 110),
            createLine("10 m² in hectare", sortOrder = 111),
            createLine("10 hectares in m²", sortOrder = 112),
            createLine("10 m² in hectares", sortOrder = 113),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("nm² to m²", 1.0E-17, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("m² to nm²", 1.0E19, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("nm2 to m²", 1.0E-17, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("m² to nm2", 1.0E19, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("sqnm to m²", 1.0E-17, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("m² to sqnm", 1.0E19, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("square nanometer to m²", 1.0E-17, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("m² to square nanometer", 1.0E19, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("square nanometers to m²", 1.0E-17, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("m² to square nanometers", 1.0E19, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("µm² to m²", 1.0E-11, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("m² to µm²", 1.0E13, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("µm2 to m²", 1.0E-11, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("m² to µm2", 1.0E13, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("um2 to m²", 1.0E-11, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("m² to um2", 1.0E13, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("squm to m²", 1.0E-11, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("m² to squm", 1.0E13, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("square micrometer to m²", 1.0E-11, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("m² to square micrometer", 1.0E13, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("square micrometers to m²", 1.0E-11, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("m² to square micrometers", 1.0E13, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("mm² to m²", 9.999999999999999E-6, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("m² to mm²", 1.0E7, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("mm2 to m²", 9.999999999999999E-6, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("m² to mm2", 1.0E7, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("sqmm to m²", 9.999999999999999E-6, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("m² to sqmm", 1.0E7, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("square millimeter to m²", 9.999999999999999E-6, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("m² to square millimeter", 1.0E7, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("square millimeters to m²", 9.999999999999999E-6, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("m² to square millimeters", 1.0E7, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("cm² to m²", 0.001, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("m² to cm²", 100000.0, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("cm2 to m²", 0.001, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("m² to cm2", 100000.0, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("sqcm to m²", 0.001, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("m² to sqcm", 100000.0, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("square centimeter to m²", 0.001, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("m² to square centimeter", 100000.0, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("square centimeters to m²", 0.001, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("m² to square centimeters", 100000.0, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("m² to m²", 10.0, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("m² to m²", 10.0, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("m2 to m²", 10.0, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("m² to m2", 10.0, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("sqm to m²", 10.0, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("m² to sqm", 10.0, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("square meter to m²", 10.0, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("m² to square meter", 10.0, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("square meters to m²", 10.0, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("m² to square meters", 10.0, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("km² to m²", 1.0E7, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("m² to km²", 1.0E-5, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("km2 to m²", 1.0E7, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("m² to km2", 1.0E-5, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("sqkm to m²", 1.0E7, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("m² to sqkm", 1.0E-5, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("square kilometer to m²", 1.0E7, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("m² to square kilometer", 1.0E-5, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("square kilometers to m²", 1.0E7, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("m² to square kilometers", 1.0E-5, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("in² to m²", 0.0064516, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("m² to in²", 15500.031000062001, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("in2 to m²", 0.0064516, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("m² to in2", 15500.031000062001, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("sqin to m²", 0.0064516, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("m² to sqin", 15500.031000062001, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("square inch to m²", 0.0064516, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("m² to square inch", 15500.031000062001, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("square inches to m²", 0.0064516, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("m² to square inches", 15500.031000062001, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("ft² to m²", 0.9290304, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("m² to ft²", 107.63910416709722, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("ft2 to m²", 0.9290304, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("m² to ft2", 107.63910416709722, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("sqft to m²", 0.9290304, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("m² to sqft", 107.63910416709722, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("square foot to m²", 0.9290304, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("m² to square foot", 107.63910416709722, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("square feet to m²", 0.9290304, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("m² to square feet", 107.63910416709722, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("yd² to m²", 8.3612736, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("m² to yd²", 11.959900463010802, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("yd2 to m²", 8.3612736, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("m² to yd2", 11.959900463010802, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("sqyd to m²", 8.3612736, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("m² to sqyd", 11.959900463010802, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("square yard to m²", 8.3612736, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("m² to square yard", 11.959900463010802, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("square yards to m²", 8.3612736, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("m² to square yards", 11.959900463010802, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("mi² to m²", 2.589988110336E7, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("m² to mi²", 3.861021585424458E-6, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("mi2 to m²", 2.589988110336E7, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("m² to mi2", 3.861021585424458E-6, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("sqmi to m²", 2.589988110336E7, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("m² to sqmi", 3.861021585424458E-6, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("square mile to m²", 2.589988110336E7, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("m² to square mile", 3.861021585424458E-6, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("square miles to m²", 2.589988110336E7, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("m² to square miles", 3.861021585424458E-6, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("ac to m²", 40468.564224, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("m² to ac", 0.0024710538146716535, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("acre to m²", 40468.564224, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("m² to acre", 0.0024710538146716535, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("acres to m²", 40468.564224, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("m² to acres", 0.0024710538146716535, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("ha to m²", 100000.0, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("m² to ha", 0.001, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("hectare to m²", 100000.0, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("m² to hectare", 0.001, val111!!, 0.05)
        val val112 = result[112].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val112)
        assertEquals("hectares to m²", 100000.0, val112!!, 0.05)
        val val113 = result[113].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val113)
        assertEquals("m² to hectares", 0.001, val113!!, 0.05)
    }

    @Test
    fun `unit conversion static volume`() = runBlocking {
        val lines = listOf(
            createLine("10 mL in L", sortOrder = 0),
            createLine("10 L in mL", sortOrder = 1),
            createLine("10 ml in L", sortOrder = 2),
            createLine("10 L in ml", sortOrder = 3),
            createLine("10 milliliter in L", sortOrder = 4),
            createLine("10 L in milliliter", sortOrder = 5),
            createLine("10 milliliters in L", sortOrder = 6),
            createLine("10 L in milliliters", sortOrder = 7),
            createLine("10 L in L", sortOrder = 8),
            createLine("10 L in L", sortOrder = 9),
            createLine("10 l in L", sortOrder = 10),
            createLine("10 L in l", sortOrder = 11),
            createLine("10 liter in L", sortOrder = 12),
            createLine("10 L in liter", sortOrder = 13),
            createLine("10 liters in L", sortOrder = 14),
            createLine("10 L in liters", sortOrder = 15),
            createLine("10 kL in L", sortOrder = 16),
            createLine("10 L in kL", sortOrder = 17),
            createLine("10 kl in L", sortOrder = 18),
            createLine("10 L in kl", sortOrder = 19),
            createLine("10 kiloliter in L", sortOrder = 20),
            createLine("10 L in kiloliter", sortOrder = 21),
            createLine("10 kiloliters in L", sortOrder = 22),
            createLine("10 L in kiloliters", sortOrder = 23),
            createLine("10 ML in L", sortOrder = 24),
            createLine("10 L in ML", sortOrder = 25),
            createLine("10 megaliter in L", sortOrder = 26),
            createLine("10 L in megaliter", sortOrder = 27),
            createLine("10 megaliters in L", sortOrder = 28),
            createLine("10 L in megaliters", sortOrder = 29),
            createLine("10 cm³ in L", sortOrder = 30),
            createLine("10 L in cm³", sortOrder = 31),
            createLine("10 cm3 in L", sortOrder = 32),
            createLine("10 L in cm3", sortOrder = 33),
            createLine("10 cc in L", sortOrder = 34),
            createLine("10 L in cc", sortOrder = 35),
            createLine("10 cubic centimeter in L", sortOrder = 36),
            createLine("10 L in cubic centimeter", sortOrder = 37),
            createLine("10 cubic centimeters in L", sortOrder = 38),
            createLine("10 L in cubic centimeters", sortOrder = 39),
            createLine("10 m³ in L", sortOrder = 40),
            createLine("10 L in m³", sortOrder = 41),
            createLine("10 m3 in L", sortOrder = 42),
            createLine("10 L in m3", sortOrder = 43),
            createLine("10 cubic meter in L", sortOrder = 44),
            createLine("10 L in cubic meter", sortOrder = 45),
            createLine("10 cubic meters in L", sortOrder = 46),
            createLine("10 L in cubic meters", sortOrder = 47),
            createLine("10 dL in L", sortOrder = 48),
            createLine("10 L in dL", sortOrder = 49),
            createLine("10 dl in L", sortOrder = 50),
            createLine("10 L in dl", sortOrder = 51),
            createLine("10 deciliter in L", sortOrder = 52),
            createLine("10 L in deciliter", sortOrder = 53),
            createLine("10 deciliters in L", sortOrder = 54),
            createLine("10 L in deciliters", sortOrder = 55),
            createLine("10 cL in L", sortOrder = 56),
            createLine("10 L in cL", sortOrder = 57),
            createLine("10 cl in L", sortOrder = 58),
            createLine("10 L in cl", sortOrder = 59),
            createLine("10 centiliter in L", sortOrder = 60),
            createLine("10 L in centiliter", sortOrder = 61),
            createLine("10 centiliters in L", sortOrder = 62),
            createLine("10 L in centiliters", sortOrder = 63),
            createLine("10 µL in L", sortOrder = 64),
            createLine("10 L in µL", sortOrder = 65),
            createLine("10 uL in L", sortOrder = 66),
            createLine("10 L in uL", sortOrder = 67),
            createLine("10 µl in L", sortOrder = 68),
            createLine("10 L in µl", sortOrder = 69),
            createLine("10 ul in L", sortOrder = 70),
            createLine("10 L in ul", sortOrder = 71),
            createLine("10 microliter in L", sortOrder = 72),
            createLine("10 L in microliter", sortOrder = 73),
            createLine("10 microliters in L", sortOrder = 74),
            createLine("10 L in microliters", sortOrder = 75),
            createLine("10 mm³ in L", sortOrder = 76),
            createLine("10 L in mm³", sortOrder = 77),
            createLine("10 mm3 in L", sortOrder = 78),
            createLine("10 L in mm3", sortOrder = 79),
            createLine("10 cubic millimeter in L", sortOrder = 80),
            createLine("10 L in cubic millimeter", sortOrder = 81),
            createLine("10 cubic millimeters in L", sortOrder = 82),
            createLine("10 L in cubic millimeters", sortOrder = 83),
            createLine("10 gal in L", sortOrder = 84),
            createLine("10 L in gal", sortOrder = 85),
            createLine("10 gallon in L", sortOrder = 86),
            createLine("10 L in gallon", sortOrder = 87),
            createLine("10 gallons in L", sortOrder = 88),
            createLine("10 L in gallons", sortOrder = 89),
            createLine("10 US gallon in L", sortOrder = 90),
            createLine("10 L in US gallon", sortOrder = 91),
            createLine("10 US gallons in L", sortOrder = 92),
            createLine("10 L in US gallons", sortOrder = 93),
            createLine("10 qt in L", sortOrder = 94),
            createLine("10 L in qt", sortOrder = 95),
            createLine("10 quart in L", sortOrder = 96),
            createLine("10 L in quart", sortOrder = 97),
            createLine("10 quarts in L", sortOrder = 98),
            createLine("10 L in quarts", sortOrder = 99),
            createLine("10 US quarts in L", sortOrder = 100),
            createLine("10 L in US quarts", sortOrder = 101),
            createLine("10 pint in L", sortOrder = 102),
            createLine("10 L in pint", sortOrder = 103),
            createLine("10 pints in L", sortOrder = 104),
            createLine("10 L in pints", sortOrder = 105),
            createLine("10 US pints in L", sortOrder = 106),
            createLine("10 L in US pints", sortOrder = 107),
            createLine("10 cup in L", sortOrder = 108),
            createLine("10 L in cup", sortOrder = 109),
            createLine("10 cups in L", sortOrder = 110),
            createLine("10 L in cups", sortOrder = 111),
            createLine("10 US cups in L", sortOrder = 112),
            createLine("10 L in US cups", sortOrder = 113),
            createLine("10 fl oz in L", sortOrder = 114),
            createLine("10 L in fl oz", sortOrder = 115),
            createLine("10 floz in L", sortOrder = 116),
            createLine("10 L in floz", sortOrder = 117),
            createLine("10 fluid ounce in L", sortOrder = 118),
            createLine("10 L in fluid ounce", sortOrder = 119),
            createLine("10 fluid ounces in L", sortOrder = 120),
            createLine("10 L in fluid ounces", sortOrder = 121),
            createLine("10 US fluid ounces in L", sortOrder = 122),
            createLine("10 L in US fluid ounces", sortOrder = 123),
            createLine("10 gal_imp in L", sortOrder = 124),
            createLine("10 L in gal_imp", sortOrder = 125),
            createLine("10 imperial gallon in L", sortOrder = 126),
            createLine("10 L in imperial gallon", sortOrder = 127),
            createLine("10 imperial gallons in L", sortOrder = 128),
            createLine("10 L in imperial gallons", sortOrder = 129),
            createLine("10 qt_imp in L", sortOrder = 130),
            createLine("10 L in qt_imp", sortOrder = 131),
            createLine("10 imperial quart in L", sortOrder = 132),
            createLine("10 L in imperial quart", sortOrder = 133),
            createLine("10 imperial quarts in L", sortOrder = 134),
            createLine("10 L in imperial quarts", sortOrder = 135),
            createLine("10 pint_imp in L", sortOrder = 136),
            createLine("10 L in pint_imp", sortOrder = 137),
            createLine("10 imperial pint in L", sortOrder = 138),
            createLine("10 L in imperial pint", sortOrder = 139),
            createLine("10 imperial pints in L", sortOrder = 140),
            createLine("10 L in imperial pints", sortOrder = 141),
            createLine("10 fl_oz_imp in L", sortOrder = 142),
            createLine("10 L in fl_oz_imp", sortOrder = 143),
            createLine("10 imperial fluid ounce in L", sortOrder = 144),
            createLine("10 L in imperial fluid ounce", sortOrder = 145),
            createLine("10 imperial fluid ounces in L", sortOrder = 146),
            createLine("10 L in imperial fluid ounces", sortOrder = 147),
            createLine("10 gi_us in L", sortOrder = 148),
            createLine("10 L in gi_us", sortOrder = 149),
            createLine("10 US gill in L", sortOrder = 150),
            createLine("10 L in US gill", sortOrder = 151),
            createLine("10 US gills in L", sortOrder = 152),
            createLine("10 L in US gills", sortOrder = 153),
            createLine("10 gi_imp in L", sortOrder = 154),
            createLine("10 L in gi_imp", sortOrder = 155),
            createLine("10 imperial gill in L", sortOrder = 156),
            createLine("10 L in imperial gill", sortOrder = 157),
            createLine("10 imperial gills in L", sortOrder = 158),
            createLine("10 L in imperial gills", sortOrder = 159),
            createLine("10 tbsp in L", sortOrder = 160),
            createLine("10 L in tbsp", sortOrder = 161),
            createLine("10 tablespoon in L", sortOrder = 162),
            createLine("10 L in tablespoon", sortOrder = 163),
            createLine("10 tablespoons in L", sortOrder = 164),
            createLine("10 L in tablespoons", sortOrder = 165),
            createLine("10 tsp in L", sortOrder = 166),
            createLine("10 L in tsp", sortOrder = 167),
            createLine("10 teaspoon in L", sortOrder = 168),
            createLine("10 L in teaspoon", sortOrder = 169),
            createLine("10 teaspoons in L", sortOrder = 170),
            createLine("10 L in teaspoons", sortOrder = 171),
            createLine("10 in³ in L", sortOrder = 172),
            createLine("10 L in in³", sortOrder = 173),
            createLine("10 in3 in L", sortOrder = 174),
            createLine("10 L in in3", sortOrder = 175),
            createLine("10 cubic inch in L", sortOrder = 176),
            createLine("10 L in cubic inch", sortOrder = 177),
            createLine("10 cubic inches in L", sortOrder = 178),
            createLine("10 L in cubic inches", sortOrder = 179),
            createLine("10 ft³ in L", sortOrder = 180),
            createLine("10 L in ft³", sortOrder = 181),
            createLine("10 ft3 in L", sortOrder = 182),
            createLine("10 L in ft3", sortOrder = 183),
            createLine("10 cuft in L", sortOrder = 184),
            createLine("10 L in cuft", sortOrder = 185),
            createLine("10 cubic foot in L", sortOrder = 186),
            createLine("10 L in cubic foot", sortOrder = 187),
            createLine("10 cubic feet in L", sortOrder = 188),
            createLine("10 L in cubic feet", sortOrder = 189),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("mL to L", 0.01, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("L to mL", 10000.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("ml to L", 0.01, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("L to ml", 10000.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("milliliter to L", 0.01, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("L to milliliter", 10000.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("milliliters to L", 0.01, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("L to milliliters", 10000.0, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("L to L", 10.0, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("L to L", 10.0, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("l to L", 10.0, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("L to l", 10.0, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("liter to L", 10.0, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("L to liter", 10.0, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("liters to L", 10.0, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("L to liters", 10.0, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("kL to L", 10000.0, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("L to kL", 0.01, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("kl to L", 10000.0, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("L to kl", 0.01, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("kiloliter to L", 10000.0, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("L to kiloliter", 0.01, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("kiloliters to L", 10000.0, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("L to kiloliters", 0.01, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("ML to L", 1.0E7, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("L to ML", 1.0E-5, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("megaliter to L", 1.0E7, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("L to megaliter", 1.0E-5, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("megaliters to L", 1.0E7, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("L to megaliters", 1.0E-5, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("cm³ to L", 0.01, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("L to cm³", 10000.0, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("cm3 to L", 0.01, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("L to cm3", 10000.0, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("cc to L", 0.01, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("L to cc", 10000.0, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("cubic centimeter to L", 0.01, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("L to cubic centimeter", 10000.0, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("cubic centimeters to L", 0.01, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("L to cubic centimeters", 10000.0, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("m³ to L", 10000.0, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("L to m³", 0.01, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("m3 to L", 10000.0, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("L to m3", 0.01, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("cubic meter to L", 10000.0, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("L to cubic meter", 0.01, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("cubic meters to L", 10000.0, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("L to cubic meters", 0.01, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("dL to L", 1.0, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("L to dL", 100.0, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("dl to L", 1.0, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("L to dl", 100.0, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("deciliter to L", 1.0, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("L to deciliter", 100.0, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("deciliters to L", 1.0, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("L to deciliters", 100.0, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("cL to L", 0.1, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("L to cL", 1000.0, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("cl to L", 0.1, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("L to cl", 1000.0, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("centiliter to L", 0.1, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("L to centiliter", 1000.0, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("centiliters to L", 0.1, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("L to centiliters", 1000.0, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("µL to L", 9.999999999999999E-6, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("L to µL", 1.0E7, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("uL to L", 9.999999999999999E-6, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("L to uL", 1.0E7, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("µl to L", 9.999999999999999E-6, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("L to µl", 1.0E7, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("ul to L", 9.999999999999999E-6, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("L to ul", 1.0E7, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("microliter to L", 9.999999999999999E-6, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("L to microliter", 1.0E7, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("microliters to L", 9.999999999999999E-6, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("L to microliters", 1.0E7, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("mm³ to L", 9.999999999999999E-6, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("L to mm³", 1.0E7, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("mm3 to L", 9.999999999999999E-6, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("L to mm3", 1.0E7, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("cubic millimeter to L", 9.999999999999999E-6, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("L to cubic millimeter", 1.0E7, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("cubic millimeters to L", 9.999999999999999E-6, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("L to cubic millimeters", 1.0E7, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("gal to L", 37.85411784, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("L to gal", 2.6417205235814842, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("gallon to L", 37.85411784, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("L to gallon", 2.6417205235814842, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("gallons to L", 37.85411784, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("L to gallons", 2.6417205235814842, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("US gallon to L", 37.85411784, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("L to US gallon", 2.6417205235814842, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("US gallons to L", 37.85411784, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("L to US gallons", 2.6417205235814842, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("qt to L", 9.46352946, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("L to qt", 10.566882094325937, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("quart to L", 9.46352946, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("L to quart", 10.566882094325937, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("quarts to L", 9.46352946, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("L to quarts", 10.566882094325937, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("US quarts to L", 9.46352946, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("L to US quarts", 10.566882094325937, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("pint to L", 4.73176473, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("L to pint", 21.133764188651874, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("pints to L", 4.73176473, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("L to pints", 21.133764188651874, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("US pints to L", 4.73176473, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("L to US pints", 21.133764188651874, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("cup to L", 2.365882365, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("L to cup", 42.26752837730375, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("cups to L", 2.365882365, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("L to cups", 42.26752837730375, val111!!, 0.05)
        val val112 = result[112].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val112)
        assertEquals("US cups to L", 2.365882365, val112!!, 0.05)
        val val113 = result[113].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val113)
        assertEquals("L to US cups", 42.26752837730375, val113!!, 0.05)
        val val114 = result[114].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val114)
        assertEquals("fl oz to L", 0.295735295625, val114!!, 0.05)
        val val115 = result[115].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val115)
        assertEquals("L to fl oz", 338.14022701843, val115!!, 0.05)
        val val116 = result[116].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val116)
        assertEquals("floz to L", 0.295735295625, val116!!, 0.05)
        val val117 = result[117].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val117)
        assertEquals("L to floz", 338.14022701843, val117!!, 0.05)
        val val118 = result[118].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val118)
        assertEquals("fluid ounce to L", 0.295735295625, val118!!, 0.05)
        val val119 = result[119].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val119)
        assertEquals("L to fluid ounce", 338.14022701843, val119!!, 0.05)
        val val120 = result[120].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val120)
        assertEquals("fluid ounces to L", 0.295735295625, val120!!, 0.05)
        val val121 = result[121].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val121)
        assertEquals("L to fluid ounces", 338.14022701843, val121!!, 0.05)
        val val122 = result[122].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val122)
        assertEquals("US fluid ounces to L", 0.295735295625, val122!!, 0.05)
        val val123 = result[123].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val123)
        assertEquals("L to US fluid ounces", 338.14022701843, val123!!, 0.05)
        val val124 = result[124].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val124)
        assertEquals("gal_imp to L", 45.4609, val124!!, 0.05)
        val val125 = result[125].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val125)
        assertEquals("L to gal_imp", 2.1996924829908777, val125!!, 0.05)
        val val126 = result[126].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val126)
        assertEquals("imperial gallon to L", 45.4609, val126!!, 0.05)
        val val127 = result[127].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val127)
        assertEquals("L to imperial gallon", 2.1996924829908777, val127!!, 0.05)
        val val128 = result[128].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val128)
        assertEquals("imperial gallons to L", 45.4609, val128!!, 0.05)
        val val129 = result[129].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val129)
        assertEquals("L to imperial gallons", 2.1996924829908777, val129!!, 0.05)
        val val130 = result[130].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val130)
        assertEquals("qt_imp to L", 11.365225, val130!!, 0.05)
        val val131 = result[131].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val131)
        assertEquals("L to qt_imp", 8.79876993196351, val131!!, 0.05)
        val val132 = result[132].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val132)
        assertEquals("imperial quart to L", 11.365225, val132!!, 0.05)
        val val133 = result[133].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val133)
        assertEquals("L to imperial quart", 8.79876993196351, val133!!, 0.05)
        val val134 = result[134].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val134)
        assertEquals("imperial quarts to L", 11.365225, val134!!, 0.05)
        val val135 = result[135].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val135)
        assertEquals("L to imperial quarts", 8.79876993196351, val135!!, 0.05)
        val val136 = result[136].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val136)
        assertEquals("pint_imp to L", 5.6826125, val136!!, 0.05)
        val val137 = result[137].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val137)
        assertEquals("L to pint_imp", 17.59753986392702, val137!!, 0.05)
        val val138 = result[138].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val138)
        assertEquals("imperial pint to L", 5.6826125, val138!!, 0.05)
        val val139 = result[139].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val139)
        assertEquals("L to imperial pint", 17.59753986392702, val139!!, 0.05)
        val val140 = result[140].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val140)
        assertEquals("imperial pints to L", 5.6826125, val140!!, 0.05)
        val val141 = result[141].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val141)
        assertEquals("L to imperial pints", 17.59753986392702, val141!!, 0.05)
        val val142 = result[142].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val142)
        assertEquals("fl_oz_imp to L", 0.28413062499999997, val142!!, 0.05)
        val val143 = result[143].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val143)
        assertEquals("L to fl_oz_imp", 351.95079727854045, val143!!, 0.05)
        val val144 = result[144].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val144)
        assertEquals("imperial fluid ounce to L", 0.28413062499999997, val144!!, 0.05)
        val val145 = result[145].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val145)
        assertEquals("L to imperial fluid ounce", 351.95079727854045, val145!!, 0.05)
        val val146 = result[146].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val146)
        assertEquals("imperial fluid ounces to L", 0.28413062499999997, val146!!, 0.05)
        val val147 = result[147].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val147)
        assertEquals("L to imperial fluid ounces", 351.95079727854045, val147!!, 0.05)
        val val148 = result[148].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val148)
        assertEquals("gi_us to L", 1.1829411825, val148!!, 0.05)
        val val149 = result[149].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val149)
        assertEquals("L to gi_us", 84.5350567546075, val149!!, 0.05)
        val val150 = result[150].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val150)
        assertEquals("US gill to L", 1.1829411825, val150!!, 0.05)
        val val151 = result[151].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val151)
        assertEquals("L to US gill", 84.5350567546075, val151!!, 0.05)
        val val152 = result[152].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val152)
        assertEquals("US gills to L", 1.1829411825, val152!!, 0.05)
        val val153 = result[153].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val153)
        assertEquals("L to US gills", 84.5350567546075, val153!!, 0.05)
        val val154 = result[154].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val154)
        assertEquals("gi_imp to L", 1.420653125, val154!!, 0.05)
        val val155 = result[155].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val155)
        assertEquals("L to gi_imp", 70.39015945570809, val155!!, 0.05)
        val val156 = result[156].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val156)
        assertEquals("imperial gill to L", 1.420653125, val156!!, 0.05)
        val val157 = result[157].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val157)
        assertEquals("L to imperial gill", 70.39015945570809, val157!!, 0.05)
        val val158 = result[158].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val158)
        assertEquals("imperial gills to L", 1.420653125, val158!!, 0.05)
        val val159 = result[159].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val159)
        assertEquals("L to imperial gills", 70.39015945570809, val159!!, 0.05)
        val val160 = result[160].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val160)
        assertEquals("tbsp to L", 0.1478676478125, val160!!, 0.05)
        val val161 = result[161].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val161)
        assertEquals("L to tbsp", 676.28045403686, val161!!, 0.05)
        val val162 = result[162].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val162)
        assertEquals("tablespoon to L", 0.1478676478125, val162!!, 0.05)
        val val163 = result[163].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val163)
        assertEquals("L to tablespoon", 676.28045403686, val163!!, 0.05)
        val val164 = result[164].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val164)
        assertEquals("tablespoons to L", 0.1478676478125, val164!!, 0.05)
        val val165 = result[165].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val165)
        assertEquals("L to tablespoons", 676.28045403686, val165!!, 0.05)
        val val166 = result[166].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val166)
        assertEquals("tsp to L", 0.0492892159375, val166!!, 0.05)
        val val167 = result[167].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val167)
        assertEquals("L to tsp", 2028.84136211058, val167!!, 0.05)
        val val168 = result[168].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val168)
        assertEquals("teaspoon to L", 0.0492892159375, val168!!, 0.05)
        val val169 = result[169].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val169)
        assertEquals("L to teaspoon", 2028.84136211058, val169!!, 0.05)
        val val170 = result[170].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val170)
        assertEquals("teaspoons to L", 0.0492892159375, val170!!, 0.05)
        val val171 = result[171].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val171)
        assertEquals("L to teaspoons", 2028.84136211058, val171!!, 0.05)
        val val172 = result[172].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val172)
        assertEquals("in³ to L", 0.16387064, val172!!, 0.05)
        val val173 = result[173].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val173)
        assertEquals("L to in³", 610.2374409473229, val173!!, 0.05)
        val val174 = result[174].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val174)
        assertEquals("in3 to L", 0.16387064, val174!!, 0.05)
        val val175 = result[175].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val175)
        assertEquals("L to in3", 610.2374409473229, val175!!, 0.05)
        val val176 = result[176].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val176)
        assertEquals("cubic inch to L", 0.16387064, val176!!, 0.05)
        val val177 = result[177].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val177)
        assertEquals("L to cubic inch", 610.2374409473229, val177!!, 0.05)
        val val178 = result[178].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val178)
        assertEquals("cubic inches to L", 0.16387064, val178!!, 0.05)
        val val179 = result[179].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val179)
        assertEquals("L to cubic inches", 610.2374409473229, val179!!, 0.05)
        val val180 = result[180].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val180)
        assertEquals("ft³ to L", 283.16846592, val180!!, 0.05)
        val val181 = result[181].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val181)
        assertEquals("L to ft³", 0.3531466672148859, val181!!, 0.05)
        val val182 = result[182].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val182)
        assertEquals("ft3 to L", 283.16846592, val182!!, 0.05)
        val val183 = result[183].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val183)
        assertEquals("L to ft3", 0.3531466672148859, val183!!, 0.05)
        val val184 = result[184].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val184)
        assertEquals("cuft to L", 283.16846592, val184!!, 0.05)
        val val185 = result[185].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val185)
        assertEquals("L to cuft", 0.3531466672148859, val185!!, 0.05)
        val val186 = result[186].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val186)
        assertEquals("cubic foot to L", 283.16846592, val186!!, 0.05)
        val val187 = result[187].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val187)
        assertEquals("L to cubic foot", 0.3531466672148859, val187!!, 0.05)
        val val188 = result[188].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val188)
        assertEquals("cubic feet to L", 283.16846592, val188!!, 0.05)
        val val189 = result[189].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val189)
        assertEquals("L to cubic feet", 0.3531466672148859, val189!!, 0.05)
    }

    @Test
    fun `unit conversion static mass`() = runBlocking {
        val lines = listOf(
            createLine("10 ng in g", sortOrder = 0),
            createLine("10 g in ng", sortOrder = 1),
            createLine("10 nanogram in g", sortOrder = 2),
            createLine("10 g in nanogram", sortOrder = 3),
            createLine("10 nanograms in g", sortOrder = 4),
            createLine("10 g in nanograms", sortOrder = 5),
            createLine("10 mcg in g", sortOrder = 6),
            createLine("10 g in mcg", sortOrder = 7),
            createLine("10 µg in g", sortOrder = 8),
            createLine("10 g in µg", sortOrder = 9),
            createLine("10 ug in g", sortOrder = 10),
            createLine("10 g in ug", sortOrder = 11),
            createLine("10 microgram in g", sortOrder = 12),
            createLine("10 g in microgram", sortOrder = 13),
            createLine("10 micrograms in g", sortOrder = 14),
            createLine("10 g in micrograms", sortOrder = 15),
            createLine("10 mg in g", sortOrder = 16),
            createLine("10 g in mg", sortOrder = 17),
            createLine("10 milligram in g", sortOrder = 18),
            createLine("10 g in milligram", sortOrder = 19),
            createLine("10 milligrams in g", sortOrder = 20),
            createLine("10 g in milligrams", sortOrder = 21),
            createLine("10 g in g", sortOrder = 22),
            createLine("10 g in g", sortOrder = 23),
            createLine("10 gram in g", sortOrder = 24),
            createLine("10 g in gram", sortOrder = 25),
            createLine("10 grams in g", sortOrder = 26),
            createLine("10 g in grams", sortOrder = 27),
            createLine("10 kg in g", sortOrder = 28),
            createLine("10 g in kg", sortOrder = 29),
            createLine("10 kgs in g", sortOrder = 30),
            createLine("10 g in kgs", sortOrder = 31),
            createLine("10 kilograms in g", sortOrder = 32),
            createLine("10 g in kilograms", sortOrder = 33),
            createLine("10 t in g", sortOrder = 34),
            createLine("10 g in t", sortOrder = 35),
            createLine("10 tonne in g", sortOrder = 36),
            createLine("10 g in tonne", sortOrder = 37),
            createLine("10 tonnes in g", sortOrder = 38),
            createLine("10 g in tonnes", sortOrder = 39),
            createLine("10 ton in g", sortOrder = 40),
            createLine("10 g in ton", sortOrder = 41),
            createLine("10 tons in g", sortOrder = 42),
            createLine("10 g in tons", sortOrder = 43),
            createLine("10 metric ton in g", sortOrder = 44),
            createLine("10 g in metric ton", sortOrder = 45),
            createLine("10 metric tons in g", sortOrder = 46),
            createLine("10 g in metric tons", sortOrder = 47),
            createLine("10 metric tonne in g", sortOrder = 48),
            createLine("10 g in metric tonne", sortOrder = 49),
            createLine("10 metric tonnes in g", sortOrder = 50),
            createLine("10 g in metric tonnes", sortOrder = 51),
            createLine("10 oz in g", sortOrder = 52),
            createLine("10 g in oz", sortOrder = 53),
            createLine("10 ounce in g", sortOrder = 54),
            createLine("10 g in ounce", sortOrder = 55),
            createLine("10 ounces in g", sortOrder = 56),
            createLine("10 g in ounces", sortOrder = 57),
            createLine("10 lb in g", sortOrder = 58),
            createLine("10 g in lb", sortOrder = 59),
            createLine("10 lbs in g", sortOrder = 60),
            createLine("10 g in lbs", sortOrder = 61),
            createLine("10 pound in g", sortOrder = 62),
            createLine("10 g in pound", sortOrder = 63),
            createLine("10 pounds in g", sortOrder = 64),
            createLine("10 g in pounds", sortOrder = 65),
            createLine("10 st in g", sortOrder = 66),
            createLine("10 g in st", sortOrder = 67),
            createLine("10 stone in g", sortOrder = 68),
            createLine("10 g in stone", sortOrder = 69),
            createLine("10 stones in g", sortOrder = 70),
            createLine("10 g in stones", sortOrder = 71),
            createLine("10 sh ton in g", sortOrder = 72),
            createLine("10 g in sh ton", sortOrder = 73),
            createLine("10 short ton in g", sortOrder = 74),
            createLine("10 g in short ton", sortOrder = 75),
            createLine("10 short tons in g", sortOrder = 76),
            createLine("10 g in short tons", sortOrder = 77),
            createLine("10 ozt in g", sortOrder = 78),
            createLine("10 g in ozt", sortOrder = 79),
            createLine("10 oz t in g", sortOrder = 80),
            createLine("10 g in oz t", sortOrder = 81),
            createLine("10 troy ounce in g", sortOrder = 82),
            createLine("10 g in troy ounce", sortOrder = 83),
            createLine("10 troy ounces in g", sortOrder = 84),
            createLine("10 g in troy ounces", sortOrder = 85),
            createLine("10 ct in g", sortOrder = 86),
            createLine("10 g in ct", sortOrder = 87),
            createLine("10 carat in g", sortOrder = 88),
            createLine("10 g in carat", sortOrder = 89),
            createLine("10 carats in g", sortOrder = 90),
            createLine("10 g in carats", sortOrder = 91),
            createLine("10 hg in g", sortOrder = 92),
            createLine("10 g in hg", sortOrder = 93),
            createLine("10 ettogram in g", sortOrder = 94),
            createLine("10 g in ettogram", sortOrder = 95),
            createLine("10 ettograms in g", sortOrder = 96),
            createLine("10 g in ettograms", sortOrder = 97),
            createLine("10 cg in g", sortOrder = 98),
            createLine("10 g in cg", sortOrder = 99),
            createLine("10 centigram in g", sortOrder = 100),
            createLine("10 g in centigram", sortOrder = 101),
            createLine("10 centigrams in g", sortOrder = 102),
            createLine("10 g in centigrams", sortOrder = 103),
            createLine("10 q in g", sortOrder = 104),
            createLine("10 g in q", sortOrder = 105),
            createLine("10 quintal in g", sortOrder = 106),
            createLine("10 g in quintal", sortOrder = 107),
            createLine("10 quintals in g", sortOrder = 108),
            createLine("10 g in quintals", sortOrder = 109),
            createLine("10 dwt in g", sortOrder = 110),
            createLine("10 g in dwt", sortOrder = 111),
            createLine("10 pennyweight in g", sortOrder = 112),
            createLine("10 g in pennyweight", sortOrder = 113),
            createLine("10 u in g", sortOrder = 114),
            createLine("10 g in u", sortOrder = 115),
            createLine("10 amu in g", sortOrder = 116),
            createLine("10 g in amu", sortOrder = 117),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("ng to g", 1.0E-8, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("g to ng", 1.0E10, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("nanogram to g", 1.0E-8, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("g to nanogram", 1.0E10, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("nanograms to g", 1.0E-8, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("g to nanograms", 1.0E10, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("mcg to g", 9.999999999999999E-6, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("g to mcg", 1.0E7, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("µg to g", 9.999999999999999E-6, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("g to µg", 1.0E7, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("ug to g", 9.999999999999999E-6, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("g to ug", 1.0E7, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("microgram to g", 9.999999999999999E-6, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("g to microgram", 1.0E7, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("micrograms to g", 9.999999999999999E-6, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("g to micrograms", 1.0E7, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("mg to g", 0.01, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("g to mg", 10000.0, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("milligram to g", 0.01, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("g to milligram", 10000.0, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("milligrams to g", 0.01, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("g to milligrams", 10000.0, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("g to g", 10.0, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("g to g", 10.0, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("gram to g", 10.0, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("g to gram", 10.0, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("grams to g", 10.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("g to grams", 10.0, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("kg to g", 10000.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("g to kg", 0.01, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("kgs to g", 10000.0, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("g to kgs", 0.01, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("kilograms to g", 10000.0, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("g to kilograms", 0.01, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("t to g", 1.0E7, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("g to t", 1.0E-5, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("tonne to g", 1.0E7, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("g to tonne", 1.0E-5, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("tonnes to g", 1.0E7, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("g to tonnes", 1.0E-5, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("ton to g", 1.0E7, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("g to ton", 1.0E-5, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("tons to g", 1.0E7, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("g to tons", 1.0E-5, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("metric ton to g", 1.0E7, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("g to metric ton", 1.0E-5, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("metric tons to g", 1.0E7, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("g to metric tons", 1.0E-5, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("metric tonne to g", 1.0E7, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("g to metric tonne", 1.0E-5, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("metric tonnes to g", 1.0E7, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("g to metric tonnes", 1.0E-5, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("oz to g", 283.49523125, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("g to oz", 0.3527396194958041, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("ounce to g", 283.49523125, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("g to ounce", 0.3527396194958041, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("ounces to g", 283.49523125, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("g to ounces", 0.3527396194958041, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("lb to g", 4535.9237, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("g to lb", 0.022046226218487758, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("lbs to g", 4535.9237, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("g to lbs", 0.022046226218487758, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("pound to g", 4535.9237, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("g to pound", 0.022046226218487758, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("pounds to g", 4535.9237, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("g to pounds", 0.022046226218487758, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("st to g", 63502.9318, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("g to st", 0.001574730444177697, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("stone to g", 63502.9318, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("g to stone", 0.001574730444177697, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("stones to g", 63502.9318, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("g to stones", 0.001574730444177697, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("sh ton to g", 9071847.4, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("g to sh ton", 1.1023113109243879E-5, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("short ton to g", 9071847.4, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("g to short ton", 1.1023113109243879E-5, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("short tons to g", 9071847.4, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("g to short tons", 1.1023113109243879E-5, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("ozt to g", 311.034768, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("g to ozt", 0.3215074656862798, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("oz t to g", 311.034768, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("g to oz t", 0.3215074656862798, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("troy ounce to g", 311.034768, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("g to troy ounce", 0.3215074656862798, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("troy ounces to g", 311.034768, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("g to troy ounces", 0.3215074656862798, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("ct to g", 2.0, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("g to ct", 50.0, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("carat to g", 2.0, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("g to carat", 50.0, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("carats to g", 2.0, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("g to carats", 50.0, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("hg to g", 1000.0, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("g to hg", 0.1, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("ettogram to g", 1000.0, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("g to ettogram", 0.1, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("ettograms to g", 1000.0, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("g to ettograms", 0.1, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("cg to g", 0.1, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("g to cg", 1000.0, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("centigram to g", 0.1, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("g to centigram", 1000.0, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("centigrams to g", 0.1, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("g to centigrams", 1000.0, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("q to g", 1000000.0, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("g to q", 1.0E-4, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("quintal to g", 1000000.0, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("g to quintal", 1.0E-4, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("quintals to g", 1000000.0, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("g to quintals", 1.0E-4, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("dwt to g", 15.5517384, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("g to dwt", 6.430149313725597, val111!!, 0.05)
        val val112 = result[112].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val112)
        assertEquals("pennyweight to g", 15.5517384, val112!!, 0.05)
        val val113 = result[113].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val113)
        assertEquals("g to pennyweight", 6.430149313725597, val113!!, 0.05)
        val val114 = result[114].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val114)
        assertEquals("u to g", 1.6605390666E-23, val114!!, 0.05)
        val val115 = result[115].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val115)
        assertEquals("g to u", 6.022140762081123E24, val115!!, 0.05)
        val val116 = result[116].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val116)
        assertEquals("amu to g", 1.6605390666E-23, val116!!, 0.05)
        val val117 = result[117].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val117)
        assertEquals("g to amu", 6.022140762081123E24, val117!!, 0.05)
    }

    @Test
    fun `unit conversion static speed`() = runBlocking {
        val lines = listOf(
            createLine("10 mps in mps", sortOrder = 0),
            createLine("10 mps in mps", sortOrder = 1),
            createLine("10 meters per second in mps", sortOrder = 2),
            createLine("10 mps in meters per second", sortOrder = 3),
            createLine("10 kmh in mps", sortOrder = 4),
            createLine("10 mps in kmh", sortOrder = 5),
            createLine("10 kph in mps", sortOrder = 6),
            createLine("10 mps in kph", sortOrder = 7),
            createLine("10 kilometers per hour in mps", sortOrder = 8),
            createLine("10 mps in kilometers per hour", sortOrder = 9),
            createLine("10 mph in mps", sortOrder = 10),
            createLine("10 mps in mph", sortOrder = 11),
            createLine("10 miles per hour in mps", sortOrder = 12),
            createLine("10 mps in miles per hour", sortOrder = 13),
            createLine("10 kn in mps", sortOrder = 14),
            createLine("10 mps in kn", sortOrder = 15),
            createLine("10 knot in mps", sortOrder = 16),
            createLine("10 mps in knot", sortOrder = 17),
            createLine("10 knots in mps", sortOrder = 18),
            createLine("10 mps in knots", sortOrder = 19),
            createLine("10 fps in mps", sortOrder = 20),
            createLine("10 mps in fps", sortOrder = 21),
            createLine("10 feet per second in mps", sortOrder = 22),
            createLine("10 mps in feet per second", sortOrder = 23),
            createLine("10 speed of light in mps", sortOrder = 24),
            createLine("10 mps in speed of light", sortOrder = 25),
            createLine("10 speed of light in mps", sortOrder = 26),
            createLine("10 mps in speed of light", sortOrder = 27),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("mps to mps", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("mps to mps", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("meters per second to mps", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("mps to meters per second", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("kmh to mps", 2.7777777777777777, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("mps to kmh", 36.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("kph to mps", 2.7777777777777777, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("mps to kph", 36.0, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("kilometers per hour to mps", 2.7777777777777777, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("mps to kilometers per hour", 36.0, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("mph to mps", 4.4704, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("mps to mph", 22.369362920544024, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("miles per hour to mps", 4.4704, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("mps to miles per hour", 22.369362920544024, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("kn to mps", 5.14444, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("mps to kn", 19.438461717893492, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("knot to mps", 5.14444, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("mps to knot", 19.438461717893492, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("knots to mps", 5.14444, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("mps to knots", 19.438461717893492, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("fps to mps", 3.048, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("mps to fps", 32.808398950131235, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("feet per second to mps", 3.048, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("mps to feet per second", 32.808398950131235, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("speed of light to mps", 2.99792458E9, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("mps to speed of light", 3.3356409519815205E-8, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("speed of light to mps", 2.99792458E9, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("mps to speed of light", 3.3356409519815205E-8, val27!!, 0.05)
    }

    @Test
    fun `unit conversion static angle`() = runBlocking {
        val lines = listOf(
            createLine("10 rad in rad", sortOrder = 0),
            createLine("10 rad in rad", sortOrder = 1),
            createLine("10 radian in rad", sortOrder = 2),
            createLine("10 rad in radian", sortOrder = 3),
            createLine("10 radians in rad", sortOrder = 4),
            createLine("10 rad in radians", sortOrder = 5),
            createLine("10 deg in rad", sortOrder = 6),
            createLine("10 rad in deg", sortOrder = 7),
            createLine("10 degree in rad", sortOrder = 8),
            createLine("10 rad in degree", sortOrder = 9),
            createLine("10 degrees in rad", sortOrder = 10),
            createLine("10 rad in degrees", sortOrder = 11),
            createLine("10 ° in rad", sortOrder = 12),
            createLine("10 rad in °", sortOrder = 13),
            createLine("10 arcmin in rad", sortOrder = 14),
            createLine("10 rad in arcmin", sortOrder = 15),
            createLine("10 minute of arc in rad", sortOrder = 16),
            createLine("10 rad in minute of arc", sortOrder = 17),
            createLine("10 arcsec in rad", sortOrder = 18),
            createLine("10 rad in arcsec", sortOrder = 19),
            createLine("10 second of arc in rad", sortOrder = 20),
            createLine("10 rad in second of arc", sortOrder = 21),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("rad to rad", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("rad to rad", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("radian to rad", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("rad to radian", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("radians to rad", 10.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("rad to radians", 10.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("deg to rad", 0.17453292519943295, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("rad to deg", 572.9577951308232, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("degree to rad", 0.17453292519943295, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("rad to degree", 572.9577951308232, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("degrees to rad", 0.17453292519943295, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("rad to degrees", 572.9577951308232, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("° to rad", 0.17453292519943295, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("rad to °", 572.9577951308232, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("arcmin to rad", 0.002908882086657216, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("rad to arcmin", 34377.46770784939, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("minute of arc to rad", 0.002908882086657216, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("rad to minute of arc", 34377.46770784939, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("arcsec to rad", 4.84813681109536E-5, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("rad to arcsec", 2062648.0624709637, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("second of arc to rad", 4.84813681109536E-5, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("rad to second of arc", 2062648.0624709637, val21!!, 0.05)
    }

    @Test
    fun `unit conversion static temperature`() = runBlocking {
        val lines = listOf(
            createLine("10 °C in °C", sortOrder = 0),
            createLine("10 °C in °C", sortOrder = 1),
            createLine("10 C in °C", sortOrder = 2),
            createLine("10 °C in C", sortOrder = 3),
            createLine("10 celsius in °C", sortOrder = 4),
            createLine("10 °C in celsius", sortOrder = 5),
            createLine("10 degC in °C", sortOrder = 6),
            createLine("10 °C in degC", sortOrder = 7),
            createLine("10 degree celsius in °C", sortOrder = 8),
            createLine("10 °C in degree celsius", sortOrder = 9),
            createLine("10 °F in °C", sortOrder = 10),
            createLine("10 °C in °F", sortOrder = 11),
            createLine("10 F in °C", sortOrder = 12),
            createLine("10 °C in F", sortOrder = 13),
            createLine("10 fahrenheit in °C", sortOrder = 14),
            createLine("10 °C in fahrenheit", sortOrder = 15),
            createLine("10 degF in °C", sortOrder = 16),
            createLine("10 °C in degF", sortOrder = 17),
            createLine("10 degree fahrenheit in °C", sortOrder = 18),
            createLine("10 °C in degree fahrenheit", sortOrder = 19),
            createLine("10 K in °C", sortOrder = 20),
            createLine("10 °C in K", sortOrder = 21),
            createLine("10 kelvin in °C", sortOrder = 22),
            createLine("10 °C in kelvin", sortOrder = 23),
            createLine("10 °Re in °C", sortOrder = 24),
            createLine("10 °C in °Re", sortOrder = 25),
            createLine("10 Re in °C", sortOrder = 26),
            createLine("10 °C in Re", sortOrder = 27),
            createLine("10 reaumur in °C", sortOrder = 28),
            createLine("10 °C in reaumur", sortOrder = 29),
            createLine("10 Réaumur in °C", sortOrder = 30),
            createLine("10 °C in Réaumur", sortOrder = 31),
            createLine("10 °Rø in °C", sortOrder = 32),
            createLine("10 °C in °Rø", sortOrder = 33),
            createLine("10 Rø in °C", sortOrder = 34),
            createLine("10 °C in Rø", sortOrder = 35),
            createLine("10 romer in °C", sortOrder = 36),
            createLine("10 °C in romer", sortOrder = 37),
            createLine("10 Rømer in °C", sortOrder = 38),
            createLine("10 °C in Rømer", sortOrder = 39),
            createLine("10 °De in °C", sortOrder = 40),
            createLine("10 °C in °De", sortOrder = 41),
            createLine("10 De in °C", sortOrder = 42),
            createLine("10 °C in De", sortOrder = 43),
            createLine("10 delisle in °C", sortOrder = 44),
            createLine("10 °C in delisle", sortOrder = 45),
            createLine("10 °Ra in °C", sortOrder = 46),
            createLine("10 °C in °Ra", sortOrder = 47),
            createLine("10 Ra in °C", sortOrder = 48),
            createLine("10 °C in Ra", sortOrder = 49),
            createLine("10 rankine in °C", sortOrder = 50),
            createLine("10 °C in rankine", sortOrder = 51),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("°C to °C", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("°C to °C", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("C to °C", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("°C to C", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("celsius to °C", 10.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("°C to celsius", 10.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("degC to °C", 10.0, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("°C to degC", 10.0, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("degree celsius to °C", 10.0, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("°C to degree celsius", 10.0, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("°F to °C", -12.222222222222229, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("°C to °F", 50.0, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("F to °C", -12.222222222222229, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("°C to F", 50.0, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("fahrenheit to °C", -12.222222222222229, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("°C to fahrenheit", 50.0, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("degF to °C", -12.222222222222229, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("°C to degF", 50.0, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("degree fahrenheit to °C", -12.222222222222229, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("°C to degree fahrenheit", 50.0, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("K to °C", -263.15, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("°C to K", 283.15, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("kelvin to °C", -263.15, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("°C to kelvin", 283.15, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("°Re to °C", 12.5, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("°C to °Re", 8.0, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("Re to °C", 12.5, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("°C to Re", 8.0, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("reaumur to °C", 12.5, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("°C to reaumur", 8.0, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("Réaumur to °C", 12.5, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("°C to Réaumur", 8.0, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("°Rø to °C", 4.761904761904759, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("°C to °Rø", 12.75, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("Rø to °C", 4.761904761904759, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("°C to Rø", 12.75, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("romer to °C", 4.761904761904759, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("°C to romer", 12.75, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("Rømer to °C", 4.761904761904759, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("°C to Rømer", 12.75, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("°De to °C", 93.33333333333331, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("°C to °De", 135.0, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("De to °C", 93.33333333333331, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("°C to De", 135.0, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("delisle to °C", 93.33333333333331, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("°C to delisle", 135.0, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("°Ra to °C", -267.59444444444443, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("°C to °Ra", 509.66999999999996, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("Ra to °C", -267.59444444444443, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("°C to Ra", 509.66999999999996, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("rankine to °C", -267.59444444444443, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("°C to rankine", 509.66999999999996, val51!!, 0.05)
    }

    @Test
    fun `unit conversion static frequency`() = runBlocking {
        val lines = listOf(
            createLine("10 Hz in Hz", sortOrder = 0),
            createLine("10 Hz in Hz", sortOrder = 1),
            createLine("10 hertz in Hz", sortOrder = 2),
            createLine("10 Hz in hertz", sortOrder = 3),
            createLine("10 kHz in Hz", sortOrder = 4),
            createLine("10 Hz in kHz", sortOrder = 5),
            createLine("10 kilohertz in Hz", sortOrder = 6),
            createLine("10 Hz in kilohertz", sortOrder = 7),
            createLine("10 MHz in Hz", sortOrder = 8),
            createLine("10 Hz in MHz", sortOrder = 9),
            createLine("10 megahertz in Hz", sortOrder = 10),
            createLine("10 Hz in megahertz", sortOrder = 11),
            createLine("10 GHz in Hz", sortOrder = 12),
            createLine("10 Hz in GHz", sortOrder = 13),
            createLine("10 gigahertz in Hz", sortOrder = 14),
            createLine("10 Hz in gigahertz", sortOrder = 15),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("Hz to Hz", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("Hz to Hz", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("hertz to Hz", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("Hz to hertz", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("kHz to Hz", 10000.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("Hz to kHz", 0.01, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("kilohertz to Hz", 10000.0, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("Hz to kilohertz", 0.01, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("MHz to Hz", 1.0E7, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("Hz to MHz", 1.0E-5, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("megahertz to Hz", 1.0E7, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("Hz to megahertz", 1.0E-5, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("GHz to Hz", 1.0E10, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("Hz to GHz", 1.0E-8, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("gigahertz to Hz", 1.0E10, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("Hz to gigahertz", 1.0E-8, val15!!, 0.05)
    }

    @Test
    fun `unit conversion static energy`() = runBlocking {
        val lines = listOf(
            createLine("10 J in J", sortOrder = 0),
            createLine("10 J in J", sortOrder = 1),
            createLine("10 joule in J", sortOrder = 2),
            createLine("10 J in joule", sortOrder = 3),
            createLine("10 joules in J", sortOrder = 4),
            createLine("10 J in joules", sortOrder = 5),
            createLine("10 kJ in J", sortOrder = 6),
            createLine("10 J in kJ", sortOrder = 7),
            createLine("10 kilojoule in J", sortOrder = 8),
            createLine("10 J in kilojoule", sortOrder = 9),
            createLine("10 kilojoules in J", sortOrder = 10),
            createLine("10 J in kilojoules", sortOrder = 11),
            createLine("10 MJ in J", sortOrder = 12),
            createLine("10 J in MJ", sortOrder = 13),
            createLine("10 megajoule in J", sortOrder = 14),
            createLine("10 J in megajoule", sortOrder = 15),
            createLine("10 megajoules in J", sortOrder = 16),
            createLine("10 J in megajoules", sortOrder = 17),
            createLine("10 cal in J", sortOrder = 18),
            createLine("10 J in cal", sortOrder = 19),
            createLine("10 calorie in J", sortOrder = 20),
            createLine("10 J in calorie", sortOrder = 21),
            createLine("10 calories in J", sortOrder = 22),
            createLine("10 J in calories", sortOrder = 23),
            createLine("10 kCal in J", sortOrder = 24),
            createLine("10 J in kCal", sortOrder = 25),
            createLine("10 kcal in J", sortOrder = 26),
            createLine("10 J in kcal", sortOrder = 27),
            createLine("10 kilocalorie in J", sortOrder = 28),
            createLine("10 J in kilocalorie", sortOrder = 29),
            createLine("10 kilocalories in J", sortOrder = 30),
            createLine("10 J in kilocalories", sortOrder = 31),
            createLine("10 Wh in J", sortOrder = 32),
            createLine("10 J in Wh", sortOrder = 33),
            createLine("10 watt hour in J", sortOrder = 34),
            createLine("10 J in watt hour", sortOrder = 35),
            createLine("10 watt hours in J", sortOrder = 36),
            createLine("10 J in watt hours", sortOrder = 37),
            createLine("10 kWh in J", sortOrder = 38),
            createLine("10 J in kWh", sortOrder = 39),
            createLine("10 kilowatt hour in J", sortOrder = 40),
            createLine("10 J in kilowatt hour", sortOrder = 41),
            createLine("10 kilowatt hours in J", sortOrder = 42),
            createLine("10 J in kilowatt hours", sortOrder = 43),
            createLine("10 eV in J", sortOrder = 44),
            createLine("10 J in eV", sortOrder = 45),
            createLine("10 electronvolt in J", sortOrder = 46),
            createLine("10 J in electronvolt", sortOrder = 47),
            createLine("10 electron volts in J", sortOrder = 48),
            createLine("10 J in electron volts", sortOrder = 49),
            createLine("10 ft_lbf in J", sortOrder = 50),
            createLine("10 J in ft_lbf", sortOrder = 51),
            createLine("10 ft_lbf in J", sortOrder = 52),
            createLine("10 J in ft_lbf", sortOrder = 53),
            createLine("10 foot_pound in J", sortOrder = 54),
            createLine("10 J in foot_pound", sortOrder = 55),
            createLine("10 BTU in J", sortOrder = 56),
            createLine("10 J in BTU", sortOrder = 57),
            createLine("10 btu in J", sortOrder = 58),
            createLine("10 J in btu", sortOrder = 59),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("J to J", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("J to J", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("joule to J", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("J to joule", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("joules to J", 10.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("J to joules", 10.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("kJ to J", 10000.0, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("J to kJ", 0.01, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("kilojoule to J", 10000.0, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("J to kilojoule", 0.01, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("kilojoules to J", 10000.0, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("J to kilojoules", 0.01, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("MJ to J", 1.0E7, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("J to MJ", 1.0E-5, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("megajoule to J", 1.0E7, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("J to megajoule", 1.0E-5, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("megajoules to J", 1.0E7, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("J to megajoules", 1.0E-5, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("cal to J", 41.84, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("J to cal", 2.390057361376673, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("calorie to J", 41.84, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("J to calorie", 2.390057361376673, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("calories to J", 41.84, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("J to calories", 2.390057361376673, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("kCal to J", 41840.0, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("J to kCal", 0.002390057361376673, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("kcal to J", 41840.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("J to kcal", 0.002390057361376673, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("kilocalorie to J", 41840.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("J to kilocalorie", 0.002390057361376673, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("kilocalories to J", 41840.0, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("J to kilocalories", 0.002390057361376673, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("Wh to J", 36000.0, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("J to Wh", 0.002777777777777778, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("watt hour to J", 36000.0, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("J to watt hour", 0.002777777777777778, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("watt hours to J", 36000.0, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("J to watt hours", 0.002777777777777778, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("kWh to J", 3.6E7, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("J to kWh", 2.777777777777778E-6, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("kilowatt hour to J", 3.6E7, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("J to kilowatt hour", 2.777777777777778E-6, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("kilowatt hours to J", 3.6E7, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("J to kilowatt hours", 2.777777777777778E-6, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("eV to J", 1.602176634E-18, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("J to eV", 6.241509074460763E19, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("electronvolt to J", 1.602176634E-18, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("J to electronvolt", 6.241509074460763E19, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("electron volts to J", 1.602176634E-18, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("J to electron volts", 6.241509074460763E19, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("ft_lbf to J", 13.558179483314, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("J to ft_lbf", 7.375621492772656, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("ft_lbf to J", 13.558179483314, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("J to ft_lbf", 7.375621492772656, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("foot_pound to J", 13.558179483314, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("J to foot_pound", 7.375621492772656, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("BTU to J", 10550.5585262, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("J to BTU", 0.009478171203133172, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("btu to J", 10550.5585262, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("J to btu", 0.009478171203133172, val59!!, 0.05)
    }

    @Test
    fun `unit conversion static power`() = runBlocking {
        val lines = listOf(
            createLine("10 W in W", sortOrder = 0),
            createLine("10 W in W", sortOrder = 1),
            createLine("10 watt in W", sortOrder = 2),
            createLine("10 W in watt", sortOrder = 3),
            createLine("10 watts in W", sortOrder = 4),
            createLine("10 W in watts", sortOrder = 5),
            createLine("10 mW in W", sortOrder = 6),
            createLine("10 W in mW", sortOrder = 7),
            createLine("10 milliwatt in W", sortOrder = 8),
            createLine("10 W in milliwatt", sortOrder = 9),
            createLine("10 milliwatts in W", sortOrder = 10),
            createLine("10 W in milliwatts", sortOrder = 11),
            createLine("10 kW in W", sortOrder = 12),
            createLine("10 W in kW", sortOrder = 13),
            createLine("10 kilowatt in W", sortOrder = 14),
            createLine("10 W in kilowatt", sortOrder = 15),
            createLine("10 kilowatts in W", sortOrder = 16),
            createLine("10 W in kilowatts", sortOrder = 17),
            createLine("10 MW in W", sortOrder = 18),
            createLine("10 W in MW", sortOrder = 19),
            createLine("10 megawatt in W", sortOrder = 20),
            createLine("10 W in megawatt", sortOrder = 21),
            createLine("10 megawatts in W", sortOrder = 22),
            createLine("10 W in megawatts", sortOrder = 23),
            createLine("10 hp in W", sortOrder = 24),
            createLine("10 W in hp", sortOrder = 25),
            createLine("10 horsepower in W", sortOrder = 26),
            createLine("10 W in horsepower", sortOrder = 27),
            createLine("10 horsepowers in W", sortOrder = 28),
            createLine("10 W in horsepowers", sortOrder = 29),
            createLine("10 GW in W", sortOrder = 30),
            createLine("10 W in GW", sortOrder = 31),
            createLine("10 gigawatt in W", sortOrder = 32),
            createLine("10 W in gigawatt", sortOrder = 33),
            createLine("10 gigawatts in W", sortOrder = 34),
            createLine("10 W in gigawatts", sortOrder = 35),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("W to W", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("W to W", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("watt to W", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("W to watt", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("watts to W", 10.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("W to watts", 10.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("mW to W", 0.01, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("W to mW", 10000.0, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("milliwatt to W", 0.01, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("W to milliwatt", 10000.0, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("milliwatts to W", 0.01, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("W to milliwatts", 10000.0, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("kW to W", 10000.0, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("W to kW", 0.01, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("kilowatt to W", 10000.0, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("W to kilowatt", 0.01, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("kilowatts to W", 10000.0, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("W to kilowatts", 0.01, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("MW to W", 1.0E7, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("W to MW", 1.0E-5, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("megawatt to W", 1.0E7, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("W to megawatt", 1.0E-5, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("megawatts to W", 1.0E7, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("W to megawatts", 1.0E-5, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("hp to W", 7457.0, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("W to hp", 0.01341021858656296, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("horsepower to W", 7457.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("W to horsepower", 0.01341021858656296, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("horsepowers to W", 7457.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("W to horsepowers", 0.01341021858656296, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("GW to W", 1.0E10, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("W to GW", 1.0E-8, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("gigawatt to W", 1.0E10, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("W to gigawatt", 1.0E-8, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("gigawatts to W", 1.0E10, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("W to gigawatts", 1.0E-8, val35!!, 0.05)
    }

    @Test
    fun `unit conversion static data`() = runBlocking {
        val lines = listOf(
            createLine("10 bit in B", sortOrder = 0),
            createLine("10 B in bit", sortOrder = 1),
            createLine("10 bits in B", sortOrder = 2),
            createLine("10 B in bits", sortOrder = 3),
            createLine("10 b in B", sortOrder = 4),
            createLine("10 B in b", sortOrder = 5),
            createLine("10 nibble in B", sortOrder = 6),
            createLine("10 B in nibble", sortOrder = 7),
            createLine("10 nibbles in B", sortOrder = 8),
            createLine("10 B in nibbles", sortOrder = 9),
            createLine("10 B in B", sortOrder = 10),
            createLine("10 B in B", sortOrder = 11),
            createLine("10 byte in B", sortOrder = 12),
            createLine("10 B in byte", sortOrder = 13),
            createLine("10 bytes in B", sortOrder = 14),
            createLine("10 B in bytes", sortOrder = 15),
            createLine("10 kB in B", sortOrder = 16),
            createLine("10 B in kB", sortOrder = 17),
            createLine("10 KB in B", sortOrder = 18),
            createLine("10 B in KB", sortOrder = 19),
            createLine("10 kilobyte in B", sortOrder = 20),
            createLine("10 B in kilobyte", sortOrder = 21),
            createLine("10 kilobytes in B", sortOrder = 22),
            createLine("10 B in kilobytes", sortOrder = 23),
            createLine("10 MB in B", sortOrder = 24),
            createLine("10 B in MB", sortOrder = 25),
            createLine("10 megabyte in B", sortOrder = 26),
            createLine("10 B in megabyte", sortOrder = 27),
            createLine("10 megabytes in B", sortOrder = 28),
            createLine("10 B in megabytes", sortOrder = 29),
            createLine("10 GB in B", sortOrder = 30),
            createLine("10 B in GB", sortOrder = 31),
            createLine("10 gigabyte in B", sortOrder = 32),
            createLine("10 B in gigabyte", sortOrder = 33),
            createLine("10 gigabytes in B", sortOrder = 34),
            createLine("10 B in gigabytes", sortOrder = 35),
            createLine("10 TB in B", sortOrder = 36),
            createLine("10 B in TB", sortOrder = 37),
            createLine("10 terabyte in B", sortOrder = 38),
            createLine("10 B in terabyte", sortOrder = 39),
            createLine("10 terabytes in B", sortOrder = 40),
            createLine("10 B in terabytes", sortOrder = 41),
            createLine("10 KiB in B", sortOrder = 42),
            createLine("10 B in KiB", sortOrder = 43),
            createLine("10 kibibyte in B", sortOrder = 44),
            createLine("10 B in kibibyte", sortOrder = 45),
            createLine("10 kibibytes in B", sortOrder = 46),
            createLine("10 B in kibibytes", sortOrder = 47),
            createLine("10 MiB in B", sortOrder = 48),
            createLine("10 B in MiB", sortOrder = 49),
            createLine("10 mebibyte in B", sortOrder = 50),
            createLine("10 B in mebibyte", sortOrder = 51),
            createLine("10 mebibytes in B", sortOrder = 52),
            createLine("10 B in mebibytes", sortOrder = 53),
            createLine("10 GiB in B", sortOrder = 54),
            createLine("10 B in GiB", sortOrder = 55),
            createLine("10 gibibyte in B", sortOrder = 56),
            createLine("10 B in gibibyte", sortOrder = 57),
            createLine("10 gibibytes in B", sortOrder = 58),
            createLine("10 B in gibibytes", sortOrder = 59),
            createLine("10 TiB in B", sortOrder = 60),
            createLine("10 B in TiB", sortOrder = 61),
            createLine("10 tebibyte in B", sortOrder = 62),
            createLine("10 B in tebibyte", sortOrder = 63),
            createLine("10 tebibytes in B", sortOrder = 64),
            createLine("10 B in tebibytes", sortOrder = 65),
            createLine("10 PiB in B", sortOrder = 66),
            createLine("10 B in PiB", sortOrder = 67),
            createLine("10 pebibyte in B", sortOrder = 68),
            createLine("10 B in pebibyte", sortOrder = 69),
            createLine("10 pebibytes in B", sortOrder = 70),
            createLine("10 B in pebibytes", sortOrder = 71),
            createLine("10 EiB in B", sortOrder = 72),
            createLine("10 B in EiB", sortOrder = 73),
            createLine("10 exbibyte in B", sortOrder = 74),
            createLine("10 B in exbibyte", sortOrder = 75),
            createLine("10 exbibytes in B", sortOrder = 76),
            createLine("10 B in exbibytes", sortOrder = 77),
            createLine("10 PB in B", sortOrder = 78),
            createLine("10 B in PB", sortOrder = 79),
            createLine("10 petabyte in B", sortOrder = 80),
            createLine("10 B in petabyte", sortOrder = 81),
            createLine("10 petabytes in B", sortOrder = 82),
            createLine("10 B in petabytes", sortOrder = 83),
            createLine("10 EB in B", sortOrder = 84),
            createLine("10 B in EB", sortOrder = 85),
            createLine("10 exabyte in B", sortOrder = 86),
            createLine("10 B in exabyte", sortOrder = 87),
            createLine("10 exabytes in B", sortOrder = 88),
            createLine("10 B in exabytes", sortOrder = 89),
            createLine("10 Kibit in B", sortOrder = 90),
            createLine("10 B in Kibit", sortOrder = 91),
            createLine("10 kibibit in B", sortOrder = 92),
            createLine("10 B in kibibit", sortOrder = 93),
            createLine("10 Mibit in B", sortOrder = 94),
            createLine("10 B in Mibit", sortOrder = 95),
            createLine("10 mebibit in B", sortOrder = 96),
            createLine("10 B in mebibit", sortOrder = 97),
            createLine("10 Gibit in B", sortOrder = 98),
            createLine("10 B in Gibit", sortOrder = 99),
            createLine("10 gibibit in B", sortOrder = 100),
            createLine("10 B in gibibit", sortOrder = 101),
            createLine("10 Tibit in B", sortOrder = 102),
            createLine("10 B in Tibit", sortOrder = 103),
            createLine("10 tebibit in B", sortOrder = 104),
            createLine("10 B in tebibit", sortOrder = 105),
            createLine("10 Pibit in B", sortOrder = 106),
            createLine("10 B in Pibit", sortOrder = 107),
            createLine("10 pebibit in B", sortOrder = 108),
            createLine("10 B in pebibit", sortOrder = 109),
            createLine("10 Eibit in B", sortOrder = 110),
            createLine("10 B in Eibit", sortOrder = 111),
            createLine("10 exbibit in B", sortOrder = 112),
            createLine("10 B in exbibit", sortOrder = 113),
            createLine("10 kb in B", sortOrder = 114),
            createLine("10 B in kb", sortOrder = 115),
            createLine("10 kilobit in B", sortOrder = 116),
            createLine("10 B in kilobit", sortOrder = 117),
            createLine("10 Mb in B", sortOrder = 118),
            createLine("10 B in Mb", sortOrder = 119),
            createLine("10 megabit in B", sortOrder = 120),
            createLine("10 B in megabit", sortOrder = 121),
            createLine("10 Gb in B", sortOrder = 122),
            createLine("10 B in Gb", sortOrder = 123),
            createLine("10 gigabit in B", sortOrder = 124),
            createLine("10 B in gigabit", sortOrder = 125),
            createLine("10 Tb in B", sortOrder = 126),
            createLine("10 B in Tb", sortOrder = 127),
            createLine("10 terabit in B", sortOrder = 128),
            createLine("10 B in terabit", sortOrder = 129),
            createLine("10 Pb in B", sortOrder = 130),
            createLine("10 B in Pb", sortOrder = 131),
            createLine("10 petabit in B", sortOrder = 132),
            createLine("10 B in petabit", sortOrder = 133),
            createLine("10 Eb in B", sortOrder = 134),
            createLine("10 B in Eb", sortOrder = 135),
            createLine("10 exabit in B", sortOrder = 136),
            createLine("10 B in exabit", sortOrder = 137),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("bit to B", 1.25, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("B to bit", 80.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("bits to B", 1.25, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("B to bits", 80.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("b to B", 1.25, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("B to b", 80.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("nibble to B", 5.0, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("B to nibble", 20.0, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("nibbles to B", 5.0, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("B to nibbles", 20.0, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("B to B", 10.0, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("B to B", 10.0, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("byte to B", 10.0, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("B to byte", 10.0, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("bytes to B", 10.0, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("B to bytes", 10.0, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("kB to B", 10000.0, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("B to kB", 0.01, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("KB to B", 10000.0, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("B to KB", 0.01, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("kilobyte to B", 10000.0, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("B to kilobyte", 0.01, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("kilobytes to B", 10000.0, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("B to kilobytes", 0.01, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("MB to B", 1.0E7, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("B to MB", 1.0E-5, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("megabyte to B", 1.0E7, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("B to megabyte", 1.0E-5, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("megabytes to B", 1.0E7, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("B to megabytes", 1.0E-5, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("GB to B", 1.0E10, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("B to GB", 1.0E-8, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("gigabyte to B", 1.0E10, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("B to gigabyte", 1.0E-8, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("gigabytes to B", 1.0E10, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("B to gigabytes", 1.0E-8, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("TB to B", 1.0E13, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("B to TB", 1.0E-11, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("terabyte to B", 1.0E13, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("B to terabyte", 1.0E-11, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("terabytes to B", 1.0E13, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("B to terabytes", 1.0E-11, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("KiB to B", 10240.0, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("B to KiB", 0.009765625, val43!!, 0.05)
        val val44 = result[44].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val44)
        assertEquals("kibibyte to B", 10240.0, val44!!, 0.05)
        val val45 = result[45].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val45)
        assertEquals("B to kibibyte", 0.009765625, val45!!, 0.05)
        val val46 = result[46].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val46)
        assertEquals("kibibytes to B", 10240.0, val46!!, 0.05)
        val val47 = result[47].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val47)
        assertEquals("B to kibibytes", 0.009765625, val47!!, 0.05)
        val val48 = result[48].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val48)
        assertEquals("MiB to B", 1.048576E7, val48!!, 0.05)
        val val49 = result[49].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val49)
        assertEquals("B to MiB", 9.5367431640625E-6, val49!!, 0.05)
        val val50 = result[50].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val50)
        assertEquals("mebibyte to B", 1.048576E7, val50!!, 0.05)
        val val51 = result[51].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val51)
        assertEquals("B to mebibyte", 9.5367431640625E-6, val51!!, 0.05)
        val val52 = result[52].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val52)
        assertEquals("mebibytes to B", 1.048576E7, val52!!, 0.05)
        val val53 = result[53].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val53)
        assertEquals("B to mebibytes", 9.5367431640625E-6, val53!!, 0.05)
        val val54 = result[54].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val54)
        assertEquals("GiB to B", 1.073741824E10, val54!!, 0.05)
        val val55 = result[55].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val55)
        assertEquals("B to GiB", 9.313225746154785E-9, val55!!, 0.05)
        val val56 = result[56].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val56)
        assertEquals("gibibyte to B", 1.073741824E10, val56!!, 0.05)
        val val57 = result[57].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val57)
        assertEquals("B to gibibyte", 9.313225746154785E-9, val57!!, 0.05)
        val val58 = result[58].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val58)
        assertEquals("gibibytes to B", 1.073741824E10, val58!!, 0.05)
        val val59 = result[59].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val59)
        assertEquals("B to gibibytes", 9.313225746154785E-9, val59!!, 0.05)
        val val60 = result[60].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val60)
        assertEquals("TiB to B", 1.099511627776E13, val60!!, 0.05)
        val val61 = result[61].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val61)
        assertEquals("B to TiB", 9.094947017729282E-12, val61!!, 0.05)
        val val62 = result[62].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val62)
        assertEquals("tebibyte to B", 1.099511627776E13, val62!!, 0.05)
        val val63 = result[63].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val63)
        assertEquals("B to tebibyte", 9.094947017729282E-12, val63!!, 0.05)
        val val64 = result[64].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val64)
        assertEquals("tebibytes to B", 1.099511627776E13, val64!!, 0.05)
        val val65 = result[65].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val65)
        assertEquals("B to tebibytes", 9.094947017729282E-12, val65!!, 0.05)
        val val66 = result[66].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val66)
        assertEquals("PiB to B", 1.125899906842624E16, val66!!, 0.05)
        val val67 = result[67].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val67)
        assertEquals("B to PiB", 8.881784197001252E-15, val67!!, 0.05)
        val val68 = result[68].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val68)
        assertEquals("pebibyte to B", 1.125899906842624E16, val68!!, 0.05)
        val val69 = result[69].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val69)
        assertEquals("B to pebibyte", 8.881784197001252E-15, val69!!, 0.05)
        val val70 = result[70].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val70)
        assertEquals("pebibytes to B", 1.125899906842624E16, val70!!, 0.05)
        val val71 = result[71].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val71)
        assertEquals("B to pebibytes", 8.881784197001252E-15, val71!!, 0.05)
        val val72 = result[72].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val72)
        assertEquals("EiB to B", 1.152921504606847E19, val72!!, 0.05)
        val val73 = result[73].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val73)
        assertEquals("B to EiB", 8.673617379935547E-18, val73!!, 0.05)
        val val74 = result[74].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val74)
        assertEquals("exbibyte to B", 1.152921504606847E19, val74!!, 0.05)
        val val75 = result[75].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val75)
        assertEquals("B to exbibyte", 8.673617379935547E-18, val75!!, 0.05)
        val val76 = result[76].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val76)
        assertEquals("exbibytes to B", 1.152921504606847E19, val76!!, 0.05)
        val val77 = result[77].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val77)
        assertEquals("B to exbibytes", 8.673617379935547E-18, val77!!, 0.05)
        val val78 = result[78].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val78)
        assertEquals("PB to B", 1.0E16, val78!!, 0.05)
        val val79 = result[79].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val79)
        assertEquals("B to PB", 1.0E-14, val79!!, 0.05)
        val val80 = result[80].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val80)
        assertEquals("petabyte to B", 1.0E16, val80!!, 0.05)
        val val81 = result[81].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val81)
        assertEquals("B to petabyte", 1.0E-14, val81!!, 0.05)
        val val82 = result[82].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val82)
        assertEquals("petabytes to B", 1.0E16, val82!!, 0.05)
        val val83 = result[83].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val83)
        assertEquals("B to petabytes", 1.0E-14, val83!!, 0.05)
        val val84 = result[84].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val84)
        assertEquals("EB to B", 1.0E19, val84!!, 0.05)
        val val85 = result[85].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val85)
        assertEquals("B to EB", 1.0E-17, val85!!, 0.05)
        val val86 = result[86].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val86)
        assertEquals("exabyte to B", 1.0E19, val86!!, 0.05)
        val val87 = result[87].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val87)
        assertEquals("B to exabyte", 1.0E-17, val87!!, 0.05)
        val val88 = result[88].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val88)
        assertEquals("exabytes to B", 1.0E19, val88!!, 0.05)
        val val89 = result[89].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val89)
        assertEquals("B to exabytes", 1.0E-17, val89!!, 0.05)
        val val90 = result[90].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val90)
        assertEquals("Kibit to B", 1280.0, val90!!, 0.05)
        val val91 = result[91].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val91)
        assertEquals("B to Kibit", 0.078125, val91!!, 0.05)
        val val92 = result[92].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val92)
        assertEquals("kibibit to B", 1280.0, val92!!, 0.05)
        val val93 = result[93].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val93)
        assertEquals("B to kibibit", 0.078125, val93!!, 0.05)
        val val94 = result[94].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val94)
        assertEquals("Mibit to B", 1310720.0, val94!!, 0.05)
        val val95 = result[95].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val95)
        assertEquals("B to Mibit", 7.62939453125E-5, val95!!, 0.05)
        val val96 = result[96].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val96)
        assertEquals("mebibit to B", 1310720.0, val96!!, 0.05)
        val val97 = result[97].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val97)
        assertEquals("B to mebibit", 7.62939453125E-5, val97!!, 0.05)
        val val98 = result[98].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val98)
        assertEquals("Gibit to B", 1.34217728E9, val98!!, 0.05)
        val val99 = result[99].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val99)
        assertEquals("B to Gibit", 7.450580596923828E-8, val99!!, 0.05)
        val val100 = result[100].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val100)
        assertEquals("gibibit to B", 1.34217728E9, val100!!, 0.05)
        val val101 = result[101].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val101)
        assertEquals("B to gibibit", 7.450580596923828E-8, val101!!, 0.05)
        val val102 = result[102].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val102)
        assertEquals("Tibit to B", 1.37438953472E12, val102!!, 0.05)
        val val103 = result[103].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val103)
        assertEquals("B to Tibit", 7.275957614183426E-11, val103!!, 0.05)
        val val104 = result[104].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val104)
        assertEquals("tebibit to B", 1.37438953472E12, val104!!, 0.05)
        val val105 = result[105].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val105)
        assertEquals("B to tebibit", 7.275957614183426E-11, val105!!, 0.05)
        val val106 = result[106].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val106)
        assertEquals("Pibit to B", 1.40737488355328E15, val106!!, 0.05)
        val val107 = result[107].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val107)
        assertEquals("B to Pibit", 7.105427357617562E-14, val107!!, 0.05)
        val val108 = result[108].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val108)
        assertEquals("pebibit to B", 1.40737488355328E15, val108!!, 0.05)
        val val109 = result[109].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val109)
        assertEquals("B to pebibit", 7.105427357617562E-14, val109!!, 0.05)
        val val110 = result[110].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val110)
        assertEquals("Eibit to B", 1.4411518807585587E18, val110!!, 0.05)
        val val111 = result[111].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val111)
        assertEquals("B to Eibit", 6.938893903948438E-17, val111!!, 0.05)
        val val112 = result[112].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val112)
        assertEquals("exbibit to B", 1.4411518807585587E18, val112!!, 0.05)
        val val113 = result[113].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val113)
        assertEquals("B to exbibit", 6.938893903948438E-17, val113!!, 0.05)
        val val114 = result[114].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val114)
        assertEquals("kb to B", 1250.0, val114!!, 0.05)
        val val115 = result[115].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val115)
        assertEquals("B to kb", 0.08, val115!!, 0.05)
        val val116 = result[116].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val116)
        assertEquals("kilobit to B", 1250.0, val116!!, 0.05)
        val val117 = result[117].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val117)
        assertEquals("B to kilobit", 0.08, val117!!, 0.05)
        val val118 = result[118].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val118)
        assertEquals("Mb to B", 1250000.0, val118!!, 0.05)
        val val119 = result[119].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val119)
        assertEquals("B to Mb", 8.0E-5, val119!!, 0.05)
        val val120 = result[120].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val120)
        assertEquals("megabit to B", 1250000.0, val120!!, 0.05)
        val val121 = result[121].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val121)
        assertEquals("B to megabit", 8.0E-5, val121!!, 0.05)
        val val122 = result[122].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val122)
        assertEquals("Gb to B", 1.25E9, val122!!, 0.05)
        val val123 = result[123].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val123)
        assertEquals("B to Gb", 8.0E-8, val123!!, 0.05)
        val val124 = result[124].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val124)
        assertEquals("gigabit to B", 1.25E9, val124!!, 0.05)
        val val125 = result[125].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val125)
        assertEquals("B to gigabit", 8.0E-8, val125!!, 0.05)
        val val126 = result[126].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val126)
        assertEquals("Tb to B", 1.25E12, val126!!, 0.05)
        val val127 = result[127].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val127)
        assertEquals("B to Tb", 8.0E-11, val127!!, 0.05)
        val val128 = result[128].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val128)
        assertEquals("terabit to B", 1.25E12, val128!!, 0.05)
        val val129 = result[129].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val129)
        assertEquals("B to terabit", 8.0E-11, val129!!, 0.05)
        val val130 = result[130].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val130)
        assertEquals("Pb to B", 1.25E15, val130!!, 0.05)
        val val131 = result[131].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val131)
        assertEquals("B to Pb", 8.0E-14, val131!!, 0.05)
        val val132 = result[132].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val132)
        assertEquals("petabit to B", 1.25E15, val132!!, 0.05)
        val val133 = result[133].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val133)
        assertEquals("B to petabit", 8.0E-14, val133!!, 0.05)
        val val134 = result[134].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val134)
        assertEquals("Eb to B", 1.25E18, val134!!, 0.05)
        val val135 = result[135].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val135)
        assertEquals("B to Eb", 8.0E-17, val135!!, 0.05)
        val val136 = result[136].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val136)
        assertEquals("exabit to B", 1.25E18, val136!!, 0.05)
        val val137 = result[137].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val137)
        assertEquals("B to exabit", 8.0E-17, val137!!, 0.05)
    }

    @Test
    fun `unit conversion static force`() = runBlocking {
        val lines = listOf(
            createLine("10 N in N", sortOrder = 0),
            createLine("10 N in N", sortOrder = 1),
            createLine("10 newton in N", sortOrder = 2),
            createLine("10 N in newton", sortOrder = 3),
            createLine("10 newtons in N", sortOrder = 4),
            createLine("10 N in newtons", sortOrder = 5),
            createLine("10 kgf in N", sortOrder = 6),
            createLine("10 N in kgf", sortOrder = 7),
            createLine("10 kg_f in N", sortOrder = 8),
            createLine("10 N in kg_f", sortOrder = 9),
            createLine("10 lbf in N", sortOrder = 10),
            createLine("10 N in lbf", sortOrder = 11),
            createLine("10 lb_f in N", sortOrder = 12),
            createLine("10 N in lb_f", sortOrder = 13),
            createLine("10 dyn in N", sortOrder = 14),
            createLine("10 N in dyn", sortOrder = 15),
            createLine("10 dyne in N", sortOrder = 16),
            createLine("10 N in dyne", sortOrder = 17),
            createLine("10 pdl in N", sortOrder = 18),
            createLine("10 N in pdl", sortOrder = 19),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("N to N", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("N to N", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("newton to N", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("N to newton", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("newtons to N", 10.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("N to newtons", 10.0, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("kgf to N", 98.06649999999999, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("N to kgf", 1.0197162129779282, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("kg_f to N", 98.06649999999999, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("N to kg_f", 1.0197162129779282, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("lbf to N", 44.482220000000005, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("N to lbf", 2.2480892365533913, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("lb_f to N", 44.482220000000005, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("N to lb_f", 2.2480892365533913, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("dyn to N", 1.0E-4, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("N to dyn", 999999.9999999999, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("dyne to N", 1.0E-4, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("N to dyne", 999999.9999999999, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("pdl to N", 1.38255, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("N to pdl", 72.33011464323171, val19!!, 0.05)
    }

    @Test
    fun `unit conversion static pressure`() = runBlocking {
        val lines = listOf(
            createLine("10 Pa in Pa", sortOrder = 0),
            createLine("10 Pa in Pa", sortOrder = 1),
            createLine("10 pascal in Pa", sortOrder = 2),
            createLine("10 Pa in pascal", sortOrder = 3),
            createLine("10 kPa in Pa", sortOrder = 4),
            createLine("10 Pa in kPa", sortOrder = 5),
            createLine("10 kilopascal in Pa", sortOrder = 6),
            createLine("10 Pa in kilopascal", sortOrder = 7),
            createLine("10 MPa in Pa", sortOrder = 8),
            createLine("10 Pa in MPa", sortOrder = 9),
            createLine("10 megapascal in Pa", sortOrder = 10),
            createLine("10 Pa in megapascal", sortOrder = 11),
            createLine("10 GPa in Pa", sortOrder = 12),
            createLine("10 Pa in GPa", sortOrder = 13),
            createLine("10 gigapascal in Pa", sortOrder = 14),
            createLine("10 Pa in gigapascal", sortOrder = 15),
            createLine("10 hPa in Pa", sortOrder = 16),
            createLine("10 Pa in hPa", sortOrder = 17),
            createLine("10 hectopascal in Pa", sortOrder = 18),
            createLine("10 Pa in hectopascal", sortOrder = 19),
            createLine("10 bar in Pa", sortOrder = 20),
            createLine("10 Pa in bar", sortOrder = 21),
            createLine("10 bars in Pa", sortOrder = 22),
            createLine("10 Pa in bars", sortOrder = 23),
            createLine("10 mbar in Pa", sortOrder = 24),
            createLine("10 Pa in mbar", sortOrder = 25),
            createLine("10 millibar in Pa", sortOrder = 26),
            createLine("10 Pa in millibar", sortOrder = 27),
            createLine("10 atm in Pa", sortOrder = 28),
            createLine("10 Pa in atm", sortOrder = 29),
            createLine("10 atmosphere in Pa", sortOrder = 30),
            createLine("10 Pa in atmosphere", sortOrder = 31),
            createLine("10 psi in Pa", sortOrder = 32),
            createLine("10 Pa in psi", sortOrder = 33),
            createLine("10 pound per square inch in Pa", sortOrder = 34),
            createLine("10 Pa in pound per square inch", sortOrder = 35),
            createLine("10 ksi in Pa", sortOrder = 36),
            createLine("10 Pa in ksi", sortOrder = 37),
            createLine("10 torr in Pa", sortOrder = 38),
            createLine("10 Pa in torr", sortOrder = 39),
            createLine("10 mmHg in Pa", sortOrder = 40),
            createLine("10 Pa in mmHg", sortOrder = 41),
            createLine("10 inHg in Pa", sortOrder = 42),
            createLine("10 Pa in inHg", sortOrder = 43),
        )
        val result = MathEngine.calculate(lines)
        val val0 = result[0].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val0)
        assertEquals("Pa to Pa", 10.0, val0!!, 0.05)
        val val1 = result[1].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val1)
        assertEquals("Pa to Pa", 10.0, val1!!, 0.05)
        val val2 = result[2].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val2)
        assertEquals("pascal to Pa", 10.0, val2!!, 0.05)
        val val3 = result[3].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val3)
        assertEquals("Pa to pascal", 10.0, val3!!, 0.05)
        val val4 = result[4].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val4)
        assertEquals("kPa to Pa", 10000.0, val4!!, 0.05)
        val val5 = result[5].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val5)
        assertEquals("Pa to kPa", 0.01, val5!!, 0.05)
        val val6 = result[6].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val6)
        assertEquals("kilopascal to Pa", 10000.0, val6!!, 0.05)
        val val7 = result[7].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val7)
        assertEquals("Pa to kilopascal", 0.01, val7!!, 0.05)
        val val8 = result[8].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val8)
        assertEquals("MPa to Pa", 1.0E7, val8!!, 0.05)
        val val9 = result[9].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val9)
        assertEquals("Pa to MPa", 1.0E-5, val9!!, 0.05)
        val val10 = result[10].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val10)
        assertEquals("megapascal to Pa", 1.0E7, val10!!, 0.05)
        val val11 = result[11].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val11)
        assertEquals("Pa to megapascal", 1.0E-5, val11!!, 0.05)
        val val12 = result[12].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val12)
        assertEquals("GPa to Pa", 1.0E10, val12!!, 0.05)
        val val13 = result[13].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val13)
        assertEquals("Pa to GPa", 1.0E-8, val13!!, 0.05)
        val val14 = result[14].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val14)
        assertEquals("gigapascal to Pa", 1.0E10, val14!!, 0.05)
        val val15 = result[15].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val15)
        assertEquals("Pa to gigapascal", 1.0E-8, val15!!, 0.05)
        val val16 = result[16].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val16)
        assertEquals("hPa to Pa", 1000.0, val16!!, 0.05)
        val val17 = result[17].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val17)
        assertEquals("Pa to hPa", 0.1, val17!!, 0.05)
        val val18 = result[18].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val18)
        assertEquals("hectopascal to Pa", 1000.0, val18!!, 0.05)
        val val19 = result[19].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val19)
        assertEquals("Pa to hectopascal", 0.1, val19!!, 0.05)
        val val20 = result[20].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val20)
        assertEquals("bar to Pa", 1000000.0, val20!!, 0.05)
        val val21 = result[21].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val21)
        assertEquals("Pa to bar", 1.0E-4, val21!!, 0.05)
        val val22 = result[22].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val22)
        assertEquals("bars to Pa", 1000000.0, val22!!, 0.05)
        val val23 = result[23].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val23)
        assertEquals("Pa to bars", 1.0E-4, val23!!, 0.05)
        val val24 = result[24].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val24)
        assertEquals("mbar to Pa", 1000.0, val24!!, 0.05)
        val val25 = result[25].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val25)
        assertEquals("Pa to mbar", 0.1, val25!!, 0.05)
        val val26 = result[26].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val26)
        assertEquals("millibar to Pa", 1000.0, val26!!, 0.05)
        val val27 = result[27].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val27)
        assertEquals("Pa to millibar", 0.1, val27!!, 0.05)
        val val28 = result[28].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val28)
        assertEquals("atm to Pa", 1013250.0, val28!!, 0.05)
        val val29 = result[29].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val29)
        assertEquals("Pa to atm", 9.869232667160128E-5, val29!!, 0.05)
        val val30 = result[30].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val30)
        assertEquals("atmosphere to Pa", 1013250.0, val30!!, 0.05)
        val val31 = result[31].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val31)
        assertEquals("Pa to atmosphere", 9.869232667160128E-5, val31!!, 0.05)
        val val32 = result[32].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val32)
        assertEquals("psi to Pa", 68947.56999999999, val32!!, 0.05)
        val val33 = result[33].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val33)
        assertEquals("Pa to psi", 0.0014503774389728313, val33!!, 0.05)
        val val34 = result[34].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val34)
        assertEquals("pound per square inch to Pa", 68947.56999999999, val34!!, 0.05)
        val val35 = result[35].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val35)
        assertEquals("Pa to pound per square inch", 0.0014503774389728313, val35!!, 0.05)
        val val36 = result[36].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val36)
        assertEquals("ksi to Pa", 6.894757E7, val36!!, 0.05)
        val val37 = result[37].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val37)
        assertEquals("Pa to ksi", 1.4503774389728311E-6, val37!!, 0.05)
        val val38 = result[38].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val38)
        assertEquals("torr to Pa", 1333.224, val38!!, 0.05)
        val val39 = result[39].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val39)
        assertEquals("Pa to torr", 0.07500615050434137, val39!!, 0.05)
        val val40 = result[40].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val40)
        assertEquals("mmHg to Pa", 1333.224, val40!!, 0.05)
        val val41 = result[41].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val41)
        assertEquals("Pa to mmHg", 0.07500615050434137, val41!!, 0.05)
        val val42 = result[42].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val42)
        assertEquals("inHg to Pa", 33863.88, val42!!, 0.05)
        val val43 = result[43].result.substringBefore(' ').toDoubleOrNull()
        assertNotNull(val43)
        assertEquals("Pa to inHg", 0.0029529988884912186, val43!!, 0.05)
    }

    @Test
    fun `unit conversion static numeral system`() = runBlocking {
        val lines = listOf(
            createLine("10 dec in dec", sortOrder = 0),
            createLine("10 dec in dec", sortOrder = 1),
            createLine("10 decimal in dec", sortOrder = 2),
            createLine("10 dec in decimal", sortOrder = 3),
            createLine("10 hex in dec", sortOrder = 4),
            createLine("10 dec in hex", sortOrder = 5),
            createLine("10 hexadecimal in dec", sortOrder = 6),
            createLine("10 dec in hexadecimal", sortOrder = 7),
            createLine("10 oct in dec", sortOrder = 8),
            createLine("10 dec in oct", sortOrder = 9),
            createLine("10 octal in dec", sortOrder = 10),
            createLine("10 dec in octal", sortOrder = 11),
            createLine("10 bin in dec", sortOrder = 12),
            createLine("10 dec in bin", sortOrder = 13),
            createLine("10 binary in dec", sortOrder = 14),
            createLine("10 dec in binary", sortOrder = 15),
        )
        val result = MathEngine.calculate(lines)
        assertFalse("Failed dec to dec", result[0].result == "Err")
        assertFalse("Failed dec to dec", result[1].result == "Err")
        assertFalse("Failed decimal to dec", result[2].result == "Err")
        assertFalse("Failed dec to decimal", result[3].result == "Err")
        assertFalse("Failed hex to dec", result[4].result == "Err")
        assertFalse("Failed dec to hex", result[5].result == "Err")
        assertFalse("Failed hexadecimal to dec", result[6].result == "Err")
        assertFalse("Failed dec to hexadecimal", result[7].result == "Err")
        assertFalse("Failed oct to dec", result[8].result == "Err")
        assertFalse("Failed dec to oct", result[9].result == "Err")
        assertFalse("Failed octal to dec", result[10].result == "Err")
        assertFalse("Failed dec to octal", result[11].result == "Err")
        assertFalse("Failed bin to dec", result[12].result == "Err")
        assertFalse("Failed dec to bin", result[13].result == "Err")
        assertFalse("Failed binary to dec", result[14].result == "Err")
        assertFalse("Failed dec to binary", result[15].result == "Err")
    }


}
