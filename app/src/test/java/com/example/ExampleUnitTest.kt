package com.example

import com.example.data.A23Repository
import com.example.util.DateUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDateExtractionAndNormalization() {
        val test1 = "25-08-2026 / 470 - 17 - 179"
        val res1 = DateUtils.extractAndNormalizeDate(test1)
        assertEquals("25-08-2026", res1.first)
        assertTrue(res1.second.contains("470"))

        val test2 = "05/03/2025: 123-45-678"
        val res2 = DateUtils.extractAndNormalizeDate(test2)
        assertEquals("05-03-2025", res2.first)

        val test3 = "2026-08-25  470 - 17 - 179"
        val res3 = DateUtils.extractAndNormalizeDate(test3)
        assertEquals("25-08-2026", res3.first)

        val test4 = "15.06.24 (Sat) *** - ** - ***"
        val res4 = DateUtils.extractAndNormalizeDate(test4)
        assertEquals("15-06-2024", res4.first)
    }

    @Test
    fun testBulk500DaysDataParsing() = runBlocking {
        val repo = A23Repository()
        val sb = StringBuilder()
        sb.append("[KALYAN]\n")
        
        // Generate 300 days of history
        for (i in 1..300) {
            val day = (i % 28) + 1
            val month = (i % 12) + 1
            val year = 2025
            val dateStr = String.format("%02d-%02d-%04d", day, month, year)
            if (i % 7 == 0) {
                sb.append("$dateStr / *** - ** - ***\n")
            } else {
                sb.append("$dateStr / 470 - 17 - 179\n")
            }
        }

        val report = repo.processSyncPayload(sb.toString(), "Test")
        assertTrue(report.isSuccess)
        assertEquals(1, report.totalMarkets)
        assertTrue(report.totalDaysHistory > 0)
        
        val kalyanHistory = repo.getMarketHistory("KALYAN")
        assertTrue(kalyanHistory.isNotEmpty())
        
        val summary = repo.getMarketSummary("KALYAN")
        assertEquals(kalyanHistory.size, summary.totalDays)
        assertTrue(summary.holidayDays > 0)
    }

    @Test
    fun testPanelChartGeneration() = runBlocking {
        val repo = A23Repository()
        val raw = """
            [KALYAN]
            25-08-2026 / 470 - 17 - 179
            24-08-2026 / 358 - 60 - 280
            23-08-2026 / *** - ** - ***
            22-08-2026 / 123 - 45 - 678
        """.trimIndent()
        repo.processSyncPayload(raw, "Test")

        val chart = repo.getPanelChartData("KALYAN")
        assertEquals("KALYAN", chart.marketName)
        assertTrue(chart.totalDays > 0)
    }
}


