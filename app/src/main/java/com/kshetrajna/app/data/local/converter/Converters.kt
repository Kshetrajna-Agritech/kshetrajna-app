package com.kshetrajna.app.data.local.converter

import androidx.room.TypeConverter
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SensorSource
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus

/**
 * Room type converters for domain enums and safety fault list serialization.
 */
class Converters {

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? = value?.let { SyncStatus.valueOf(it) }

    @TypeConverter
    fun fromMeasurementCategory(value: MeasurementCategory?): String? = value?.name

    @TypeConverter
    fun toMeasurementCategory(value: String?): MeasurementCategory? = value?.let { MeasurementCategory.valueOf(it) }

    @TypeConverter
    fun fromSystemSafetyStatus(value: SystemSafetyStatus?): String? = value?.name

    @TypeConverter
    fun toSystemSafetyStatus(value: String?): SystemSafetyStatus? = value?.let { SystemSafetyStatus.valueOf(it) }

    @TypeConverter
    fun fromCommandLifecycleStatus(value: CommandLifecycleStatus?): String? = value?.name

    @TypeConverter
    fun toCommandLifecycleStatus(value: String?): CommandLifecycleStatus? = value?.let { CommandLifecycleStatus.valueOf(it) }

    @TypeConverter
    fun fromActuatorStatus(value: ActuatorStatus?): String? = value?.name

    @TypeConverter
    fun toActuatorStatus(value: String?): ActuatorStatus? = value?.let { ActuatorStatus.valueOf(it) }

    @TypeConverter
    fun fromIrrigationCommandType(value: IrrigationCommandType?): String? = value?.name

    @TypeConverter
    fun toIrrigationCommandType(value: String?): IrrigationCommandType? = value?.let { IrrigationCommandType.valueOf(it) }

    @TypeConverter
    fun fromAlertSeverity(value: AlertSeverity?): String? = value?.name

    @TypeConverter
    fun toAlertSeverity(value: String?): AlertSeverity? = value?.let { AlertSeverity.valueOf(it) }

    @TypeConverter
    fun fromSensorSource(value: SensorSource?): String? = value?.name

    @TypeConverter
    fun toSensorSource(value: String?): SensorSource? = value?.let { SensorSource.valueOf(it) }

    @TypeConverter
    fun fromSafetyFaultList(faults: List<SafetyFault>?): String {
        if (faults.isNullOrEmpty()) return ""
        return faults.joinToString(";") { fault ->
            "${fault.type.name}|${fault.message.replace("|", "_").replace(";", "_")}|${fault.triggeredAtEpochMillis}"
        }
    }

    @TypeConverter
    fun toSafetyFaultList(data: String?): List<SafetyFault> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 3) {
                try {
                    SafetyFault(
                        type = SafetyFaultType.valueOf(parts[0]),
                        message = parts[1],
                        triggeredAtEpochMillis = parts[2].toLong(),
                    )
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }
}
