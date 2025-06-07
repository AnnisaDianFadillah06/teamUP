package com.example.teamup.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.*
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.TeamRepository
import com.example.teamup.data.sources.remote.GoogleDriveHelper
import com.example.teamup.data.sources.remote.GoogleDriveTeamDataSource
import com.example.teamup.data.viewmodels.TeamViewModel
import com.example.teamup.data.viewmodels.TeamViewModelFactory
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    teamName: String = "Tim Lomba"
) {
    // Context for creating repositories and view models
    val context = LocalContext.current

    // Initialize repositories and view model
    val driveHelper = remember { GoogleDriveHelper(context) }
    val teamDataSource = remember { GoogleDriveTeamDataSource(context) }
    val teamRepository = remember { TeamRepository.getInstance(teamDataSource) }
    val teamViewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(teamRepository, driveHelper)
    )

    // Get current user ID
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Collect states from view model
    val uiState by teamViewModel.uiState.collectAsState()
    val userTeams by teamViewModel.userTeams.collectAsState()

// Cek status loading dan error
    if (uiState.isLoading) {
        // Tampilkan loading
    } else if (uiState.errorMessage != null) {
        // Tampilkan error
    } else {
        // Tampilkan data tim
        LazyColumn {
            items(userTeams) { team ->
                TeamListItem(team, navController, currentUserId)
            }
        }
    }



    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Anggota", "Admin")

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = DodgerBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Show loading indicator when loading
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DodgerBlue)
                }
            } else if (uiState.errorMessage != null) {
                // Show error message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.errorMessage ?: "Unknown error occurred")
                }
            } else {
                // Team members list based on tab selection
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTabIndex == 0) {
                        // All teams the user is a member of
                        items(userTeams) { team ->
                            TeamListItem(team, navController, currentUserId)
                        }

                        if (userTeams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "You haven't joined any teams yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        // Teams where user is captain/admin
                        val adminTeams = if (currentUserId != null) {
                            userTeams.filter { it.captainId == currentUserId }
                        } else {
                            emptyList()
                        }

                        items(adminTeams) { team ->
                            TeamListItem(team, navController, currentUserId, isAdmin = true)
                        }

                        if (adminTeams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "You're not an admin of any team",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet
        if (showBottomSheet) {
            LaunchedEffect(Unit) {
                bottomSheetState.show()
            }

            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                TeamOptionsBottomSheet(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun TeamListItem(
    team: TeamModel,
    navController: NavController,
    currentUserId: String?,
    isAdmin: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    Routes.TeamDetailGrup.createRoute(
                        teamId = team.id,
                        isJoined = team.members.contains(currentUserId),
                        isFull = team.isFull
                    )
                )
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Image/Avatar
            if (team.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(team.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Team Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = team.avatarResId),
                    contentDescription = "Team Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Team Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = team.category,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )

                // Display member count
                Text(
                    text = "${team.memberCount}/${team.maxMembers} anggota",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }

            // Role indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isAdmin || team.captainId == currentUserId) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DodgerBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DodgerBlue,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEEEEE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Member",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamOptionsBottomSheet(
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(Routes.JoinTeam.routes) {
                        popUpTo(Routes.JoinTeam.routes) {
                            inclusive = true
                        }
                    }
                },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = DodgerBlue)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Cari & Join Tim Lomba", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(Routes.FormAddTeam.routes) {
                        popUpTo(Routes.FormAddTeam.routes) {
                            inclusive = true
                        }
                    }
                },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = DodgerBlue)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Buat Tim Lomba", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}