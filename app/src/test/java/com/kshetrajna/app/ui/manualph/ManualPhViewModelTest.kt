package com.kshetrajna.app.ui.manualph

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultManualPhRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.ManualPhRepository
import com.kshetrajna.app.domain.usecase.GetManualPhEntriesUseCase
import com.kshetrajna.app.domain.usecase.RecordManualPhUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManualPhViewModelTest {

    private fun createViewModel(
        manualPhRepository: ManualPhRepository,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): ManualPhViewModel {
        val getUseCase = GetManualPhEntriesUseCase(manualPhRepository)
        val recordUseCase = RecordManualPhUseCase(manualPhRepository)
        return ManualPhViewModel(
            getManualPhEntriesUseCase = getUseCase,
            recordManualPhUseCase = recordUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `valid pH input saves successfully and creates PENDING sync record`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val repository = DefaultManualPhRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onPhInputChanged("6.8")
        viewModel.onNotesInputChanged("North field sample")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<ManualPhUiStateData>).data
        assertNull(uiData.validationError)
        assertNotNull(uiData.saveSuccessMessage)

        // Verify entry in local repository
        val savedEntries = localDataSource.manualPhs.value
        assertEquals(1, savedEntries.size)

        val entry = savedEntries.first()
        assertEquals(6.8f, entry.phValue, 0.001f)
        assertEquals("North field sample", entry.notes)
        assertEquals(MeasurementCategory.MANUAL, entry.category)
        assertEquals(SyncStatus.PENDING, entry.syncStatus)
        assertTrue(entry.timestampEpochMillis > 0L)

        // Verify SyncRecord created in local DB
        val syncRecords = localDataSource.syncRecords.value
        assertEquals(1, syncRecords.size)
        assertEquals("ManualPH", syncRecords.first().entityType)
        assertEquals(SyncStatus.PENDING, syncRecords.first().syncStatus)
    }

    @Test
    fun `empty input is rejected with validation error`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val repository = DefaultManualPhRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onPhInputChanged("   ")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<ManualPhUiStateData>).data
        assertEquals("pH value cannot be empty.", uiData.validationError)
        assertEquals(0, localDataSource.manualPhs.value.size)
    }

    @Test
    fun `malformed non-numeric input is rejected`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val repository = DefaultManualPhRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onPhInputChanged("abc_ph")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<ManualPhUiStateData>).data
        assertEquals("Please enter a valid numeric pH value.", uiData.validationError)
        assertEquals(0, localDataSource.manualPhs.value.size)
    }

    @Test
    fun `out of range domain pH values are rejected by domain validation`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val repository = DefaultManualPhRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        // Test negative pH
        viewModel.onPhInputChanged("-1.5")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val stateNegative = (viewModel.uiState.value as UiState.Success<ManualPhUiStateData>).data
        assertEquals("pH value must be within valid physical range [0.0, 14.0].", stateNegative.validationError)

        // Test pH > 14.0
        viewModel.onPhInputChanged("14.5")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val stateTooHigh = (viewModel.uiState.value as UiState.Success<ManualPhUiStateData>).data
        assertEquals("pH value must be within valid physical range [0.0, 14.0].", stateTooHigh.validationError)

        assertEquals(0, localDataSource.manualPhs.value.size)
    }

    @Test
    fun `saved pH entries are sorted chronologically in reverse order`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val repository = DefaultManualPhRepository(localDataSource)

        val entry1 = ManualPH(
            id = "mph-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            phValue = 6.2f,
            category = MeasurementCategory.MANUAL
        )
        val entry2 = ManualPH(
            id = "mph-2",
            nodeId = "sim_node_01",
            timestampEpochMillis = 2000L,
            phValue = 6.8f,
            category = MeasurementCategory.MANUAL
        )
        localDataSource.manualPhs.value = listOf(entry1, entry2)

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<ManualPhUiStateData>).data
        assertEquals(2, uiData.entries.size)
        // Newest entry first
        assertEquals("mph-2", uiData.entries[0].id)
        assertEquals("mph-1", uiData.entries[1].id)
        assertEquals(6.8f, uiData.latestEntry!!.phValue, 0.001f)
    }

    @Test
    fun `persistence error produces explicit error state`() = runTest {
        val failingRepo = object : ManualPhRepository {
            override fun observeManualPhForNode(nodeId: String): Flow<Resource<List<ManualPH>>> {
                return flowOf(Resource.Success(emptyList()))
            }

            override suspend fun recordManualPh(entry: ManualPH): Resource<Unit> {
                return Resource.Error("Room database write access error")
            }

            override suspend fun updateManualPhSyncStatus(id: String, status: SyncStatus): Resource<Unit> {
                return Resource.Error("Error")
            }
        }

        val viewModel = createViewModel(failingRepo)
        advanceUntilIdle()

        viewModel.onPhInputChanged("6.5")
        viewModel.saveManualPh("sim_node_01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<ManualPhUiStateData>).data
        assertEquals("Room database write access error", uiData.validationError)
    }
}
