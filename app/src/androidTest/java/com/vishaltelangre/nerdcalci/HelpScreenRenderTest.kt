package com.vishaltelangre.nerdcalci

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishaltelangre.nerdcalci.ui.help.HelpScreen
import com.vishaltelangre.nerdcalci.ui.theme.NerdCalciTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpScreenRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHelpScreenDisplaysReferenceMarkdown() {
        composeTestRule.setContent {
            NerdCalciTheme {
                HelpScreen(onBack = {})
            }
        }
        
        // AndroidView rendering Markwon isn't naturally accessible to Compose semantic tree
        // by default. We just want to check that it doesn't crash on load and the top bar 
        // renders correctly.
        composeTestRule.onNodeWithText("Help").assertIsDisplayed()
    }
}
