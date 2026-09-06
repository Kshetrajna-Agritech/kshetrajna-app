package com.kshetrajna.app.ui.irrigation

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IrrigationScreen(
    viewModel: IrrigationViewModel,
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
                                text = "Unable to load irrigation state",
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
                IrrigationContent(
                    stateData = state.data,
                    onRequestCommand = viewModel::requestCommand,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No irrigation data available")
                }
            }
        }
    }
}

@Composable
fun IrrigationContent(
    stateData: IrrigationUiStateData,
    onRequestCommand: (IrrigationCommandType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val data = stateData.irrigationData

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hardware Safety Authority Header Card
        SafetyInterlockCard(
            nodeId = data.nodeId,
            safetyState = data.safetyState
        )

        // 2. Command Request Actions Card
        IrrigationControlCard(
            safetyState = data.safetyState,
            isSending = stateData.isSendingCommand,
            actionMessage = stateData.commandActionMessage,
            errorMessage = stateData.commandErrorMessage,
            onRequestCommand = onRequestCommand
        )

        // 3. Command Lifecycle vs Actuator State Card
        CommandAndActuatorStatusCard(
            latestCommand = data.latestCommand,
            irrigationState = data.latestIrrigationState
        )

        // 4. Command History Log Card
        CommandHistoryLogCard(commands = data.commandHistory)

        // 5. Environmental Context Card (Informational Context)
        EnvironmentalContextCard(
            reading = data.latestReading,
            weather = data.latestWeather
        )
    }
}

@Composable
fun SafetyInterlockCard(
    nodeId: String,
    safetyState: SafetyState?,
) {
    val isLockedOut = safetyState?.isLockedOut == true
    val containerColor = if (isLockedOut) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hardware Safety Status ($nodeId)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BadgeText(
                    text = safetyState?.status?.name ?: "UNKNOWN",
                    color = if (isLockedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeText(text = "FIRMWARE SAFETY AUTHORITATIVE", color = MaterialTheme.colorScheme.tertiary)
                BadgeText(text = "NO SAFETY BYPASS", color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isLockedOut) {
                Text(
                    text = "SAFETY LOCKOUT ACTIVE: Firmware interlocks prevent physical actuation. Software command requests will be rejected.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                safetyState.activeFaults.forEach { fault ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Fault (${fault.type.name}): ${fault.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Text(
                    text = "Firmware safety interlocks normal. Hardware retains ultimate physical safety authority.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IrrigationControlCard(
    safetyState: SafetyState?,
    isSending: Boolean,
    actionMessage: String?,
    errorMessage: String?,
    onRequestCommand: (IrrigationCommandType) -> Unit,
) {
    val isLockedOut = safetyState?.isLockedOut == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Manual Irrigation Control Request",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Request software command dispatch. Commands are recorded locally first with asynchronous queue sync.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onRequestCommand(IrrigationCommandType.START_IRRIGATION) },
                    enabled = !isSending,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLockedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Start Irrigation",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onRequestCommand(IrrigationCommandType.STOP_IRRIGATION) },
                    enabled = !isSending,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Stop Irrigation",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (actionMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFD8F3DC), shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = actionMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF081C15),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CommandAndActuatorStatusCard(
    latestCommand: IrrigationCommand?,
    irrigationState: IrrigationState?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Command Lifecycle vs Actuator State",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "COMMAND ACCEPTED != ACTUATOR RUNNING. Physical pump operation requires hardware feedback telemetry.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Command Status Column
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Software Command Lifecycle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestCommand?.lifecycleStatus?.name ?: "NO COMMAND",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (latestCommand?.lifecycleStatus == CommandLifecycleStatus.COMMAND_REJECTED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = latestCommand?.requestedAtEpochMillis?.let { "Req: ${formatTimestamp(it)}" } ?: "--",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Physical Actuator Column
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "Physical Actuator Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = irrigationState?.status?.name ?: "NOT VERIFIED",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (irrigationState?.status) {
                            ActuatorStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            ActuatorStatus.LOCKED_OUT, ActuatorStatus.FAULTED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = irrigationState?.activeFlowRateLpm?.let { "Flow: %.1f L/min".format(it) } ?: "Flow: --",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (latestCommand?.lifecycleStatus == CommandLifecycleStatus.COMMAND_REJECTED && !latestCommand.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Rejection Reason: ${latestCommand.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CommandHistoryLogCard(commands: List<IrrigationCommand>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chronological Command Dispatch Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (commands.isEmpty()) {
                Text(
                    text = "No previous irrigation commands requested.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                commands.take(5).forEach { command ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = command.commandType.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Requested: ${formatTimestamp(command.requestedAtEpochMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!command.rejectionReason.isNullOrBlank()) {
                                Text(
                                    text = "Reason: ${command.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeText(
                            text = command.lifecycleStatus.name,
                            color = if (command.lifecycleStatus == CommandLifecycleStatus.COMMAND_REJECTED) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnvironmentalContextCard(
    reading: SensorReading?,
    weather: WeatherData?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Environmental Context (Informational)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Informational context only. No automated pump trigger or weather delay rules are executed in M11.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Soil Moisture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = reading?.soilMoisturePercent?.let { "%.1f %%".format(it) } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(text = "Rainfall Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = weather?.rainfallMm?.let { "%.1f mm".format(it) } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
