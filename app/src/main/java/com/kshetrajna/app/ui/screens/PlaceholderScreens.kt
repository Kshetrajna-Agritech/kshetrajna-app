package com.kshetrajna.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.ui.base.FoundationViewModel

@Composable
fun FoundationScreenContainer(
    title: String,
    viewModel: FoundationViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Screen State Foundation",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (val currentState = state) {
                    is UiState.Success -> {
                        Text(
                            text = "Status: ${currentState.data}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is UiState.Loading -> {
                        Text(text = "Status: Loading...", style = MaterialTheme.typography.bodyMedium)
                    }
                    is UiState.Error -> {
                        Text(text = "Status: Error (${currentState.message})", style = MaterialTheme.typography.bodyMedium)
                    }
                    is UiState.Stale -> {
                        Text(text = "Status: Stale Data (${currentState.data})", style = MaterialTheme.typography.bodyMedium)
                    }
                    is UiState.SafetyLocked -> {
                        Text(text = "Status: Safety Locked (${currentState.reason})", style = MaterialTheme.typography.bodyMedium)
                    }
                    UiState.Empty -> {
                        Text(text = "Status: No Data", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}





@Composable
fun IrrigationScreen(viewModel: FoundationViewModel = FoundationViewModel()) {
    FoundationScreenContainer(title = "Irrigation Control", viewModel = viewModel)
}

@Composable
fun AlertsScreen(viewModel: FoundationViewModel = FoundationViewModel()) {
    FoundationScreenContainer(title = "Safety & System Alerts", viewModel = viewModel)
}

@Composable
fun SettingsScreen(viewModel: FoundationViewModel = FoundationViewModel()) {
    FoundationScreenContainer(title = "Settings & Diagnostics", viewModel = viewModel)
}
