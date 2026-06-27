package com.endocrine.checkin.presentation.checkin

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.endocrine.checkin.R
import com.endocrine.checkin.domain.usecase.GetCheckinUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import com.endocrine.checkin.presentation.theme.CheckInTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Robot for the two-step check-in flow. Hosts a real [CheckinViewModel] wired to [repo] (an
 * in-memory fake), mirrors [CheckinRoot]'s save-confirmation snackbar, and exposes the
 * interactions the happy-path test needs. The emotion wheel is a Canvas, so leaves are tapped
 * via coordinates computed from its documented geometry.
 */
class CheckinFlowRobot(
    private val rule: ComposeContentTestRule,
    val repo: FakeCheckinRepository,
) {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    fun start() = apply {
        val viewModel = CheckinViewModel(
            saveCheckin = SaveCheckinUseCase(repo),
            getCheckin = GetCheckinUseCase(repo),
            savedStateHandle = SavedStateHandle(mapOf("entryId" to null, "fromNotification" to false)),
        )
        rule.setContent {
            CheckInTheme {
                val state by viewModel.state.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val savedMessage = str(R.string.checkin_saved)
                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        if (event is CheckinEvent.Saved) {
                            scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                        }
                    }
                }
                CheckinScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onAction = viewModel::onAction,
                )
            }
        }
    }

    fun assertNextDisabled() = apply {
        rule.onNodeWithText(str(R.string.checkin_next)).assertIsNotEnabled()
    }

    fun assertNextEnabled() = apply {
        rule.onNodeWithText(str(R.string.checkin_next)).assertIsEnabled()
    }

    fun setScale(scaleNameRes: Int, value: Int) = apply {
        val cd = str(R.string.cd_scale_value, str(scaleNameRes), value)
        rule.onNodeWithContentDescription(cd).performClick()
    }

    fun completeBody() = apply {
        setScale(R.string.scale_energy, 3)
        setScale(R.string.scale_fatigue, 2)
        setScale(R.string.scale_hunger, 4)
        setScale(R.string.scale_tension, 1)
    }

    fun tapNext() = apply {
        rule.onNodeWithText(str(R.string.checkin_next)).performClick()
        rule.waitForIdle()
    }

    fun assertSaveDisabled() = apply {
        rule.onNodeWithText(str(R.string.checkin_save)).assertIsNotEnabled()
    }

    fun assertSaveEnabled() = apply {
        rule.onNodeWithText(str(R.string.checkin_save)).assertIsEnabled()
    }

    /** Tap the wheel at the polar point ([degTop] from 12 o'clock, clockwise; [radiusFraction] of r). */
    private fun tapWheel(degTop: Float, radiusFraction: Float) = apply {
        rule.onNodeWithContentDescription(str(R.string.cd_emotion_wheel)).performTouchInput {
            val cx = width / 2f
            val cy = height / 2f
            val r = max(cx, cy)
            // Inverse of the wheel's tap math: deg = atan2(dy,dx) + 90  ⇒  atan2 angle = deg - 90.
            val rad = Math.toRadians((degTop - 90f).toDouble())
            val dist = radiusFraction * r
            click(Offset(cx + dist * cos(rad).toFloat(), cy + dist * sin(rad).toFloat()))
        }
        rule.waitForIdle()
    }

    /** Stage 1: select a category sector by its center angle (index * 60 + 30). */
    fun selectCategory(index: Int) = tapWheel(index * 60f + 30f, 0.615f)

    /** Stage 2: select a leaf at l2 slot [l2Index] / leaf slot [leafIndex] (its sector center). */
    fun selectLeaf(l2Index: Int, leafIndex: Int) =
        tapWheel(l2Index * 60f + leafIndex * 30f + 15f, 0.77f)

    fun tapSave() = apply {
        rule.onNodeWithText(str(R.string.checkin_save)).performClick()
        rule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertSavedConfirmationShown() = apply {
        val msg = str(R.string.checkin_saved)
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText(msg).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText(msg).assertIsDisplayed()
    }
}
