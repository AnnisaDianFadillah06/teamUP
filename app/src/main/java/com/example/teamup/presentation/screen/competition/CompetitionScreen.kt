package com.example.teamup.presentation.screen.competition

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.viewmodels.CabangLombaViewModel
import com.example.teamup.data.viewmodels.CabangLombaViewModelFactory
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.competition.AddCompetitionForm
import com.example.teamup.presentation.components.competition.EditCompetitionForm
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionScreen(
    navController: NavHostController,
    viewModel: CompetitionViewModel = viewModel(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository(),
            Injection.provideCabangLombaRepository()
        )
    ),
    cabangLombaViewModel: CabangLombaViewModel = viewModel(
        factory = CabangLombaViewModelFactory(
            Injection.provideCabangLombaRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }
    var showEditForm by remember { mutableStateOf(false) }
    var selectedCompetition by remember { mutableStateOf<CompetitionModel?>(null) }

    BackHandler(enabled = showAddForm || showEditForm) {
        showAddForm = false
        showEditForm = false
        selectedCompetition = null
    }

    Scaffold(
        topBar = {
            // ✅ FIXED: Match HomeScreenV5 navbar style exactly
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ Back arrow + Title in Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (showAddForm || showEditForm) {
                                    showAddForm = false
                                    showEditForm = false
                                    selectedCompetition = null
                                } else {
                                    navController.navigate(Routes.HomeV5.routes) {
                                        popUpTo(Routes.HomeV5.routes) { inclusive = true }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // ✅ Title with same style as "TeamUp" in HomeScreenV5
                        Text(
                            text = when {
                                showAddForm -> "Buat Kompetisi Baru"
                                showEditForm -> "Edit Kompetisi"
                                else -> "Kompetisi"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showAddForm && !showEditForm) {
                FloatingActionButton(onClick = { showAddForm = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Competition")
                }
            }
        }
    ) { paddingValues ->
        when {
            showAddForm -> {
                AddCompetitionForm(
                    viewModel = viewModel,
                    onSuccess = { showAddForm = false },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            showEditForm && selectedCompetition != null -> {
                EditCompetitionForm(
                    competition = selectedCompetition!!,
                    viewModel = viewModel,
                    cabangLombaViewModel = cabangLombaViewModel,
                    onSuccess = {
                        showEditForm = false
                        selectedCompetition = null
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                CompetitionListContent(
                    uiState = uiState,
                    onAddClick = { showAddForm = true },
                    onEditClick = { competition ->
                        selectedCompetition = competition
                        showEditForm = true
                    },
                    onDetailClick = { competition ->
                        navController.navigate(Routes.CompetitionDetail.createRoute(competition.id))
                    },
                    cabangLombaViewModel = cabangLombaViewModel,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}