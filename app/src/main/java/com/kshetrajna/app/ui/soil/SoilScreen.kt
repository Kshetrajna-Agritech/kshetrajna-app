package com.kshetrajna.app.ui.soil

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SensorSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SoilScreen(
    viewModel: SoilViewModel,
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
                                text = "Unable to load soil telemetry data",
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
            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Soil Telemetry Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No sensor readings recorded for this field node yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is UiState.Success -> {
                SoilTelemetryContent(
                    stateData = state.data,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No soil telemetry available")
                }
            }
        }
    }
}

@Composable
fun SoilTelemetryContent(
    stateData: SoilUiStateData,
    modifier: Modifier = Modifier,
) {
    val telemetryData = stateData.telemetryData
    val node = telemetryData.node
    val latestReading = telemetryData.latestReading
    val historyReadings = telemetryData.historyReadings
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Node Header & Source Provenance Card
        NodeHeaderCard(
            nodeName = node?.name ?: "Field Node",
            isOnline = node?.isOnline ?: false,
            source = latestReading?.source ?: SensorSource.LORA_FIELD_NODE
        )

        // 2. Latest Telemetry Readings Card
        LatestReadingCard(reading = latestReading)

        // 3. Chronological Trend Chart
        ChronologicalTrendCard(readings = historyReadings)

        // 4. Chronological Telemetry History Log
        HistoryLogCard(readings = historyReadings)
    }
}

@Composable
fun NodeHeaderCard(
    nodeName: String,
    isOnline: Boolean,
    source: SensorSource,
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
                    text = "Node: $nodeName",
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeText(
                    text = "MEASURED TELEMETRY",
                    color = MaterialTheme.colorScheme.secondary
                )

                val (sourceLabel, sourceColor) = when (source) {
                    SensorSource.LORA_FIELD_NODE -> Pair("LORA FIELD NODE", Color(0xFF17A2B8))
                    SensorSource.BLE_DIRECT -> Pair("BLE DIRECT", Color(0xFF28A745))
                    SensorSource.LOCAL_SIMULATION -> Pair("SIMULATED DATA (DEMO)", Color(0xFFD97706))
                }
                BadgeText(text = sourceLabel, color = sourceColor)
            }
        }
    }
}

@Composable
fun LatestReadingCard(reading: SensorReading?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Latest Measured Readings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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

            Spacer(modifier = Modifier.height(12.dp))
            val timeText = reading?.timestampEpochMillis?.let { formatTimestamp(it) } ?: "--"
            Text(
                text = "Recorded at: $timeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChronologicalTrendCard(readings: List<SensorReading>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chronological Telemetry Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Measured data points plotted chronologically without interpolation or artificial smoothing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (readings.isEmpty()) {
                Text(
                    text = "No history points available for trend.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(label = "Moisture (%)", color = Color(0xFF28A745))
                    LegendItem(label = "Temp (°C)", color = Color(0xFFD97706))
                    LegendItem(label = "EC (dS/m)", color = Color(0xFF17A2B8))
                }
                Spacer(modifier = Modifier.height(12.dp))

                ChronologicalCanvasChart(
                    readings = readings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }
    }
}

