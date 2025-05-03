package com.example.teamup.presentation.screen

import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.viewmodels.TeamViewModel
import com.example.teamup.data.viewmodels.TeamViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    navController: NavController,
    teamViewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(
            Injection.provideTeamRepository(),
            Injection.provideGoogleDriveHelper()
        )
    )
) {
    val teams by teamViewModel.teams.collectAsState()
    val uiState by teamViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(key1 = Unit) {
        teamViewModel.getAllTeams()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Teams",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.FormAddTeam.routes) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Team"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (teams.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No teams found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Routes.FormAddTeam.routes) }
                    ) {
                        Text("Create New Team")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(teams) { team ->
                        // Check if user is a member or captain of the team
                        val isJoined = currentUserId != null &&
                                (team.members.contains(currentUserId) || team.captainId == currentUserId)

                        // Create a copy of the team with updated isJoined status
                        val updatedTeam = team.copy(isJoined = isJoined)

                        TeamListItem(team = updatedTeam, navController = navController)
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun TeamListItem(
    team: TeamModel,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    Routes.TeamDetailGrup.createRoute(
                        teamId = team.id,
                        isJoined = team.isJoined,
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
            // Team Avatar - Load from imageUrl if available, fallback to resource
            if (team.imageUrl != null) {
                // Load from Google Drive URL
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(team.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Team Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.captain_icon)
                )
            } else {
                // Load from resource ID
                Image(
                    painter = painterResource(id = team.avatarResId),
                    contentDescription = "Team Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )

                Text(
                    text = "${team.memberCount}/${team.maxMembers} members",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }

            // Status indicator
            if (team.isJoined) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Joined",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            } else if (team.isFull) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEEEEEE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Full",
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