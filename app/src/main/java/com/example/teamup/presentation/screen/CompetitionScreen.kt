package com.example.teamup.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.teamup.data.viewmodels.CabangLombaViewModel
import com.example.teamup.data.viewmodels.CabangLombaViewModelFactory
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.AddCompetitionForm
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.presentation.components.CustomBottomNavigationBar

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

    // Handle hardware back button
    BackHandler(enabled = showAddForm) {
        showAddForm = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showAddForm) "Buat Kompetisi Baru" else "Kompetisi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (showAddForm) {
                        IconButton(onClick = { showAddForm = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB when not in add form mode
            if (!showAddForm) {
                FloatingActionButton(onClick = { showAddForm = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Competition")
                }
            }
        },
        bottomBar = {
            if (showAddForm) {
                CustomBottomNavigationBar(
                    navController = navController,
                    onCompetitionClick = { showAddForm = false }
                )
            } else {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        if (showAddForm) {
            AddCompetitionForm(
                viewModel = viewModel,
                onSuccess = { showAddForm = false },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CompetitionListContent(
                uiState = uiState,
                onAddClick = { showAddForm = true },
                cabangLombaViewModel = cabangLombaViewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
