package com.zclei.hitrise

import com.zclei.hitrise.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalizationTest {
    @Test
    fun allSupportedLanguageCodesRoundTrip() {
        val expectedCodes = listOf("zh", "en", "fr", "es", "de", "ko", "th")
        assertEquals(expectedCodes, AppLanguage.entries.map { it.storageValue })
        AppLanguage.entries.forEach { language ->
            assertEquals(language, AppLanguage.fromStorage(language.storageValue))
        }
    }

    @Test
    fun newLanguagesHaveLocalizedCoreAndTrainingText() {
        val languages = listOf(AppLanguage.Spanish, AppLanguage.German, AppLanguage.Korean)
        val coreKeys = listOf("language", "stop", "settings", "training_live", "leaderboard_title")
        languages.forEach { language ->
            coreKeys.forEach { key ->
                val translated = UiStrings.get(language, key)
                assertNotEquals(key, translated)
                assertNotEquals(UiStrings.get(AppLanguage.English, key), translated)
            }
            assertNotNull(AdditionalUiStrings.get(language, "Live Training"))
            assertNotNull(AdditionalUiStrings.get(language, "Bluetooth Connection"))
            assertNotNull(AdditionalUiStrings.get(language, "Training Report"))
        }
    }

    @Test
    fun newLanguagesCoverLegacyStatusAndReportText() {
        val languages = listOf(AppLanguage.Spanish, AppLanguage.German, AppLanguage.Korean)
        val legacyTexts =
            listOf(
                "Waiting for Relative Power Score",
                "No writable Bluetooth channel found",
                "TempoStrike Training Report",
                "Today: 500 hits | Done 0 hits",
                "Background music follows training BPM; Champion Rush may play near 122 BPM.",
            )
        languages.forEach { language ->
            legacyTexts.forEach { english ->
                val translated = AdditionalUiStrings.get(language, english)
                assertNotNull("Missing $language translation for: $english", translated)
                assertNotEquals("Unexpected English fallback for $language: $english", english, translated)
            }
        }
    }

}
