package com.kshetrajna.app.ui

import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.ui.base.FoundationViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    testDispatcher: TestDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val unconfined: CoroutineDispatcher = testDispatcher
}

@OptIn(ExperimentalCoroutinesApi::class)
class FoundationViewModelTest {

    @Test
    fun `initial state is Success with foundation ready message`() = runTest {
        val testDispatcherProvider = TestDispatcherProvider()
        val viewModel = FoundationViewModel(dispatchers = testDispatcherProvider)

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals("Kshetrajna Application Foundation Ready", (state as UiState.Success).data)
    }
}
