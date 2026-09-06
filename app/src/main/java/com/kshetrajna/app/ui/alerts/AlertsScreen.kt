package com.kshetrajna.app.ui.alerts

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
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
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
                                text = "Unable to load safety status and alerts",
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
                AlertsContent(
                    stateData = state.data,
                    onAcknowledgeAlert = viewModel::acknowledgeAlert,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No safety or alert data available")
                }
            }
        }
    }
}

@Composable
fun AlertsContent(
    stateData: AlertsUiStateData,
    onAcknowledgeAlert: (Alert) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val data = stateData.data

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Current Safety Status Banner Card
        SafetyStatusBannerCard(
            nodeId = data.nodeId,
            safetyState = data.safetyState
        )

        // 2. Action Messages Card (if user acknowledged an alert)
        if (stateData.actionMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFD8F3DC), shape = RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = stateData.actionMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF081C15),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 3. Active System Alerts Card
        ActiveAlertsCard(
            activeAlerts = data.activeAlerts,
            onAcknowledgeAlert = onAcknowledgeAlert
        )

        // 4. Chronological Alert History Card
        AlertHistoryCard(alerts = data.alertHistory)

        // 5. Safety & Irrigation Boundary Scope Card
        SafetyBoundaryScopeCard()
    }
}

@Composable
fun SafetyStatusBannerCard(
    nodeId: String,
    safetyState: SafetyState?,
) {
    val status = safetyState?.status
    val isLockedOut = safetyState?.isLockedOut == true
    val isNoData = safetyState == null

    val containerColor = when {
        isNoData -> MaterialTheme.colorScheme.surfaceVariant
        isLockedOut -> MaterialTheme.colorScheme.errorContainer
        status == SystemSafetyStatus.WARNING -> Color(0xFFFFF3CD)
        else -> MaterialTheme.colorScheme.surface
    }

    val statusText = when {
        isNoData -> "NO SAFETY DATA"
        else -> status?.name ?: "UNKNOWN"
    }

    val statusColor = when {
        isNoData -> MaterialTheme.colorScheme.onSurfaceVariant
        isLockedOut -> MaterialTheme.colorScheme.error
        status == SystemSafetyStatus.WARNING -> Color(0xFF856404)
        else -> MaterialTheme.colorScheme.primary
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
                    text = "System Safety Status ($nodeId)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BadgeText(text = statusText, color = statusColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeText(text = "FIRMWARE SAFETY AUTHORITATIVE", color = MaterialTheme.colorScheme.tertiary)
                BadgeText(text = "NO SAFETY BYPASS", color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isNoData) {
                Text(
                    text = "No safety telemetry available from field node. State is unverified; NO SAFETY DATA != SAFETY NORMAL.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (isLockedOut) {
                Text(
                    text = "HARDWARE SAFETY LOCKOUT: Firmware safety interlocks active. Software irrigation commands are blocked.",
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
                    text = "System operating normally within safety boundaries. Firmware hardware interlocks retain authoritative control.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActiveAlertsCard(
    activeAlerts: List<Alert>,
    onAcknowledgeAlert: (Alert) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active System Notifications (${activeAlerts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ACKNOWLEDGEMENT != FAULT RESOLUTION. Acknowledging an alert does not clear a hardware safety lockout.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (activeAlerts.isEmpty()) {
                Text(
                    text = "No active unacknowledged system alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activeAlerts.forEach { alert ->
                    AlertItemRow(
                        alert = alert,
                        onAcknowledge = { onAcknowledgeAlert(alert) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AlertItemRow(
    alert: Alert,
    onAcknowledge: (() -> Unit)?,
) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        AlertSeverity.WARNING -> Color(0xFFFFC107)
        AlertSeverity.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeText(text = alert.severity.name, color = severityColor)
                Text(
                    text = formatTimestamp(alert.timestampEpochMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!alert.affectedZone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zone: ${alert.affectedZone}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (onAcknowledge != null && !alert.isAcknowledged) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAcknowledge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = severityColor
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Acknowledge Alert",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AlertHistoryCard(alerts: List<Alert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chronological Alert History Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (alerts.isEmpty()) {
                Text(
                    text = "No historical alerts recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                alerts.take(10).forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${alert.message} (${formatTimestamp(alert.timestampEpochMillis)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeText(
                            text = if (alert.isAcknowledged) "ACKNOWLEDGED" else "ACTIVE",
                            color = if (alert.isAcknowledged) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyBoundaryScopeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Safety & Irrigation Architecture Boundaries",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            BoundaryScopeRow(tag = "HARDWARE SAFETY", description = "Firmware retains ultimate authority over electrical/thermal safety.")
            Spacer(modifier = Modifier.height(4.dp))
            BoundaryScopeRow(tag = "COMMAND BLOCKING", description = "LOCKED or FAULT safety states automatically block software irrigation dispatch.")
            Spacer(modifier = Modifier.height(4.dp))
            BoundaryScopeRow(tag = "NO BYPASS", description = "No Android app controls exist to force irrigation or ignore safety faults.")
        }
    }
}

@Composable
fun BoundaryScopeRow(tag: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BadgeText(text = tag, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
