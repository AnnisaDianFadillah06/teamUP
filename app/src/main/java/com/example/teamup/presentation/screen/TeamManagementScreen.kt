package com.example.teamup.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.*
import com.example.teamup.data.model.TeamMemberModel
import com.example.teamup.route.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    navController: NavController,
    teamName: String = "Tim Lomba"
) {
    val teamMembers = remember {
        listOf(
            TeamMemberModel(id = "1", userId = "1234", teamId = "team_1", name = "Nama Tim1", profileImage = R.drawable.captain_icon, role = "Admin", email = "annisadian01@gmail.com"),
            TeamMemberModel(id = "2", userId = "5678", teamId = "team_1", name = "Nama Tim2", profileImage = R.drawable.captain_icon, role = "Member", email = "annisadian01@gmail.com"),
            TeamMemberModel(id = "3", userId = "9012", teamId = "team_1", name = "Nama Tim3", profileImage = R.drawable.captain_icon, role = "Member", email = "annisadian01@gmail.com")
        )
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

            // Team members list with Card
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(teamMembers) { member ->
                    if ((selectedTabIndex == 0) || (selectedTabIndex == 1 && member.role == "Admin")) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            TeamMemberItem(member)
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
fun TeamManagementMemberItem(member: TeamMemberModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = member.profileImage),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = member.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(text = member.id, style = MaterialTheme.typography.bodyMedium.copy(color = SoftGray2))
        }

        Spacer(modifier = Modifier.weight(1f))

        if (member.role == "Admin") {
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DodgerBlue,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .background(color = DodgerBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
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
