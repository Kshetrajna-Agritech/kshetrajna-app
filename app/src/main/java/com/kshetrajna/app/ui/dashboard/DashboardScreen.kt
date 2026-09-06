package com.kshetrajna.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.DashboardData
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Unable to load dashboard data",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            is UiState.Success -> {
                DashboardContent(
                    uiState = state.data,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No dashboard data available")
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
) {
    val data = uiState.data ?: DashboardData()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Safety Status Banner (Visually Prominent)
        SafetyStatusBanner(
            safetyState = data.safetyState,
            isOfflineNode = uiState.isOfflineNode
        )

        // 2. Sync Status Banner
        SyncStatusCard(syncStatus = data.syncStatus)

        // 3. Soil Telemetry Card
        SoilTelemetryCard(
            nodeName = data.node?.name ?: "Field Node",
            isOnline = data.node?.isOnline ?: false,
            reading = data.latestReading
        )

        // 4. Weather Context Card
        WeatherCard(weather = data.latestWeather)

        // 5. Irrigation Status Card
        IrrigationStatusCard(
            irrigationState = data.irrigationState,
            latestCommand = data.latestCommand
        )

        // 6. Alerts Summary Section
        if (data.alerts.isNotEmpty()) {
            AlertsSummaryCard(alerts = data.alerts)
        }
    }
}

@Composable
fun SafetyStatusBanner(
    safetyState: SafetyState?,
    isOfflineNode: Boolean,
) {
    val status = when {
        isOfflineNode -> SystemSafetyStatus.OFFLINE
        safetyState != null -> safetyState.status
        else -> SystemSafetyStatus.NORMAL
    }

    val (bgColor, textColor, labelText) = when (status) {
        SystemSafetyStatus.NORMAL -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "SAFETY STATUS: NORMAL"
        )
        SystemSafetyStatus.WARNING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "SAFETY STATUS: WARNING"
        )
        SystemSafetyStatus.LOCKED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "SAFETY STATUS: LOCKED OUT"
        )
        SystemSafetyStatus.FAULT -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "SAFETY STATUS: FAULT DETECTED"
        )
        SystemSafetyStatus.OFFLINE -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "SAFETY STATUS: NODE OFFLINE"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val faults = safetyState?.activeFaults ?: emptyList()
            if (faults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                faults.forEach { fault ->
                    Text(
                        text = "• ${fault.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusCard(syncStatus: SyncStatus) {
    val (statusText, badgeColor) = when (syncStatus) {
        SyncStatus.SYNCED -> Pair("Synced with Server", Color(0xFF28A745))
        SyncStatus.PENDING -> Pair("Pending Offline Sync", Color(0xFFFFC107))
        SyncStatus.UPLOADING -> Pair("Uploading Data...", Color(0xFF17A2B8))
        SyncStatus.FAILED -> Pair("Sync Failed (Will Retry)", Color(0xFFDC3545))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Data Synchronization",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            BadgeText(text = statusText, color = badgeColor)
        }
    }
}

@Composable
fun SoilTelemetryCard(
    nodeName: String,
    isOnline: Boolean,
    reading: SensorReading?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soil Telemetry ($nodeName)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                BadgeText(
                    text = if (isOnline) "ONLINE" else "OFFLINE",
                    color = if (isOnline) Color(0xFF28A745) else Color(0xFF6C757D)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BadgeText(
                text = "MEASURED TELEMETRY",
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryMetricItem(
                    label = "Soil Moisture",
                    value = reading?.soilMoisturePercent?.let { "%.1f %%".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "Soil Temp",
                    value = reading?.soilTemperatureCelsius?.let { "%.1f °C".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "Soil EC",
                    value = reading?.soilEcDsPerM?.let { "%.2f dS/m".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weather Context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            BadgeText(
                text = "EXTERNAL FORECAST (CACHED)",
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryMetricItem(
                    label = "Rainfall",
                    value = weather?.rainfallMm?.let { "%.1f mm".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "Air Temp",
                    value = weather?.temperatureCelsius?.let { "%.1f °C".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "Humidity",
                    value = weather?.humidityPercent?.let { "%.0f %%".format(it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
            if (weather?.conditionText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Condition: ${weather.conditionText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IrrigationStatusCard(
    irrigationState: IrrigationState?,
    latestCommand: IrrigationCommand?,
) {
    val actuatorStatus = irrigationState?.status ?: ActuatorStatus.STOPPED
    val isRunning = actuatorStatus == ActuatorStatus.RUNNING

    val (actuatorText, actuatorColor) = when (actuatorStatus) {
        ActuatorStatus.RUNNING -> Pair("PUMP RUNNING", Color(0xFF28A745))
        ActuatorStatus.STOPPED -> Pair("PUMP STOPPED", Color(0xFF6C757D))
        ActuatorStatus.FAULTED -> Pair("ACTUATOR FAULT", Color(0xFFDC3545))
        ActuatorStatus.LOCKED_OUT -> Pair("ACTUATOR LOCKED OUT", Color(0xFFDC3545))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Irrigation Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                BadgeText(text = actuatorText, color = actuatorColor)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isRunning && (irrigationState?.activeFlowRateLpm != null)) {
                Text(
                    text = "Active Flow Rate: %.1f L/min".format(irrigationState.activeFlowRateLpm),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF28A745)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (latestCommand != null) {
                val cmdText = when (latestCommand.lifecycleStatus) {
                    CommandLifecycleStatus.COMMAND_REQUESTED -> "Command Requested (Awaiting Sent)"
                    CommandLifecycleStatus.COMMAND_SENT -> "Command Sent to Field Node"
                    CommandLifecycleStatus.COMMAND_ACCEPTED -> "Command Accepted by Field Node"
                    CommandLifecycleStatus.ACTUATOR_RUNNING -> "Actuator Running Confirmed"
                    CommandLifecycleStatus.ACTUATOR_STOPPED -> "Actuator Stopped Confirmed"
                    CommandLifecycleStatus.COMMAND_REJECTED -> "Command Rejected: ${latestCommand.rejectionReason ?: "Safety Interlock"}"
                }
                Text(
                    text = "Latest Command: $cmdText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AlertsSummaryCard(alerts: List<Alert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active System Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            alerts.take(3).forEach { alert ->
                val badgeColor = when (alert.severity) {
                    AlertSeverity.CRITICAL -> Color(0xFFDC3545)
                    AlertSeverity.WARNING -> Color(0xFFFFC107)
                    AlertSeverity.INFO -> Color(0xFF17A2B8)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeText(text = alert.severity.name, color = badgeColor)
                }
            }
        }
    }
}

@Composable
fun TelemetryMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BadgeText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
