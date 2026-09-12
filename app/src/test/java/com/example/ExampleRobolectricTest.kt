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
    assertEquals(34185L, sridevi.step1Result)
    assertEquals(3798.33, sridevi.step2Result, 0.1)
    assertEquals(listOf(3, 7, 9, 8), sridevi.otcDigits)
    assertTrue(sridevi.superJodis.isNotEmpty())
    assertTrue(sridevi.superJodis.all { it.length == 2 })

    // TIMEBAZAR: OpenPana=148, Jodi=60, Divisor=9
    val timebazar = FormulaCalculator.calculate(148, 60, 9)
    assertEquals(30784L, timebazar.step1Result)
    assertEquals(listOf(3, 4, 2, 0), timebazar.otcDigits)
    assertTrue(timebazar.superJodis.isNotEmpty())
    assertTrue(timebazar.superJodis.all { it.length == 2 })

    // MILAN: OpenPana=156, Jodi=80, Divisor=9
    val milan = FormulaCalculator.calculate(156, 80, 9)
    assertEquals(36816L, milan.step1Result)
    assertTrue(milan.otcDigits.containsAll(listOf(4, 0, 9)))
    assertTrue(milan.superJodis.isNotEmpty())
    assertTrue(milan.superJodis.all { it.length == 2 })

    // KALYAN: OpenPana=156, Jodi=81, Divisor=9
    val kalyan = FormulaCalculator.calculate(156, 81, 9)
    assertEquals(36972L, kalyan.step1Result)
    assertEquals(listOf(4, 1, 0, 8), kalyan.otcDigits)
    assertTrue(kalyan.superJodis.isNotEmpty())
    assertTrue(kalyan.superJodis.all { it.length == 2 })
  }
}
