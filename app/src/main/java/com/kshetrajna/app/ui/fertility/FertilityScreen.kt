package com.kshetrajna.app.ui.fertility

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
import com.kshetrajna.app.domain.model.FertilityData
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SensorReading
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FertilityScreen(
    viewModel: FertilityViewModel,
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
                                text = "Unable to load fertility context",
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
                FertilityContent(
                    stateData = state.data,
                    onRequestInference = viewModel::onRequestInference,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No fertility data available")
                }
            }
        }
    }
}

@Composable
fun FertilityContent(
    stateData: FertilityUiStateData,
    onRequestInference: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val data = stateData.fertilityData

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Model Availability & Configuration Status Banner
        NpkModelStatusCard(data = data)

        // 2. Calculation Error / TBD Notification Card (if inference requested)
        if (stateData.calculationError != null) {
            CalculationErrorCard(message = stateData.calculationError)
        }

        // 3. Supporting Input/Context Data Card
        SupportingInputsCard(
            reading = data.latestReading,
            manualPh = data.latestManualPh
        )

        // 4. Inferred NPK Output Card
        NpkOutputCard(
            npkResult = data.latestNpkResult,
            isCalculating = stateData.isCalculating,
            onRequestInference = onRequestInference
        )

        // 5. Data Semantics & Provenance Legend Card
        ProvenanceLegendCard()
    }
}

@Composable
fun NpkModelStatusCard(data: FertilityData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fertility & NPK Inferences (${data.nodeId})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeText(
                    text = "INFERENCE MODEL TBD",
                    color = MaterialTheme.colorScheme.error
                )
                BadgeText(
                    text = "NO FABRICATED DATA",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.modelStatusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalculationErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Model Execution Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun SupportingInputsCard(
    reading: SensorReading?,
    manualPh: ManualPH?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Supporting Telemetry & Input Context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Measured sensor telemetry and manual entries used as supporting inputs for NPK analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Grid / Row of inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Soil EC
                InputMetricItem(
                    label = "Soil EC",
                    value = reading?.soilEcDsPerM?.let { "%.2f dS/m".format(it) } ?: "Unavailable",
                    timestamp = reading?.timestampEpochMillis?.let { formatTimestamp(it) } ?: "No reading",
                    categoryLabel = "MEASURED TELEMETRY",
                    categoryColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Soil Temp
                InputMetricItem(
                    label = "Soil Temp",
                    value = reading?.soilTemperatureCelsius?.let { "%.1f °C".format(it) } ?: "Unavailable",
                    timestamp = reading?.timestampEpochMillis?.let { formatTimestamp(it) } ?: "No reading",
                    categoryLabel = "MEASURED TELEMETRY",
                    categoryColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Soil Moisture
                InputMetricItem(
                    label = "Soil Moisture",
                    value = reading?.soilMoisturePercent?.let { "%.1f %%".format(it) } ?: "Unavailable",
                    timestamp = reading?.timestampEpochMillis?.let { formatTimestamp(it) } ?: "No reading",
                    categoryLabel = "MEASURED TELEMETRY",
                    categoryColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Manual pH
                InputMetricItem(
                    label = "Manual pH",
                    value = manualPh?.phValue?.let { "%.2f pH".format(it) } ?: "Unavailable",
                    timestamp = manualPh?.timestampEpochMillis?.let { formatTimestamp(it) } ?: "No entry",
                    categoryLabel = "MANUAL INPUT",
                    categoryColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InputMetricItem(
    label: String,
    value: String,
    timestamp: String,
    categoryLabel: String,
    categoryColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        BadgeText(text = categoryLabel, color = categoryColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NpkOutputCard(
    npkResult: NpkResult?,
    isCalculating: Boolean,
    onRequestInference: () -> Unit,
) {
    val nitrogenStr = npkResult?.inferredNitrogenPpm?.let { "%.1f ppm".format(it) } ?: "-- ppm"
    val phosphorusStr = npkResult?.inferredPhosphorusPpm?.let { "%.1f ppm".format(it) } ?: "-- ppm"
    val potassiumStr = npkResult?.inferredPotassiumPpm?.let { "%.1f ppm".format(it) } ?: "-- ppm"

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
                    text = "Inferred N/P/K Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BadgeText(text = "INFERRED OUTPUT (TBD)", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Inferred nitrogen (N), phosphorus (P), and potassium (K) concentration outputs in ppm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // N, P, K Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NpkValueItem(
                    nutrient = "Nitrogen (N)",
                    value = nitrogenStr,
                    modifier = Modifier.weight(1f)
                )
                NpkValueItem(
                    nutrient = "Phosphorus (P)",
                    value = phosphorusStr,
                    modifier = Modifier.weight(1f)
                )
                NpkValueItem(
                    nutrient = "Potassium (K)",
                    value = potassiumStr,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestInference,
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(20.dp).height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Executing Model...")
                } else {
                    Text(
                        text = "Request NPK Inference",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NpkValueItem(
    nutrient: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = nutrient,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ProvenanceLegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Data Provenance Semantics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProvenanceRow(tag = "MEASURED", description = "Direct field telemetry (EC, soil temp, soil moisture)")
            Spacer(modifier = Modifier.height(4.dp))
            ProvenanceRow(tag = "MANUAL", description = "User app-entered measurement (manual soil pH)")
            Spacer(modifier = Modifier.height(4.dp))
            ProvenanceRow(tag = "INFERRED", description = "Model output (N/P/K estimations, currently TBD)")
            Spacer(modifier = Modifier.height(4.dp))
            ProvenanceRow(tag = "SIMULATED", description = "M5 simulation scenario test fixture")
        }
    }
}

@Composable
fun ProvenanceRow(tag: String, description: String) {
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
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
