package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.FormulaCalculator
import com.example.model.MarketPrediction
import com.example.ui.components.CalculationCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun formula_card_screenshot() {
    val calc = FormulaCalculator.calculate(159, 56, 9)
    val testPrediction = MarketPrediction(
        id = "sridevi",
        marketName = "SRIDEVI",
        date = "23-08-2026",
        lastEntryDate = "22-08-2026",
        lastOpenPana = "159",
        lastJodi = "56",
        lastClosePana = "647",
        openNumber = "5",
        closeNumber = "7",
        isPassed = true,
        otcList = calc.otcDigits,
        highlightedOtc = 7,
        jodiList = calc.superJodis,
        panneList = calc.pannes,
        step1Formula = calc.step1Formula,
        step1Result = calc.step1Result,
        step2Formula = calc.step2Formula,
        step2Result = calc.step2Result.toLong(),
        step3Formula = calc.step3Formula,
        calculatedOtcDigits = calc.otcDigits,
        superJodiList = calc.superJodis
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        CalculationCard(
            prediction = testPrediction
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

