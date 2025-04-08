package com.example.teamup.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.viewmodels.InviteSelectMemberViewModel
import com.example.teamup.route.Routes

// Data models for this screen
data class FilterOption(
    val id: String,
    val name: String,
    val type: FilterType
)

enum class FilterType {
    UNIVERSITY, MAJOR, SKILL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSelectMemberScreen(
    navController: NavController,
    viewModel: InviteSelectMemberViewModel = remember { InviteSelectMemberViewModel() }
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()
    val members by viewModel.filteredMembers.collectAsState()
    val selectedCount = members.count { it.isSelected }

    // State for filter dialog
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Undang Anggota", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search and filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Cari Anggota") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Filter button
                OutlinedButton(
                    onClick = { showFilterDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                    if (selectedFilters.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = selectedFilters.size.toString(),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Selected filters summary (if any)
            if (selectedFilters.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Filter Aktif:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text(
                                text = selectedFilters.joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { viewModel.clearAllFilters() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Hapus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Member list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(members) { member ->
                    MemberItem(
                        member = member,
                        onToggleSelect = { viewModel.toggleMemberSelection(member.id) }
                    )
                }
            }

            // Draft Button
            Button(
                onClick = {
                    val selectedIds = viewModel.getSelectedMembers().joinToString(",") { it.id }
                    navController.navigate("draft_invitation/$selectedIds")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = selectedCount > 0,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCount > 0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Lihat Draft Undangan ($selectedCount)",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            viewModel = viewModel,
            selectedFilters = selectedFilters
        )
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    viewModel: InviteSelectMemberViewModel,
    selectedFilters: List<FilterOption>
) {
    var expandedUniversity by remember { mutableStateOf(true) }
    var expandedMajor by remember { mutableStateOf(false) }
    var expandedSkill by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearAllFilters() }) {
                        Text("Hapus Semua")
                    }
                }

                // University filter section
                FilterSection(
                    title = "Universitas",
                    isExpanded = expandedUniversity,
                    onToggleExpand = { expandedUniversity = !expandedUniversity },
                    content = {
                        FilterOptionsGrid(
                            options = viewModel.universityFilters,
                            selectedFilters = selectedFilters,
                            onFilterClick = { filter ->
                                if (selectedFilters.contains(filter)) {
                                    viewModel.removeFilter(filter)
                                } else {
                                    viewModel.addFilter(filter)
                                }
                            }
                        )
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Major filter section
                FilterSection(
                    title = "Jurusan",
                    isExpanded = expandedMajor,
                    onToggleExpand = { expandedMajor = !expandedMajor },
                    content = {
                        FilterOptionsGrid(
                            options = viewModel.majorFilters,
                            selectedFilters = selectedFilters,
                            onFilterClick = { filter ->
                                if (selectedFilters.contains(filter)) {
                                    viewModel.removeFilter(filter)
                                } else {
                                    viewModel.addFilter(filter)
                                }
                            }
                        )
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Skills filter section
                FilterSection(
                    title = "Keahlian",
                    isExpanded = expandedSkill,
                    onToggleExpand = { expandedSkill = !expandedSkill },
                    content = {
                        FilterOptionsGrid(
                            options = viewModel.skillFilters,
                            selectedFilters = selectedFilters,
                            onFilterClick = { filter ->
                                if (selectedFilters.contains(filter)) {
                                    viewModel.removeFilter(filter)
                                } else {
                                    viewModel.addFilter(filter)
                                }
                            }
                        )
                    }
                )

                // Apply button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Terapkan")
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onToggleExpand() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }
        }

        if (isExpanded) {
            content()
        }
    }
}

@Composable
fun FilterOptionsGrid(
    options: List<FilterOption>,
    selectedFilters: List<FilterOption>,
    onFilterClick: (FilterOption) -> Unit
) {
    FlowRow(
        mainAxisSpacing = 8,
        crossAxisSpacing = 8,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        options.forEach { filter ->
            val isSelected = selectedFilters.contains(filter)
            FilterChip(
                selected = isSelected,
                onClick = { onFilterClick(filter) },
                label = { Text(filter.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun MemberItem(
    member: ProfileModel,
    onToggleSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Image(
                painter = painterResource(id = member.imageResId),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Member info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }

            // Checkbox for selection instead of button
            Checkbox(
                checked = member.isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// Flow Row untuk layout filter yang lebih fleksibel
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Int = 0,
    crossAxisSpacing: Int = 0,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacingWidth = mainAxisSpacing.dp.roundToPx()
        val spacingHeight = crossAxisSpacing.dp.roundToPx()

        val rows = mutableListOf<MutableList<Int>>()
        val itemConstraints = constraints.copy(minWidth = 0)

        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }

        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Int>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        placeables.forEachIndexed { index, placeable ->
            if (currentRowWidth + placeable.width + (if (currentRow.isEmpty()) 0 else spacingWidth) > constraints.maxWidth) {
                // Start a new row
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)

                currentRow = mutableListOf(index)
                currentRowWidth = placeable.width
                currentRowHeight = placeable.height
            } else {
                // Add to current row
                currentRow.add(index)
                currentRowWidth += placeable.width + (if (currentRow.size > 1) spacingWidth else 0)
                currentRowHeight = maxOf(currentRowHeight, placeable.height)
            }
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rows.size - 1) * spacingHeight

        layout(constraints.maxWidth, totalHeight) {
            var y = 0

            rows.forEachIndexed { rowIndex, row ->
                var x = 0

                row.forEach { index ->
                    val placeable = placeables[index]
                    placeable.place(x, y)
                    x += placeable.width + spacingWidth
                }

                y += rowHeights[rowIndex] + spacingHeight
            }
        }
    }
}