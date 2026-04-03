package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity

import org.junit.Assert.*
import org.junit.Test
import com.vishaltelangre.nerdcalci.core.createLine
import com.vishaltelangre.nerdcalci.core.testCalculate

class UnitConversionStaticTest {
    @Test
    fun `unit conversion static time`() = testCalculate(
            "10 ns in s",
            "10 s in ns",
            "10 nanosecond in s",
            "10 s in nanosecond",
            "10 nanoseconds in s",
            "10 s in nanoseconds",
            "10 µs in s",
            "10 s in µs",
            "10 us in s",
            "10 s in us",
            "10 microsecond in s",
            "10 s in microsecond",
            "10 microseconds in s",
            "10 s in microseconds",
            "10 ms in s",
            "10 s in ms",
            "10 millisecond in s",
            "10 s in millisecond",
            "10 milliseconds in s",
            "10 s in milliseconds",
            "10 s in s",
            "10 s in s",
            "10 sec in s",
            "10 s in sec",
            "10 secs in s",
            "10 s in secs",
            "10 second in s",
            "10 s in second",
            "10 seconds in s",
            "10 s in seconds",
            "10 min in s",
            "10 s in min",
            "10 mins in s",
            "10 s in mins",
            "10 minute in s",
            "10 s in minute",
            "10 minutes in s",
            "10 s in minutes",
            "10 h in s",
            "10 s in h",
            "10 hr in s",
            "10 s in hr",
            "10 hrs in s",
            "10 s in hrs",
            "10 hour in s",
            "10 s in hour",
            "10 hours in s",
            "10 s in hours",
            "10 d in s",
            "10 s in d",
            "10 day in s",
            "10 s in day",
            "10 days in s",
            "10 s in days",
            "10 wk in s",
            "10 s in wk",
            "10 wks in s",
            "10 s in wks",
            "10 week in s",
            "10 s in week",
            "10 weeks in s",
            "10 s in weeks",
            "10 mo in s",
            "10 s in mo",
            "10 mnth in s",
            "10 s in mnth",
            "10 mnths in s",
            "10 s in mnths",
            "10 month in s",
            "10 s in month",
            "10 months in s",
            "10 s in months",
            "10 y in s",
            "10 s in y",
            "10 yr in s",
            "10 s in yr",
            "10 yrs in s",
            "10 s in yrs",
            "10 year in s",
            "10 s in year",
            "10 years in s",
            "10 s in years",
            "10 lustrum in s",
            "10 s in lustrum",
            "10 lustrums in s",
            "10 s in lustrums",
            "10 decade in s",
            "10 s in decade",
            "10 decades in s",
            "10 s in decades",
            "10 century in s",
            "10 s in century",
            "10 centuries in s",
            "10 s in centuries",
            "10 millennium in s",
            "10 s in millennium",
            "10 millennia in s",
            "10 s in millennia",
            "10 millenniums in s",
            "10 s in millenniums",
            "10 ds in s",
            "10 s in ds",
            "10 decisecond in s",
            "10 s in decisecond",
            "10 deciseconds in s",
            "10 s in deciseconds",
            "10 cs in s",
            "10 s in cs",
            "10 centisecond in s",
            "10 s in centisecond",
            "10 centiseconds in s",
            "10 s in centiseconds"
        ) { result ->
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
    fun `unit conversion static length`() = testCalculate(
        "10 nm in m",
        "10 m in nm",
        "10 nanometer in m",
        "10 m in nanometer",
        "10 nanometers in m",
        "10 m in nanometers",
        "10 µm in m",
        "10 m in µm",
        "10 um in m",
        "10 m in um",
        "10 micrometer in m",
        "10 m in micrometer",
        "10 micrometers in m",
        "10 m in micrometers",
        "10 mm in m",
        "10 m in mm",
        "10 millimeter in m",
        "10 m in millimeter",
        "10 millimeters in m",
        "10 m in millimeters",
        "10 cm in m",
        "10 m in cm",
        "10 centimeter in m",
        "10 m in centimeter",
        "10 centimeters in m",
        "10 m in centimeters",
        "10 dm in m",
        "10 m in dm",
        "10 decimeter in m",
        "10 m in decimeter",
        "10 decimeters in m",
        "10 m in decimeters",
        "10 m in m",
        "10 m in m",
        "10 meter in m",
        "10 m in meter",
        "10 meters in m",
        "10 m in meters",
        "10 km in m",
        "10 m in km",
        "10 kms in m",
        "10 m in kms",
        "10 kilometer in m",
        "10 m in kilometer",
        "10 kilometers in m",
        "10 m in kilometers",
        "10 inch in m",
        "10 m in inch",
        "10 inch in m",
        "10 m in inch",
        "10 inches in m",
        "10 m in inches",
        "10 ft in m",
        "10 m in ft",
        "10 foot in m",
        "10 m in foot",
        "10 feet in m",
        "10 m in feet",
        "10 yd in m",
        "10 m in yd",
        "10 yard in m",
        "10 m in yard",
        "10 yards in m",
        "10 m in yards",
        "10 mi in m",
        "10 m in mi",
        "10 mile in m",
        "10 m in mile",
        "10 miles in m",
        "10 m in miles",
        "10 fur in m",
        "10 m in fur",
        "10 furlong in m",
        "10 m in furlong",
        "10 ftm in m",
        "10 m in ftm",
        "10 fathom in m",
        "10 m in fathom",
        "10 NM in m",
        "10 m in NM",
        "10 nmi in m",
        "10 m in nmi",
        "10 ly in m",
        "10 m in ly",
        "10 Å in m",
        "10 m in Å",
        "10 angstrom in m",
        "10 m in angstrom",
        "10 angstroms in m",
        "10 m in angstroms",
        "10 pm in m",
        "10 m in pm",
        "10 picometer in m",
        "10 m in picometer",
        "10 picometers in m",
        "10 m in picometers",
        "10 au in m",
        "10 m in au",
        "10 AU in m",
        "10 m in AU",
        "10 astronomical unit in m",
        "10 m in astronomical unit",
        "10 astronomical units in m",
        "10 m in astronomical units",
        "10 px in m",
        "10 m in px",
        "10 pixel in m",
        "10 m in pixel",
        "10 pixels in m",
        "10 m in pixels",
        "10 pt in m",
        "10 m in pt",
        "10 em in m",
        "10 m in em"
    ) { result ->
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
    fun `unit conversion static area`() = testCalculate(
        "10 nm² in m²",
        "10 m² in nm²",
        "10 nm2 in m²",
        "10 m² in nm2",
        "10 sqnm in m²",
        "10 m² in sqnm",
        "10 square nanometer in m²",
        "10 m² in square nanometer",
        "10 square nanometers in m²",
        "10 m² in square nanometers",
        "10 µm² in m²",
        "10 m² in µm²",
        "10 µm2 in m²",
        "10 m² in µm2",
        "10 um2 in m²",
        "10 m² in um2",
        "10 squm in m²",
        "10 m² in squm",
        "10 square micrometer in m²",
        "10 m² in square micrometer",
        "10 square micrometers in m²",
        "10 m² in square micrometers",
        "10 mm² in m²",
        "10 m² in mm²",
        "10 mm2 in m²",
        "10 m² in mm2",
        "10 sqmm in m²",
        "10 m² in sqmm",
        "10 square millimeter in m²",
        "10 m² in square millimeter",
        "10 square millimeters in m²",
        "10 m² in square millimeters",
        "10 cm² in m²",
        "10 m² in cm²",
        "10 cm2 in m²",
        "10 m² in cm2",
        "10 sqcm in m²",
        "10 m² in sqcm",
        "10 square centimeter in m²",
        "10 m² in square centimeter",
        "10 square centimeters in m²",
        "10 m² in square centimeters",
        "10 m² in m²",
        "10 m² in m²",
        "10 m2 in m²",
        "10 m² in m2",
        "10 sqm in m²",
        "10 m² in sqm",
        "10 square meter in m²",
        "10 m² in square meter",
        "10 square meters in m²",
        "10 m² in square meters",
        "10 km² in m²",
        "10 m² in km²",
        "10 km2 in m²",
        "10 m² in km2",
        "10 sqkm in m²",
        "10 m² in sqkm",
        "10 square kilometer in m²",
        "10 m² in square kilometer",
        "10 square kilometers in m²",
        "10 m² in square kilometers",
        "10 in² in m²",
        "10 m² in in²",
        "10 in2 in m²",
        "10 m² in in2",
        "10 sqin in m²",
        "10 m² in sqin",
        "10 square inch in m²",
        "10 m² in square inch",
        "10 square inches in m²",
        "10 m² in square inches",
        "10 ft² in m²",
        "10 m² in ft²",
        "10 ft2 in m²",
        "10 m² in ft2",
        "10 sqft in m²",
        "10 m² in sqft",
        "10 square foot in m²",
        "10 m² in square foot",
        "10 square feet in m²",
        "10 m² in square feet",
        "10 yd² in m²",
        "10 m² in yd²",
        "10 yd2 in m²",
        "10 m² in yd2",
        "10 sqyd in m²",
        "10 m² in sqyd",
        "10 square yard in m²",
        "10 m² in square yard",
        "10 square yards in m²",
        "10 m² in square yards",
        "10 mi² in m²",
        "10 m² in mi²",
        "10 mi2 in m²",
        "10 m² in mi2",
        "10 sqmi in m²",
        "10 m² in sqmi",
        "10 square mile in m²",
        "10 m² in square mile",
        "10 square miles in m²",
        "10 m² in square miles",
        "10 ac in m²",
        "10 m² in ac",
        "10 acre in m²",
        "10 m² in acre",
        "10 acres in m²",
        "10 m² in acres",
        "10 ha in m²",
        "10 m² in ha",
        "10 hectare in m²",
        "10 m² in hectare",
        "10 hectares in m²",
        "10 m² in hectares"
    ) { result ->
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
    fun `unit conversion static volume`() = testCalculate(
        "10 mL in L",
        "10 L in mL",
        "10 ml in L",
        "10 L in ml",
        "10 milliliter in L",
        "10 L in milliliter",
        "10 milliliters in L",
        "10 L in milliliters",
        "10 L in L",
        "10 L in L",
        "10 l in L",
        "10 L in l",
        "10 liter in L",
        "10 L in liter",
        "10 liters in L",
        "10 L in liters",
        "10 kL in L",
        "10 L in kL",
        "10 kl in L",
        "10 L in kl",
        "10 kiloliter in L",
        "10 L in kiloliter",
        "10 kiloliters in L",
        "10 L in kiloliters",
        "10 ML in L",
        "10 L in ML",
        "10 megaliter in L",
        "10 L in megaliter",
        "10 megaliters in L",
        "10 L in megaliters",
        "10 cm³ in L",
        "10 L in cm³",
        "10 cm3 in L",
        "10 L in cm3",
        "10 cc in L",
        "10 L in cc",
        "10 cubic centimeter in L",
        "10 L in cubic centimeter",
        "10 cubic centimeters in L",
        "10 L in cubic centimeters",
        "10 m³ in L",
        "10 L in m³",
        "10 m3 in L",
        "10 L in m3",
        "10 cubic meter in L",
        "10 L in cubic meter",
        "10 cubic meters in L",
        "10 L in cubic meters",
        "10 dL in L",
        "10 L in dL",
        "10 dl in L",
        "10 L in dl",
        "10 deciliter in L",
        "10 L in deciliter",
        "10 deciliters in L",
        "10 L in deciliters",
        "10 cL in L",
        "10 L in cL",
        "10 cl in L",
        "10 L in cl",
        "10 centiliter in L",
        "10 L in centiliter",
        "10 centiliters in L",
        "10 L in centiliters",
        "10 µL in L",
        "10 L in µL",
        "10 uL in L",
        "10 L in uL",
        "10 µl in L",
        "10 L in µl",
        "10 ul in L",
        "10 L in ul",
        "10 microliter in L",
        "10 L in microliter",
        "10 microliters in L",
        "10 L in microliters",
        "10 mm³ in L",
        "10 L in mm³",
        "10 mm3 in L",
        "10 L in mm3",
        "10 cubic millimeter in L",
        "10 L in cubic millimeter",
        "10 cubic millimeters in L",
        "10 L in cubic millimeters",
        "10 gal in L",
        "10 L in gal",
        "10 gallon in L",
        "10 L in gallon",
        "10 gallons in L",
        "10 L in gallons",
        "10 US gallon in L",
        "10 L in US gallon",
        "10 US gallons in L",
        "10 L in US gallons",
        "10 qt in L",
        "10 L in qt",
        "10 quart in L",
        "10 L in quart",
        "10 quarts in L",
        "10 L in quarts",
        "10 US quarts in L",
        "10 L in US quarts",
        "10 pint in L",
        "10 L in pint",
        "10 pints in L",
        "10 L in pints",
        "10 US pints in L",
        "10 L in US pints",
        "10 cup in L",
        "10 L in cup",
        "10 cups in L",
        "10 L in cups",
        "10 US cups in L",
        "10 L in US cups",
        "10 fl oz in L",
        "10 L in fl oz",
        "10 floz in L",
        "10 L in floz",
        "10 fluid ounce in L",
        "10 L in fluid ounce",
        "10 fluid ounces in L",
        "10 L in fluid ounces",
        "10 US fluid ounces in L",
        "10 L in US fluid ounces",
        "10 gal_imp in L",
        "10 L in gal_imp",
        "10 imperial gallon in L",
        "10 L in imperial gallon",
        "10 imperial gallons in L",
        "10 L in imperial gallons",
        "10 qt_imp in L",
        "10 L in qt_imp",
        "10 imperial quart in L",
        "10 L in imperial quart",
        "10 imperial quarts in L",
        "10 L in imperial quarts",
        "10 pint_imp in L",
        "10 L in pint_imp",
        "10 imperial pint in L",
        "10 L in imperial pint",
        "10 imperial pints in L",
        "10 L in imperial pints",
        "10 fl_oz_imp in L",
        "10 L in fl_oz_imp",
        "10 imperial fluid ounce in L",
        "10 L in imperial fluid ounce",
        "10 imperial fluid ounces in L",
        "10 L in imperial fluid ounces",
        "10 gi_us in L",
        "10 L in gi_us",
        "10 US gill in L",
        "10 L in US gill",
        "10 US gills in L",
        "10 L in US gills",
        "10 gi_imp in L",
        "10 L in gi_imp",
        "10 imperial gill in L",
        "10 L in imperial gill",
        "10 imperial gills in L",
        "10 L in imperial gills",
        "10 tbsp in L",
        "10 L in tbsp",
        "10 tablespoon in L",
        "10 L in tablespoon",
        "10 tablespoons in L",
        "10 L in tablespoons",
        "10 tsp in L",
        "10 L in tsp",
        "10 teaspoon in L",
        "10 L in teaspoon",
        "10 teaspoons in L",
        "10 L in teaspoons",
        "10 in³ in L",
        "10 L in in³",
        "10 in3 in L",
        "10 L in in3",
        "10 cubic inch in L",
        "10 L in cubic inch",
        "10 cubic inches in L",
        "10 L in cubic inches",
        "10 ft³ in L",
        "10 L in ft³",
        "10 ft3 in L",
        "10 L in ft3",
        "10 cuft in L",
        "10 L in cuft",
        "10 cubic foot in L",
        "10 L in cubic foot",
        "10 cubic feet in L",
        "10 L in cubic feet"
    ) { result ->
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
    fun `unit conversion static mass`() = testCalculate(
        "10 ng in g",
        "10 g in ng",
        "10 nanogram in g",
        "10 g in nanogram",
        "10 nanograms in g",
        "10 g in nanograms",
        "10 mcg in g",
        "10 g in mcg",
        "10 µg in g",
        "10 g in µg",
        "10 ug in g",
        "10 g in ug",
        "10 microgram in g",
        "10 g in microgram",
        "10 micrograms in g",
        "10 g in micrograms",
        "10 mg in g",
        "10 g in mg",
        "10 milligram in g",
        "10 g in milligram",
        "10 milligrams in g",
        "10 g in milligrams",
        "10 g in g",
        "10 g in g",
        "10 gram in g",
        "10 g in gram",
        "10 grams in g",
        "10 g in grams",
        "10 kg in g",
        "10 g in kg",
        "10 kgs in g",
        "10 g in kgs",
        "10 kilograms in g",
        "10 g in kilograms",
        "10 t in g",
        "10 g in t",
        "10 tonne in g",
        "10 g in tonne",
        "10 tonnes in g",
        "10 g in tonnes",
        "10 ton in g",
        "10 g in ton",
        "10 tons in g",
        "10 g in tons",
        "10 metric ton in g",
        "10 g in metric ton",
        "10 metric tons in g",
        "10 g in metric tons",
        "10 metric tonne in g",
        "10 g in metric tonne",
        "10 metric tonnes in g",
        "10 g in metric tonnes",
        "10 oz in g",
        "10 g in oz",
        "10 ounce in g",
        "10 g in ounce",
        "10 ounces in g",
        "10 g in ounces",
        "10 lb in g",
        "10 g in lb",
        "10 lbs in g",
        "10 g in lbs",
        "10 pound in g",
        "10 g in pound",
        "10 pounds in g",
        "10 g in pounds",
        "10 st in g",
        "10 g in st",
        "10 stone in g",
        "10 g in stone",
        "10 stones in g",
        "10 g in stones",
        "10 sh ton in g",
        "10 g in sh ton",
        "10 short ton in g",
        "10 g in short ton",
        "10 short tons in g",
        "10 g in short tons",
        "10 ozt in g",
        "10 g in ozt",
        "10 oz t in g",
        "10 g in oz t",
        "10 troy ounce in g",
        "10 g in troy ounce",
        "10 troy ounces in g",
        "10 g in troy ounces",
        "10 ct in g",
        "10 g in ct",
        "10 carat in g",
        "10 g in carat",
        "10 carats in g",
        "10 g in carats",
        "10 hg in g",
        "10 g in hg",
        "10 ettogram in g",
        "10 g in ettogram",
        "10 ettograms in g",
        "10 g in ettograms",
        "10 cg in g",
        "10 g in cg",
        "10 centigram in g",
        "10 g in centigram",
        "10 centigrams in g",
        "10 g in centigrams",
        "10 q in g",
        "10 g in q",
        "10 quintal in g",
        "10 g in quintal",
        "10 quintals in g",
        "10 g in quintals",
        "10 dwt in g",
        "10 g in dwt",
        "10 pennyweight in g",
        "10 g in pennyweight",
        "10 u in g",
        "10 g in u",
        "10 amu in g",
        "10 g in amu"
    ) { result ->
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
    fun `unit conversion static speed`() = testCalculate(
        "10 mps in mps",
        "10 mps in mps",
        "10 meters per second in mps",
        "10 mps in meters per second",
        "10 kmh in mps",
        "10 mps in kmh",
        "10 kph in mps",
        "10 mps in kph",
        "10 kilometers per hour in mps",
        "10 mps in kilometers per hour",
        "10 mph in mps",
        "10 mps in mph",
        "10 miles per hour in mps",
        "10 mps in miles per hour",
        "10 kn in mps",
        "10 mps in kn",
        "10 knot in mps",
        "10 mps in knot",
        "10 knots in mps",
        "10 mps in knots",
        "10 fps in mps",
        "10 mps in fps",
        "10 feet per second in mps",
        "10 mps in feet per second",
        "10 speed of light in mps",
        "10 mps in speed of light",
        "10 speed of light in mps",
        "10 mps in speed of light"
    ) { result ->
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
    fun `unit conversion static angle`() = testCalculate(
        "10 rad in rad",
        "10 rad in rad",
        "10 radian in rad",
        "10 rad in radian",
        "10 radians in rad",
        "10 rad in radians",
        "10 deg in rad",
        "10 rad in deg",
        "10 degree in rad",
        "10 rad in degree",
        "10 degrees in rad",
        "10 rad in degrees",
        "10 ° in rad",
        "10 rad in °",
        "10 arcmin in rad",
        "10 rad in arcmin",
        "10 minute of arc in rad",
        "10 rad in minute of arc",
        "10 arcsec in rad",
        "10 rad in arcsec",
        "10 second of arc in rad",
        "10 rad in second of arc"
    ) { result ->
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
    fun `unit conversion static temperature`() = testCalculate(
        "10 °C in °C",
        "10 °C in °C",
        "10 C in °C",
        "10 °C in C",
        "10 celsius in °C",
        "10 °C in celsius",
        "10 degC in °C",
        "10 °C in degC",
        "10 degree celsius in °C",
        "10 °C in degree celsius",
        "10 °F in °C",
        "10 °C in °F",
        "10 F in °C",
        "10 °C in F",
        "10 fahrenheit in °C",
        "10 °C in fahrenheit",
        "10 degF in °C",
        "10 °C in degF",
        "10 degree fahrenheit in °C",
        "10 °C in degree fahrenheit",
        "10 K in °C",
        "10 °C in K",
        "10 kelvin in °C",
        "10 °C in kelvin",
        "10 °Re in °C",
        "10 °C in °Re",
        "10 Re in °C",
        "10 °C in Re",
        "10 reaumur in °C",
        "10 °C in reaumur",
        "10 Réaumur in °C",
        "10 °C in Réaumur",
        "10 °Rø in °C",
        "10 °C in °Rø",
        "10 Rø in °C",
        "10 °C in Rø",
        "10 romer in °C",
        "10 °C in romer",
        "10 Rømer in °C",
        "10 °C in Rømer",
        "10 °De in °C",
        "10 °C in °De",
        "10 De in °C",
        "10 °C in De",
        "10 delisle in °C",
        "10 °C in delisle",
        "10 °Ra in °C",
        "10 °C in °Ra",
        "10 Ra in °C",
        "10 °C in Ra",
        "10 rankine in °C",
        "10 °C in rankine"
    ) { result ->
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
    fun `unit conversion static frequency`() = testCalculate(
        "10 Hz in Hz",
        "10 Hz in Hz",
        "10 hertz in Hz",
        "10 Hz in hertz",
        "10 kHz in Hz",
        "10 Hz in kHz",
        "10 kilohertz in Hz",
        "10 Hz in kilohertz",
        "10 MHz in Hz",
        "10 Hz in MHz",
        "10 megahertz in Hz",
        "10 Hz in megahertz",
        "10 GHz in Hz",
        "10 Hz in GHz",
        "10 gigahertz in Hz",
        "10 Hz in gigahertz"
    ) { result ->
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
    fun `unit conversion static energy`() = testCalculate(
        "10 J in J",
        "10 J in J",
        "10 joule in J",
        "10 J in joule",
        "10 joules in J",
        "10 J in joules",
        "10 kJ in J",
        "10 J in kJ",
        "10 kilojoule in J",
        "10 J in kilojoule",
        "10 kilojoules in J",
        "10 J in kilojoules",
        "10 MJ in J",
        "10 J in MJ",
        "10 megajoule in J",
        "10 J in megajoule",
        "10 megajoules in J",
        "10 J in megajoules",
        "10 cal in J",
        "10 J in cal",
        "10 calorie in J",
        "10 J in calorie",
        "10 calories in J",
        "10 J in calories",
        "10 kCal in J",
        "10 J in kCal",
        "10 kcal in J",
        "10 J in kcal",
        "10 kilocalorie in J",
        "10 J in kilocalorie",
        "10 kilocalories in J",
        "10 J in kilocalories",
        "10 Wh in J",
        "10 J in Wh",
        "10 watt hour in J",
        "10 J in watt hour",
        "10 watt hours in J",
        "10 J in watt hours",
        "10 kWh in J",
        "10 J in kWh",
        "10 kilowatt hour in J",
        "10 J in kilowatt hour",
        "10 kilowatt hours in J",
        "10 J in kilowatt hours",
        "10 eV in J",
        "10 J in eV",
        "10 electronvolt in J",
        "10 J in electronvolt",
        "10 electron volts in J",
        "10 J in electron volts",
        "10 ft_lbf in J",
        "10 J in ft_lbf",
        "10 ft_lbf in J",
        "10 J in ft_lbf",
        "10 foot_pound in J",
        "10 J in foot_pound",
        "10 BTU in J",
        "10 J in BTU",
        "10 btu in J",
        "10 J in btu"
    ) { result ->
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
    fun `unit conversion static power`() = testCalculate(
        "10 W in W",
        "10 W in W",
        "10 watt in W",
        "10 W in watt",
        "10 watts in W",
        "10 W in watts",
        "10 mW in W",
        "10 W in mW",
        "10 milliwatt in W",
        "10 W in milliwatt",
        "10 milliwatts in W",
        "10 W in milliwatts",
        "10 kW in W",
        "10 W in kW",
        "10 kilowatt in W",
        "10 W in kilowatt",
        "10 kilowatts in W",
        "10 W in kilowatts",
        "10 MW in W",
        "10 W in MW",
        "10 megawatt in W",
        "10 W in megawatt",
        "10 megawatts in W",
        "10 W in megawatts",
        "10 hp in W",
        "10 W in hp",
        "10 horsepower in W",
        "10 W in horsepower",
        "10 horsepowers in W",
        "10 W in horsepowers",
        "10 GW in W",
        "10 W in GW",
        "10 gigawatt in W",
        "10 W in gigawatt",
        "10 gigawatts in W",
        "10 W in gigawatts"
    ) { result ->
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
    fun `unit conversion static data`() = testCalculate(
        "10 bit in B",
        "10 B in bit",
        "10 bits in B",
        "10 B in bits",
        "10 b in B",
        "10 B in b",
        "10 nibble in B",
        "10 B in nibble",
        "10 nibbles in B",
        "10 B in nibbles",
        "10 B in B",
        "10 B in B",
        "10 byte in B",
        "10 B in byte",
        "10 bytes in B",
        "10 B in bytes",
        "10 kB in B",
        "10 B in kB",
        "10 KB in B",
        "10 B in KB",
        "10 kilobyte in B",
        "10 B in kilobyte",
        "10 kilobytes in B",
        "10 B in kilobytes",
        "10 MB in B",
        "10 B in MB",
        "10 megabyte in B",
        "10 B in megabyte",
        "10 megabytes in B",
        "10 B in megabytes",
        "10 GB in B",
        "10 B in GB",
        "10 gigabyte in B",
        "10 B in gigabyte",
        "10 gigabytes in B",
        "10 B in gigabytes",
        "10 TB in B",
        "10 B in TB",
        "10 terabyte in B",
        "10 B in terabyte",
        "10 terabytes in B",
        "10 B in terabytes",
        "10 KiB in B",
        "10 B in KiB",
        "10 kibibyte in B",
        "10 B in kibibyte",
        "10 kibibytes in B",
        "10 B in kibibytes",
        "10 MiB in B",
        "10 B in MiB",
        "10 mebibyte in B",
        "10 B in mebibyte",
        "10 mebibytes in B",
        "10 B in mebibytes",
        "10 GiB in B",
        "10 B in GiB",
        "10 gibibyte in B",
        "10 B in gibibyte",
        "10 gibibytes in B",
        "10 B in gibibytes",
        "10 TiB in B",
        "10 B in TiB",
        "10 tebibyte in B",
        "10 B in tebibyte",
        "10 tebibytes in B",
        "10 B in tebibytes",
        "10 PiB in B",
        "10 B in PiB",
        "10 pebibyte in B",
        "10 B in pebibyte",
        "10 pebibytes in B",
        "10 B in pebibytes",
        "10 EiB in B",
        "10 B in EiB",
        "10 exbibyte in B",
        "10 B in exbibyte",
        "10 exbibytes in B",
        "10 B in exbibytes",
        "10 PB in B",
        "10 B in PB",
        "10 petabyte in B",
        "10 B in petabyte",
        "10 petabytes in B",
        "10 B in petabytes",
        "10 EB in B",
        "10 B in EB",
        "10 exabyte in B",
        "10 B in exabyte",
        "10 exabytes in B",
        "10 B in exabytes",
        "10 Kibit in B",
        "10 B in Kibit",
        "10 kibibit in B",
        "10 B in kibibit",
        "10 Mibit in B",
        "10 B in Mibit",
        "10 mebibit in B",
        "10 B in mebibit",
        "10 Gibit in B",
        "10 B in Gibit",
        "10 gibibit in B",
        "10 B in gibibit",
        "10 Tibit in B",
        "10 B in Tibit",
        "10 tebibit in B",
        "10 B in tebibit",
        "10 Pibit in B",
        "10 B in Pibit",
        "10 pebibit in B",
        "10 B in pebibit",
        "10 Eibit in B",
        "10 B in Eibit",
        "10 exbibit in B",
        "10 B in exbibit",
        "10 kb in B",
        "10 B in kb",
        "10 kilobit in B",
        "10 B in kilobit",
        "10 Mb in B",
        "10 B in Mb",
        "10 megabit in B",
        "10 B in megabit",
        "10 Gb in B",
        "10 B in Gb",
        "10 gigabit in B",
        "10 B in gigabit",
        "10 Tb in B",
        "10 B in Tb",
        "10 terabit in B",
        "10 B in terabit",
        "10 Pb in B",
        "10 B in Pb",
        "10 petabit in B",
        "10 B in petabit",
        "10 Eb in B",
        "10 B in Eb",
        "10 exabit in B",
        "10 B in exabit"
    ) { result ->
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
    fun `unit conversion static force`() = testCalculate(
        "10 N in N",
        "10 N in N",
        "10 newton in N",
        "10 N in newton",
        "10 newtons in N",
        "10 N in newtons",
        "10 kgf in N",
        "10 N in kgf",
        "10 kg_f in N",
        "10 N in kg_f",
        "10 lbf in N",
        "10 N in lbf",
        "10 lb_f in N",
        "10 N in lb_f",
        "10 dyn in N",
        "10 N in dyn",
        "10 dyne in N",
        "10 N in dyne",
        "10 pdl in N",
        "10 N in pdl"
    ) { result ->
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
    fun `unit conversion static pressure`() = testCalculate(
        "10 Pa in Pa",
        "10 Pa in Pa",
        "10 pascal in Pa",
        "10 Pa in pascal",
        "10 kPa in Pa",
        "10 Pa in kPa",
        "10 kilopascal in Pa",
        "10 Pa in kilopascal",
        "10 MPa in Pa",
        "10 Pa in MPa",
        "10 megapascal in Pa",
        "10 Pa in megapascal",
        "10 GPa in Pa",
        "10 Pa in GPa",
        "10 gigapascal in Pa",
        "10 Pa in gigapascal",
        "10 hPa in Pa",
        "10 Pa in hPa",
        "10 hectopascal in Pa",
        "10 Pa in hectopascal",
        "10 bar in Pa",
        "10 Pa in bar",
        "10 bars in Pa",
        "10 Pa in bars",
        "10 mbar in Pa",
        "10 Pa in mbar",
        "10 millibar in Pa",
        "10 Pa in millibar",
        "10 atm in Pa",
        "10 Pa in atm",
        "10 atmosphere in Pa",
        "10 Pa in atmosphere",
        "10 psi in Pa",
        "10 Pa in psi",
        "10 pound per square inch in Pa",
        "10 Pa in pound per square inch",
        "10 ksi in Pa",
        "10 Pa in ksi",
        "10 torr in Pa",
        "10 Pa in torr",
        "10 mmHg in Pa",
        "10 Pa in mmHg",
        "10 inHg in Pa",
        "10 Pa in inHg"
    ) { result ->
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
    fun `unit conversion static numeral system`() = testCalculate(
        "10 dec in dec",
        "10 dec in dec",
        "10 decimal in dec",
        "10 dec in decimal",
        "10 hex in dec",
        "10 dec in hex",
        "10 hexadecimal in dec",
        "10 dec in hexadecimal",
        "10 oct in dec",
        "10 dec in oct",
        "10 octal in dec",
        "10 dec in octal",
        "10 bin in dec",
        "10 dec in bin",
        "10 binary in dec",
        "10 dec in binary"
    ) { result ->
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
