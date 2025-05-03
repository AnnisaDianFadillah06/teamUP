package com.example.teamup.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModelDummy
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.repositories.NotificationRepository
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSource
import com.example.teamup.data.viewmodels.JoinTeamViewModel
import com.example.teamup.data.viewmodels.NotificationViewModel
import com.example.teamup.presentation.components.NotificationIcon
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTeamScreen(
    navController: NavController,
    viewModel: JoinTeamViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsState()
    val popularTeams by viewModel.popularTeams.collectAsState()

    // Get NotificationViewModel to display unread count
    val notificationRepository = NotificationRepository.getInstance(
        FirebaseNotificationDataSource(context)
    )
    val notificationViewModel = NotificationViewModel.getInstance(notificationRepository)
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadCategories()
        viewModel.loadPopularTeams()
        notificationViewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Tim Lomba",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ))},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    NotificationIcon(
                        count = unreadCount,
                        onClick = { navController.navigate(Routes.Notifications.routes) }
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari Tim Lomba") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Category Section
            item {
                Text(
                    text = "Kategori Lomba",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Category Cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { navController.navigate(Routes.TeamListCategory.routes)
                            }
                        )
                    }
                }
            }

            // "See More" Button
            item {
                TextButton(
                    onClick = { /* Navigate to all categories */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Lainnya",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "See more",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Popular Teams Section
            item {
                Text(
                    text = "Tim Lomba Popular",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Popular Team Cards
            items(popularTeams) { team ->
                TeamCard(
                    team = team,
                    navController = navController // Add NavController parameter
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CompetitionModelDummy,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.iconResId),
                contentDescription = category.name,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${category.teamCount} Tim Lomba",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                color = Color.Gray
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TeamCard(
    team: TeamModel,
    navController: NavController // Add NavController parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navigate directly from the card
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
            // Team Avatar
            Image(
                painter = painterResource(id = team.avatarResId),
                contentDescription = "Team Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

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

            // Status indicators
            Column(
                horizontalAlignment = Alignment.End
            ) {
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
}