@Composable
fun ChronologicalCanvasChart(
    readings: List<SensorReading>,
    modifier: Modifier = Modifier,
) {
    val greenColor = Color(0xFF28A745)
    val amberColor = Color(0xFFD97706)
    val cyanColor = Color(0xFF17A2B8)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val count = readings.size

            if (count < 1) return@Canvas

            // Draw horizontal grid lines
            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (count == 1) {
                // Single reading point
                val r = readings.first()
                val x = width / 2f
                r.soilMoisturePercent?.let {
                    drawCircle(color = greenColor, radius = 6.dp.toPx(), center = Offset(x, height * 0.5f))
                }
                return@Canvas
            }

            // Normalization ranges based on actual data
            val moistures = readings.mapNotNull { it.soilMoisturePercent }
            val temps = readings.mapNotNull { it.soilTemperatureCelsius }
            val ecs = readings.mapNotNull { it.soilEcDsPerM }

            val minMoisture = (moistures.minOrNull() ?: 0f).coerceAtMost(0f)
            val maxMoisture = (moistures.maxOrNull() ?: 100f).coerceAtLeast(100f)

            val minTemp = (temps.minOrNull() ?: 0f).coerceAtMost(0f)
            val maxTemp = (temps.maxOrNull() ?: 50f).coerceAtLeast(50f)

            val minEc = (ecs.minOrNull() ?: 0f).coerceAtMost(0f)
            val maxEc = (ecs.maxOrNull() ?: 5f).coerceAtLeast(5f)

            fun xPos(index: Int): Float = (index.toFloat() / (count - 1)) * width

            fun yPos(value: Float, minVal: Float, maxVal: Float): Float {
                val range = (maxVal - minVal).let { if (it <= 0f) 1f else it }
                val norm = (value - minVal) / range
                return height - (norm * (height - 16.dp.toPx()) + 8.dp.toPx())
            }

            // Plot Moisture
            val moisturePath = Path()
            var moistureStarted = false
            readings.forEachIndexed { idx, r ->
                r.soilMoisturePercent?.let { valVal ->
                    val x = xPos(idx)
                    val y = yPos(valVal, minMoisture, maxMoisture)
                    if (!moistureStarted) {
                        moisturePath.moveTo(x, y)
                        moistureStarted = true
                    } else {
                        moisturePath.lineTo(x, y)
                    }
                    drawCircle(color = greenColor, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
            if (moistureStarted) {
                drawPath(path = moisturePath, color = greenColor, style = Stroke(width = 2.dp.toPx()))
            }

            // Plot Temp
            val tempPath = Path()
            var tempStarted = false
            readings.forEachIndexed { idx, r ->
                r.soilTemperatureCelsius?.let { valVal ->
                    val x = xPos(idx)
                    val y = yPos(valVal, minTemp, maxTemp)
                    if (!tempStarted) {
                        tempPath.moveTo(x, y)
                        tempStarted = true
                    } else {
                        tempPath.lineTo(x, y)
                    }
                    drawCircle(color = amberColor, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
            if (tempStarted) {
                drawPath(path = tempPath, color = amberColor, style = Stroke(width = 2.dp.toPx()))
            }

            // Plot EC
            val ecPath = Path()
            var ecStarted = false
            readings.forEachIndexed { idx, r ->
                r.soilEcDsPerM?.let { valVal ->
                    val x = xPos(idx)
                    val y = yPos(valVal, minEc, maxEc)
                    if (!ecStarted) {
                        ecPath.moveTo(x, y)
                        ecStarted = true
                    } else {
                        ecPath.lineTo(x, y)
                    }
                    drawCircle(color = cyanColor, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
            if (ecStarted) {
                drawPath(path = ecPath, color = cyanColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

@Composable
fun HistoryLogCard(readings: List<SensorReading>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chronological Telemetry History Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (readings.isEmpty()) {
                Text(
                    text = "No historical telemetry log entries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                readings.take(10).forEach { reading ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatTimestamp(reading.timestampEpochMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "M: %s | T: %s | EC: %s".format(
                                    reading.soilMoisturePercent?.let { "%.1f%%".format(it) } ?: "--",
                                    reading.soilTemperatureCelsius?.let { "%.1f°C".format(it) } ?: "--",
                                    reading.soilEcDsPerM?.let { "%.2fdS/m".format(it) } ?: "--"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val (srcLabel, srcColor) = when (reading.source) {
                            SensorSource.LORA_FIELD_NODE -> Pair("LORA", Color(0xFF17A2B8))
                            SensorSource.BLE_DIRECT -> Pair("BLE", Color(0xFF28A745))
                            SensorSource.LOCAL_SIMULATION -> Pair("SIM", Color(0xFFD97706))
                        }
                        BadgeText(text = srcLabel, color = srcColor)
                    }
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
fun LegendItem(
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
