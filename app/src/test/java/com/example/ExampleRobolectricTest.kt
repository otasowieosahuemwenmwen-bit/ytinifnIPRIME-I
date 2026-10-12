package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppLanguage
import com.example.data.model.DefaultCategories
import com.example.data.model.UserProfile
import com.example.ui.screens.AvailableChatTemplates
import com.example.ui.screens.VideoPresets
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
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Prime Infinity", appName)
  }

  @Test
  fun verifyDefaultCategoriesLoaded() {
    val categories = DefaultCategories.ALL
    assertEquals(5, categories.size)
    assertTrue(categories.any { it.id == "schools" })
    assertTrue(categories.any { it.id == "sports" })
    assertTrue(categories.any { it.id == "businesses" })
    assertTrue(categories.any { it.id == "anime" })
    assertTrue(categories.any { it.id == "beauty_makeup" })
  }

  @Test
  fun verifyUserProfileCreation() {
    val profile = UserProfile(
      name = "Test User",
      avatarEmoji = "⚡",
      bio = "Test Bio",
      activeCategoryId = "sports"
    )
    assertEquals("Test User", profile.name)
    assertEquals("sports", profile.activeCategoryId)
  }

  @Test
  fun verifyAppLanguages() {
    val languages = AppLanguage.entries
    assertTrue(languages.size >= 10)
    assertEquals("en", AppLanguage.fromCode("en").code)
    assertEquals("es", AppLanguage.fromCode("es").code)
    assertEquals("ja", AppLanguage.fromCode("ja").code)
  }

  @Test
  fun verifyVideoPresetsAndChatTemplates() {
    assertTrue(VideoPresets.isNotEmpty())
    assertTrue(VideoPresets.any { it.id == "cinematic" })
    assertTrue(VideoPresets.any { it.id == "anime" })

    assertTrue(AvailableChatTemplates.isNotEmpty())
    assertTrue(AvailableChatTemplates.any { it.title.contains("Image Creation") })
    assertTrue(AvailableChatTemplates.any { it.title.contains("Video Creation") })
  }
}
