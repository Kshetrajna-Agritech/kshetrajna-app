package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SoilAnalysis
import com.kshetrajna.app.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [TelemetryRepository] backing telemetry operations via [LocalDataSource].
 */
class DefaultTelemetryRepository(
    private val localDataSource: LocalDataSource,
) : TelemetryRepository {

    override fun observeNodes(farmId: String?): Flow<Resource<List<Node>>> {
        val flow = if (farmId.isNullOrBlank()) {
            localDataSource.getNodes()
        } else {
            localDataSource.getNodesForFarm(farmId)
        }
        return flow.map { nodes ->
            Resource.Success(nodes) as Resource<List<Node>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe nodes from local database: ${e.message}", e))
        }
    }

    override fun observeReadingsForNode(nodeId: String): Flow<Resource<List<SensorReading>>> {
        return localDataSource.observeReadingsForNode(nodeId).map { readings ->
            Resource.Success(readings) as Resource<List<SensorReading>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe sensor readings: ${e.message}", e))
        }
    }

    override suspend fun saveSensorReading(reading: SensorReading): Resource<Unit> {
        return try {
            localDataSource.insertSensorReading(reading)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save sensor reading locally: ${e.message}", e)
        }
    }

    override fun observeSoilAnalyses(nodeId: String): Flow<Resource<List<SoilAnalysis>>> {
        return localDataSource.observeSoilAnalysesForNode(nodeId).map { analyses ->
            Resource.Success(analyses) as Resource<List<SoilAnalysis>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe soil analyses: ${e.message}", e))
        }
    }

    override suspend fun saveSoilAnalysis(analysis: SoilAnalysis): Resource<Unit> {
        return try {
            localDataSource.insertSoilAnalysis(analysis)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save soil analysis locally: ${e.message}", e)
        }
    }
}
