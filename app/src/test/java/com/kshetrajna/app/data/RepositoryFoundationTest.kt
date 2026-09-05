package com.kshetrajna.app.data

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.domain.model.Node
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeLocalDataSource : LocalDataSource {
    override fun getNodes(): Flow<List<Node>> {
        return flowOf(listOf(Node(id = "node-1", name = "North Field Node", isOnline = true)))
    }
}

class RepositoryFoundationTest {

    @Test
    fun `DefaultTelemetryRepository returns data from local data source as Success resource`() = runTest {
        val fakeLocal = FakeLocalDataSource()
        val repository = DefaultTelemetryRepository(fakeLocal)

        val result = repository.observeNodes().first()

        assertTrue(result is Resource.Success)
        val nodes = (result as Resource.Success).data
        assertEquals(1, nodes.size)
        assertEquals("node-1", nodes.first().id)
        assertEquals("North Field Node", nodes.first().name)
    }
}
