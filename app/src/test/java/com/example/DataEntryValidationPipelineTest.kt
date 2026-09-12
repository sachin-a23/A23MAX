package com.example

import com.example.engine.DataEntryValidationPipeline
import com.example.model.RecordValidationStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * End-to-End Test Suite for A23MAX Multi-Market Data Architecture and Validation Pipeline.
 * Tests:
 * 1. Dynamic Market Creation & Recognition
 * 2. Single Record Validation (Open/Jodi/Close)
 * 3. Holiday (*** - ** - ***) Processing
 * 4. Multi-market Bulk Ingestion & Segmentation
 * 5. Duplicate Detection & Prevention
 * 6. Malformed Data Flagging
 * 7. Canonical Record Conversion
 */
class DataEntryValidationPipelineTest {

    @Test
    fun test1_dynamicNewMarketCreation() {
        val knownMarkets = listOf("KALYAN", "MAIN BAZAR")
        val parsed = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "SRIDEVI NIGHT",
            dateInput = "2026-09-11",
            openPanaInput = "465",
            jodiInput = "27",
            closePanaInput = "389",
            isHolidayInput = false,
            existingDatesForMarket = emptySet(),
            knownMarkets = knownMarkets
        )

        assertEquals("SRIDEVI NIGHT", parsed.marketName)
        assertEquals("2026-09-11", parsed.date)
        assertEquals(RecordValidationStatus.VALID, parsed.status)
        assertTrue("Market should be flagged as new", parsed.isMarketNew)
    }

    @Test
    fun test2_singleValidDataEntry() {
        val parsed = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "KALYAN",
            dateInput = "11-09-2026",
            openPanaInput = "465",
            jodiInput = "27",
            closePanaInput = "389",
            isHolidayInput = false,
            existingDatesForMarket = emptySet(),
            knownMarkets = listOf("KALYAN")
        )

        assertEquals("2026-09-11", parsed.date)
        assertEquals("465", parsed.openPana)
        assertEquals("27", parsed.jodi)
        assertEquals("389", parsed.closePana)
        assertEquals(RecordValidationStatus.VALID, parsed.status)
        assertFalse(parsed.isMarketNew)
    }

    @Test
    fun test3_holidayMarkerHandling() {
        val parsed = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "MILAN",
            dateInput = "2026-09-11",
            openPanaInput = "***",
            jodiInput = "**",
            closePanaInput = "***",
            isHolidayInput = true,
            existingDatesForMarket = emptySet(),
            knownMarkets = listOf("MILAN")
        )

        assertEquals(RecordValidationStatus.HOLIDAY, parsed.status)
        assertTrue(parsed.isHoliday)
        assertEquals("***", parsed.openPana)
        assertEquals("**", parsed.jodi)
        assertEquals("***", parsed.closePana)
    }

    @Test
    fun test4_duplicateDetectionNonDestructive() {
        val existingDates = setOf("2026-09-11", "2026-09-10")
        val parsed = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "TIME BAZAR",
            dateInput = "2026-09-11",
            openPanaInput = "123",
            jodiInput = "45",
            closePanaInput = "678",
            isHolidayInput = false,
            existingDatesForMarket = existingDates,
            knownMarkets = listOf("TIME BAZAR")
        )

        assertEquals(RecordValidationStatus.DUPLICATE, parsed.status)
        assertNotNull(parsed.validationError)
        assertTrue(parsed.validationError!!.contains("already exists"))
    }

    @Test
    fun test5_malformedDataFlagging() {
        // Bad Open Pana (2 digits instead of 3)
        val parsedBadPana = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "KALYAN",
            dateInput = "2026-09-11",
            openPanaInput = "46",
            jodiInput = "27",
            closePanaInput = "389",
            isHolidayInput = false,
            existingDatesForMarket = emptySet(),
            knownMarkets = listOf("KALYAN")
        )
        assertEquals(RecordValidationStatus.INVALID, parsedBadPana.status)

        // Bad Date format
        val parsedBadDate = DataEntryValidationPipeline.validateSingleRecord(
            marketName = "KALYAN",
            dateInput = "invalid-date",
            openPanaInput = "465",
            jodiInput = "27",
            closePanaInput = "389",
            isHolidayInput = false,
            existingDatesForMarket = emptySet(),
            knownMarkets = listOf("KALYAN")
        )
        assertEquals(RecordValidationStatus.INVALID, parsedBadDate.status)
    }

    @Test
    fun test6_multiMarketBulkEntryParsingAndGrouping() {
        val rawBulk = """
            Sridevi | 2026-09-01 | 236-11-290
            Sridevi | 2026-09-02 | 560-14-266
            Milan   | 2026-09-01 | 123-45-678
            Milan   | 2026-09-02 | 456-78-901
            Kalyan  | 2026-09-01 | 111-22-333
            Kalyan  | 2026-09-02 | ***-**-***
        """.trimIndent()

        val preview = DataEntryValidationPipeline.parseAndValidateBulk(
            rawBulkText = rawBulk,
            defaultMarketName = "KALYAN",
            existingDatesByMarket = emptyMap(),
            knownMarkets = listOf("KALYAN", "MILAN", "SRIDEVI")
        )

        assertEquals(6, preview.totalCount)
        assertEquals(5, preview.validCount)
        assertEquals(1, preview.holidayCount)
        assertEquals(0, preview.invalidCount)
        assertEquals(0, preview.duplicateCount)
        assertTrue(preview.hasMultipleMarkets)

        val srideviRecords = preview.parsedRecords.filter { it.marketName == "SRIDEVI" }
        assertEquals(2, srideviRecords.size)

        val milanRecords = preview.parsedRecords.filter { it.marketName == "MILAN" }
        assertEquals(2, milanRecords.size)

        val kalyanRecords = preview.parsedRecords.filter { it.marketName == "KALYAN" }
        assertEquals(2, kalyanRecords.size)
        assertTrue(kalyanRecords.any { it.isHoliday })
    }

    @Test
    fun test7_multiMarketBulkWithDuplicatesAndInvalids() {
        val existingDatesByMarket = mapOf(
            "SRIDEVI" to setOf("2026-09-01")
        )

        val rawBulk = """
            Sridevi | 2026-09-01 | 236-11-290
            Sridevi | 2026-09-02 | 560-14-266
            UnknownMarket | 2026-09-03 | 123-45-678
            Kalyan | 2026-09-04 | invalid_numbers
        """.trimIndent()

        val preview = DataEntryValidationPipeline.parseAndValidateBulk(
            rawBulkText = rawBulk,
            defaultMarketName = "SRIDEVI",
            existingDatesByMarket = existingDatesByMarket,
            knownMarkets = listOf("SRIDEVI", "KALYAN")
        )

        assertEquals(4, preview.totalCount)
        assertEquals(2, preview.validCount) // Sridevi 09-02 & UnknownMarket 09-03
        assertEquals(1, preview.duplicateCount) // Sridevi 09-01
        assertEquals(1, preview.invalidCount) // Kalyan invalid_numbers
        assertTrue(preview.newMarketsDetected.contains("UNKNOWNMARKET"))
    }
}
