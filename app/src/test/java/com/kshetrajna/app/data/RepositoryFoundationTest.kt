package com.kshetrajna.app.data

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.Node
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryFoundationTest {

    @Test
    fun `DefaultTelemetryRepository returns data from local data source as Success resource`() = runTest {
        val fakeLocal = InMemoryLocalDataSource()
        fakeLocal.nodes.value = listOf(Node(id = "node-1", farmId = "farm-1", name = "North Field Node", isOnline = true))
        val repository = DefaultTelemetryRepository(fakeLocal)

        val result = repository.observeNodes().first()

        assertTrue(result is Resource.Success<*>)
        val nodes = (result as Resource.Success<List<Node>>).data
        assertEquals(1, nodes.size)
        assertEquals("node-1", nodes.first().id)
        assertEquals("North Field Node", nodes.first().name)
    }
}
