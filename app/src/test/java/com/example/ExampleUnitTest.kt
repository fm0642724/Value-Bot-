package com.example

import com.example.domain.CrosshairConfig
import com.example.domain.CrosshairStyle
import com.example.domain.SensitivityEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun sensitivityEngine_calculatesValuesWithinValidRange() {
    val result = SensitivityEngine.calculate(
      ramGb = 8,
      screenInches = 6.67f,
      refreshRateHz = 120,
      chipsetClass = "High",
      fingerCount = 4,
      playFocus = "Headshot"
    )
    val config = result.config

    assertTrue("General sensitivity should be between 0 and 100", config.general in 0..100)
    assertTrue("Red Dot sensitivity should be between 0 and 100", config.redDot in 0..100)
    assertTrue("2X Scope sensitivity should be between 0 and 100", config.scope2x in 0..100)
    assertTrue("4X Scope sensitivity should be between 0 and 100", config.scope4x in 0..100)
    assertTrue("Sniper sensitivity should be between 0 and 100", config.sniperScope in 0..100)
    assertTrue("Free Look sensitivity should be between 0 and 100", config.freeLook in 0..100)
    assertNotNull(result.breakdown.formulaSummary)
    assertNotNull(result.breakdown.formulaSummaryAr)
  }

  @Test
  fun crosshairConfig_defaultStylesExist() {
    val styles = CrosshairStyle.entries
    assertEquals(12, styles.size)
    val defaultCfg = CrosshairConfig()
    assertEquals(CrosshairStyle.CIRCLE_DOT, defaultCfg.style)
    assertEquals(50f, defaultCfg.posXPercent)
    assertEquals(50f, defaultCfg.posYPercent)
  }
}
