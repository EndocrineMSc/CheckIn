package com.endocrine.checkin.presentation.checkin

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.endocrine.checkin.domain.usecase.FakeCheckinRepository
import com.endocrine.checkin.domain.usecase.GetCheckinUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric (JUnit4 + vintage engine) because the ViewModel decodes its type-safe
 * route via [androidx.lifecycle.SavedStateHandle.toRoute], which needs a real `android.os.Bundle`.
 * The rest of the unit suite stays on JUnit5.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
// Plain Application so the real CheckInApplication does not start Koin (the VM is built by hand).
@Config(sdk = [34], application = android.app.Application::class)
class CheckinViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeCheckinRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeCheckinRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** A new check-in: route args are the type-safe defaults (entryId = null). */
    private fun viewModel(): CheckinViewModel = CheckinViewModel(
        saveCheckin = SaveCheckinUseCase(repo),
        getCheckin = GetCheckinUseCase(repo),
        savedStateHandle = SavedStateHandle(mapOf("entryId" to null, "fromNotification" to false)),
    )

    private fun CheckinViewModel.completeBody() {
        onAction(CheckinAction.SelectScale(BodyField.Energy, 3))
        onAction(CheckinAction.SelectScale(BodyField.Fatigue, 2))
        onAction(CheckinAction.SelectScale(BodyField.Hunger, 4))
        onAction(CheckinAction.SelectScale(BodyField.Tension, 1))
    }

    @Test
    fun `Weiter is gated until all four scales are set`() = runTest {
        val vm = viewModel()
        assertThat(vm.state.value.isBodyComplete).isFalse()

        vm.onAction(CheckinAction.SelectScale(BodyField.Energy, 3))
        vm.onAction(CheckinAction.SelectScale(BodyField.Fatigue, 2))
        vm.onAction(CheckinAction.SelectScale(BodyField.Hunger, 4))
        assertThat(vm.state.value.isBodyComplete).isFalse()

        vm.onAction(CheckinAction.SelectScale(BodyField.Tension, 1))
        assertThat(vm.state.value.isBodyComplete).isTrue()
    }

    @Test
    fun `Next does nothing while body is incomplete and advances once complete`() = runTest {
        val vm = viewModel()
        vm.onAction(CheckinAction.Next)
        assertThat(vm.state.value.step).isEqualTo(CheckinStep.Body)

        vm.completeBody()
        vm.onAction(CheckinAction.Next)
        assertThat(vm.state.value.step).isEqualTo(CheckinStep.Emotion)
    }

    @Test
    fun `Speichern is gated until a leaf is chosen`() = runTest {
        val vm = viewModel()
        vm.completeBody()
        vm.onAction(CheckinAction.Next)
        assertThat(vm.state.value.canSave).isFalse()

        // Selecting only the category is not enough.
        vm.onAction(CheckinAction.SelectCategory("Wut"))
        assertThat(vm.state.value.canSave).isFalse()

        vm.onAction(CheckinAction.SelectLeaf("verletzt"))
        assertThat(vm.state.value.canSave).isTrue()
    }

    @Test
    fun `selecting a leaf fills in the structural parents`() = runTest {
        val vm = viewModel()
        vm.onAction(CheckinAction.SelectLeaf("verletzt"))
        val emotion = vm.state.value.selectedEmotion
        assertThat(emotion).isNotNull()
        assertThat(emotion!!.category).isEqualTo("Wut")
        assertThat(emotion.l2).isEqualTo("hasserfüllt")
        assertThat(emotion.l3).isEqualTo("verletzt")
    }

    @Test
    fun `requesting exit with no input exits silently`() = runTest {
        val vm = viewModel()
        vm.events.test {
            vm.onAction(CheckinAction.RequestExit)
            assertThat(awaitItem()).isEqualTo(CheckinEvent.Exit)
        }
        assertThat(vm.state.value.showDiscardDialog).isFalse()
    }

    @Test
    fun `requesting exit with input shows the discard dialog`() = runTest {
        val vm = viewModel()
        vm.onAction(CheckinAction.SelectScale(BodyField.Energy, 3))
        vm.onAction(CheckinAction.RequestExit)
        assertThat(vm.state.value.showDiscardDialog).isTrue()
    }

    @Test
    fun `confirming discard emits Exit`() = runTest {
        val vm = viewModel()
        vm.onAction(CheckinAction.SelectScale(BodyField.Energy, 3))
        vm.onAction(CheckinAction.RequestExit)
        vm.events.test {
            vm.onAction(CheckinAction.ConfirmDiscard)
            assertThat(awaitItem()).isEqualTo(CheckinEvent.Exit)
        }
    }

    @Test
    fun `saving a complete check-in emits Saved and persists a row`() = runTest {
        val vm = viewModel()
        vm.completeBody()
        vm.onAction(CheckinAction.Next)
        vm.onAction(CheckinAction.SelectLeaf("erfreut"))

        vm.events.test {
            vm.onAction(CheckinAction.Save)
            assertThat(awaitItem()).isEqualTo(CheckinEvent.Saved)
        }

        val stored = repo.getAllOnce()
        assertThat(stored.size).isEqualTo(1)
        val entry = stored.first()
        assertThat(entry.body.energy).isEqualTo(3)
        assertThat(entry.emotion.l3).isEqualTo("erfreut")
    }

    @Test
    fun `a saved new entry receives a fresh timestamp`() = runTest {
        val vm = viewModel()
        vm.completeBody()
        vm.onAction(CheckinAction.Next)
        vm.onAction(CheckinAction.SelectLeaf("erfreut"))
        vm.onAction(CheckinAction.Save)

        val entry = repo.observeAll().first().first()
        assertThat(entry.timestamp > 0L).isTrue()
    }
}
