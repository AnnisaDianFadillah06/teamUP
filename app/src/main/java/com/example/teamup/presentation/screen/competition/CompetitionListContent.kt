package com.example.teamup.presentation.screen.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.viewmodels.CabangLombaViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionListContent(
    uiState: CompetitionViewModel.CompetitionUiState,
    onAddClick: () -> Unit,
    onEditClick: (CompetitionModel) -> Unit,
    onDetailClick: (CompetitionModel) -> Unit = {}, // ✅ Add detail click
    cabangLombaViewModel: CabangLombaViewModel,
    viewModel: CompetitionViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Semua Status") }
    var showCabangMenu by remember { mutableStateOf(false) }
    var selectedCabang by remember { mutableStateOf("Semua Cabang Lomba") }

    // ✅ Simple refresh state without pull-to-refresh for now
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    // Fetch all cabang lomba data once when the screen loads
    LaunchedEffect(Unit) {
        cabangLombaViewModel.getAllCabangLomba()
    }

    // Get all cabang data
    val cabangUiState by cabangLombaViewModel.uiState.collectAsState()

    // Group cabang entries by competitionId for efficient lookup
    val cabangByCompetitionId = cabangUiState.allCabangList.groupBy { it.competitionId }

    // Get all unique cabang names for the dropdown
    val allUniqueCabang = cabangUiState.allCabangList.map { it.namaCabang }.distinct().sorted()

    // Define our filter options
    val filterOptions = listOf(
        "Semua Status",
        "Terbuka (Aktif & Published)",
        "Ditutup",
        "Draft",
        "Dibatalkan"
    )

    val filteredCompetitions = uiState.competitions.filter { competition ->
        val matchesSearch = competition.namaLomba.contains(searchQuery, ignoreCase = true) ||
                competition.deskripsiLomba.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Semua Status" -> true
            "Terbuka (Aktif & Published)" ->
                competition.visibilityStatus == CompetitionVisibilityStatus.PUBLISHED.value &&
                        competition.activityStatus == CompetitionActivityStatus.ACTIVE.value
            "Ditutup" ->
                competition.visibilityStatus == CompetitionVisibilityStatus.PUBLISHED.value &&
                        competition.activityStatus == CompetitionActivityStatus.INACTIVE.value
            "Draft" -> competition.visibilityStatus == CompetitionVisibilityStatus.DRAFT.value
            "Dibatalkan" -> competition.visibilityStatus == CompetitionVisibilityStatus.CANCELLED.value
            else -> true
        }

        val competitionCabangList = cabangByCompetitionId[competition.id] ?: emptyList()
        val cabangNames = competitionCabangList.map { it.namaCabang }
        val matchesCabang = selectedCabang == "Semua Cabang Lomba" ||
                cabangNames.contains(selectedCabang)

        matchesSearch && matchesFilter && matchesCabang
    }

    Column(modifier = modifier.fillMaxSize()) {
        // ✅ Refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kompetisi (${filteredCompetitions.size})",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = {
                    isRefreshing = true
                    viewModel.refreshData()
                    cabangLombaViewModel.getAllCabangLomba()
                }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cari kompetisi di sini") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Filter chip
            Box {
                FilterChip(
                    selected = false,
                    onClick = { showFilterMenu = !showFilterMenu },
                    label = { Text(selectedFilter) },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Status Filter")
                    }
                )
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedFilter = option
                                showFilterMenu = false
                            }
                        )
                    }
                }
            }

            // Cabang chip
            Box {
                FilterChip(
                    selected = false,
                    onClick = { showCabangMenu = !showCabangMenu },
                    label = { Text(selectedCabang) },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Cabang Filter")
                    }
                )
                DropdownMenu(
                    expanded = showCabangMenu,
                    onDismissRequest = { showCabangMenu = false }
                ) {
                    val cabangOptions = listOf("Semua Cabang Lomba") + allUniqueCabang
                    cabangOptions.forEach { cabang ->
                        DropdownMenuItem(
                            text = { Text(cabang) },
                            onClick = {
                                selectedCabang = cabang
                                showCabangMenu = false
                            }
                        )
                    }
                }
            }
        }

        when {
            uiState.isLoading && uiState.competitions.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            filteredCompetitions.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.task2),
                        contentDescription = "No Competition",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedFilter != "Semua Status" || selectedCabang != "Semua Cabang Lomba")
                            "Tidak ada kompetisi yang sesuai dengan filter"
                        else
                            "Belum ada kompetisi tersedia",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Buat Kompetisi")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCompetitions) { competition ->
                        val associatedCabangList = cabangByCompetitionId[competition.id] ?: emptyList()

                        // ✅ Enhanced CompetitionCard with detail click
                        EnhancedCompetitionCard(
                            competition = competition,
                            associatedCabangList = associatedCabangList,
                            onEditClick = onEditClick,
                            onDetailClick = { selectedCompetition ->
                                // ✅ Navigate to detail screen with competition ID
                                onDetailClick(selectedCompetition)
                            }
                        )
                    }
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}