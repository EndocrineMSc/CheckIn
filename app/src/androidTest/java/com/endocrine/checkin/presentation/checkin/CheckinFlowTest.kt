package com.endocrine.checkin.presentation.checkin

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.endocrine.checkin.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented happy-path test for the check-in flow: set 4 body scales → Weiter → pick a
 * category → pick a leaf → Speichern, then assert a row was saved (fake repo) and the
 * confirmation snackbar is shown.
 */
@RunWith(AndroidJUnit4::class)
class CheckinFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repo = FakeCheckinRepository()
    private val robot by lazy { CheckinFlowRobot(composeTestRule, repo) }

    @Test
    fun happyPath_savesEntry_andShowsConfirmation() {
        robot
            .start()
            .assertNextDisabled()
            .completeBody()
            .assertNextEnabled()
            .tapNext()
            .assertSaveDisabled()
            // Wut is category index 3; "verletzt" is its hasserfüllt (slot 0) second leaf (slot 1).
            .selectCategory(index = 3)
            .selectLeaf(l2Index = 0, leafIndex = 1)
            .assertSaveEnabled()
            .tapSave()
            .assertSavedConfirmationShown()

        assertThat(repo.saved).hasSize(1)
        val entry = repo.saved.first()
        assertThat(entry.body.energy).isEqualTo(3)
        assertThat(entry.body.fatigue).isEqualTo(2)
        assertThat(entry.body.hunger).isEqualTo(4)
        assertThat(entry.body.tension).isEqualTo(1)
        assertThat(entry.emotion.category).isEqualTo("Wut")
        assertThat(entry.emotion.l2).isEqualTo("hasserfüllt")
        assertThat(entry.emotion.l3).isEqualTo("verletzt")
    }
}
