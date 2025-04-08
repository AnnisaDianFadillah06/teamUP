package com.example.teamup.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.data.model.MemberInviteModel
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(
    navController: NavController
) {
    // Sample data for both tabs
    val pendingMembers = remember {
        listOf(
            MemberInviteModel(
                id = "1",
                name = "Annisa Dian",
                email = "annisadian01@gmail.com",
                profileImage = R.drawable.captain_icon,
                status = "PENDING"
            )
        )
    }

    val waitingMembers = remember {
        listOf(
            MemberInviteModel(
                id = "2",
                name = "Annisa Dian",
                email = "annisadian01@gmail.com",
                profileImage = R.drawable.captain_icon,
                status = "WAITING"
            )
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Permintaan", "Menunggu")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Undang Anggota",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {/* ke screen select invite */ },
                containerColor = DodgerBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Member"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Anggota",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .background(DodgerBlue)
                    )
                },
                divider = { Divider(thickness = 1.dp, color = Color.LightGray) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) DodgerBlue else Color.Gray
                                )
                            )
                        },
                        modifier = Modifier.background(Color.White)
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Permintaan Tab
                    if (pendingMembers.isEmpty()) {
                        EmptyStateMessage("Tidak ada permintaan bergabung")
                    } else {
                        MemberRequestsList(
                            members = pendingMembers,
                            showActionButtons = true
                        )
                    }
                }
                1 -> {
                    // Menunggu Tab
                    if (waitingMembers.isEmpty()) {
                        EmptyStateMessage("Tidak ada undangan yang menunggu")
                    } else {
                        MemberRequestsList(
                            members = waitingMembers,
                            showActionButtons = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberRequestsList(
    members: List<MemberInviteModel>,
    showActionButtons: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (showActionButtons) {
            Text(
                text = "Anggota Meminta Bergabung",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Text(
                text = "Anggota Belum Merespon Undangan",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn {
            items(members) { member ->
                MemberRequestItem(member = member, showActionButtons = showActionButtons)
            }
        }
    }
}

@Composable
fun MemberRequestItem(
    member: MemberInviteModel,
    showActionButtons: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(member.profileImage)
                .crossfade(true)
                .build(),
            contentDescription = "Member Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Member Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = member.email,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
        }

        // Action buttons
        if (showActionButtons) {
            Button(
                onClick = { /* Handle accept */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF27AE60)
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Setuju")
            }

            Button(
                onClick = { /* Handle reject */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEB5757)
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Tolak")
            }
        } else {
            Text(
                text = "Menunggu",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}