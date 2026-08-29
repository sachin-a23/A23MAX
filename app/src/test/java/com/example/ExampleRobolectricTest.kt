package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FormulaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("A23MAX", appName)
  }

  @Test
  fun `verify formula calculation step 1 and step 2`() {
    // SRIDEVI: OpenPana=159, Jodi=56, Divisor=9
    val sridevi = FormulaCalculator.calculate(159, 56, 9)
    assertEquals("Step 1: (159 + 56) × 159 = 34185", sridevi.step1Formula)
    assertEquals(34185L, sridevi.step1Result)
    assertEquals("Step 2: 34185 ÷ 9 = 3798", sridevi.step2Formula)
    assertEquals(3798L, sridevi.step2Result)
    assertEquals(listOf(3, 7, 9, 8), sridevi.otcDigits)
    assertTrue(sridevi.superJodis.isNotEmpty())
    assertTrue(sridevi.superJodis.all { it.length == 2 })

    // TIMEBAZAR: OpenPana=148, Jodi=60, Divisor=9
    val timebazar = FormulaCalculator.calculate(148, 60, 9)
    assertEquals("Step 1: (148 + 60) × 148 = 30784", timebazar.step1Formula)
    assertEquals("Step 2: 30784 ÷ 9 = 3420", timebazar.step2Formula)
    assertEquals(listOf(3, 4, 2, 0), timebazar.otcDigits)
    assertTrue(timebazar.superJodis.isNotEmpty())
    assertTrue(timebazar.superJodis.all { it.length == 2 })

    // MILAN: OpenPana=156, Jodi=80, Divisor=9
    val milan = FormulaCalculator.calculate(156, 80, 9)
    assertEquals("Step 1: (156 + 80) × 156 = 36816", milan.step1Formula)
    assertEquals("Step 2: 36816 ÷ 9 = 4090", milan.step2Formula)
    assertTrue(milan.otcDigits.containsAll(listOf(4, 0, 9)))
    assertTrue(milan.superJodis.isNotEmpty())
    assertTrue(milan.superJodis.all { it.length == 2 })

    // KALYAN: OpenPana=156, Jodi=81, Divisor=9
    val kalyan = FormulaCalculator.calculate(156, 81, 9)
    assertEquals("Step 1: (156 + 81) × 156 = 36972", kalyan.step1Formula)
    assertEquals("Step 2: 36972 ÷ 9 = 4108", kalyan.step2Formula)
    assertEquals(listOf(4, 1, 0, 8), kalyan.otcDigits)
    assertTrue(kalyan.superJodis.isNotEmpty())
    assertTrue(kalyan.superJodis.all { it.length == 2 })
  }
}
