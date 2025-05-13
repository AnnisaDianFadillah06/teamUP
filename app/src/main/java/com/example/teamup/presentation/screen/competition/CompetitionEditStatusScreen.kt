package com.example.teamup.presentation.screen.competition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.presentation.components.competition.CompetitionStatusForm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionEditStatusScreen(
    competitionId: String,
    viewModel: CompetitionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Find the competition from the list
    val competition = uiState.competitions.find { it.id == competitionId }

    // Local state for form data
    var visibilityStatus by remember { mutableStateOf(competition?.visibilityStatus ?: "") }
    var activityStatus by remember { mutableStateOf(competition?.activityStatus ?: "") }
    var deadlineString by remember { mutableStateOf("") }
    var autoCloseEnabled by remember { mutableStateOf(competition?.autoCloseEnabled ?: false) }

    // Initialize values when competition loads
    LaunchedEffect(competition) {
        competition?.let {
            visibilityStatus = it.visibilityStatus
            activityStatus = it.activityStatus
            autoCloseEnabled = it.autoCloseEnabled
        }
    }

    // Show success message
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Status kompetisi berhasil diperbarui")
            }
            viewModel.resetSuccess()
        }
    }

    // Show error message
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
            }
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Status Kompetisi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (competition == null) {
                Text(
                    text = "Kompetisi tidak ditemukan",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = competition.namaLomba,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CompetitionStatusForm(
                        competition = competition,
                        onVisibilityStatusChange = { visibilityStatus = it },
                        onActivityStatusChange = { activityStatus = it },
                        onDeadlineChange = { deadlineString = it },
                        onAutoCloseChange = { autoCloseEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.updateCompetitionStatus(
                                competitionId = competitionId,
                                visibilityStatus = visibilityStatus,
                                activityStatus = activityStatus,
                                tanggalTutupPendaftaran = if (deadlineString.isNotBlank()) deadlineString else null,
                                autoCloseEnabled = autoCloseEnabled
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}