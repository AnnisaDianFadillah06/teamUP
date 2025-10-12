package com.example.teamup.presentation.screen

import android.widget.Toast
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.data.model.JoinRequestModel
import com.example.teamup.data.model.InvitationModel
import com.example.teamup.data.viewmodels.JoinRequestUiState
import com.example.teamup.data.viewmodels.JoinRequestViewModel
import com.example.teamup.data.viewmodels.InvitationViewModel
import com.example.teamup.di.Injection
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(
    teamId: String,
    teamName: String,
    navController: NavController,
    joinRequestViewModel: JoinRequestViewModel = Injection.provideJoinRequestViewModel(),
    invitationViewModel: InvitationViewModel = Injection.provideInvitationViewModel() // ✅ TAMBAH INI
) {
    val teamRequests by joinRequestViewModel.teamRequests.collectAsState()
    val uiState by joinRequestViewModel.uiState.collectAsState()

    // ✅ TAMBAH: Observe team invitations
    val teamInvitations by invitationViewModel.teamInvitations.collectAsState()

    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Permintaan", "Menunggu")

    // ✅ LOAD DATA saat screen launch
    LaunchedEffect(teamId) {
        joinRequestViewModel.loadTeamJoinRequests(teamId)
        invitationViewModel.loadTeamInvitations(teamId) // ✅ Load invitations
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is JoinRequestUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                joinRequestViewModel.resetState()
            }
            is JoinRequestUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                joinRequestViewModel.resetState()
            }
            else -> {}
        }
    }

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
                onClick = {
                    navController.navigate("invite_select/$teamId/$teamName")
                },
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
                            val count = if (index == 0) teamRequests.size else teamInvitations.size
                            val displayTitle = "$title ($count)"
                            Text(
                                text = displayTitle,
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
                    // TAB PERMINTAAN
                    if (teamRequests.isEmpty()) {
                        EmptyStateMessage("Tidak ada permintaan bergabung")
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Anggota Meminta Bergabung",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(16.dp)
                            )

                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(teamRequests) { request ->
                                    JoinRequestCard(
                                        request = request,
                                        onApprove = {
                                            joinRequestViewModel.handleJoinRequest(
                                                requestId = request.id,
                                                approve = true,
                                                teamId = request.teamId,
                                                requesterId = request.requesterId,
                                                requesterName = request.requesterName,
                                                teamName = request.teamName
                                            )
                                        },
                                        onReject = {
                                            joinRequestViewModel.handleJoinRequest(
                                                requestId = request.id,
                                                approve = false,
                                                teamId = request.teamId,
                                                requesterId = request.requesterId,
                                                requesterName = request.requesterName,
                                                teamName = request.teamName
                                            )
                                        },
                                        isLoading = uiState is JoinRequestUiState.Loading
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // ✅ TAB MENUNGGU - REAL DATA
                    if (teamInvitations.isEmpty()) {
                        EmptyStateMessage("Tidak ada undangan yang menunggu")
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Anggota Belum Merespon Undangan",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(16.dp)
                            )

                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(teamInvitations) { invitation ->
                                    WaitingInvitationCard(invitation = invitation)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ COMPOSABLE BARU untuk WAITING INVITATION
@Composable
fun WaitingInvitationCard(invitation: InvitationModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar dengan initial
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DodgerBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = invitation.recipientName.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Member Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invitation.recipientName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = invitation.recipientEmail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
            Text(
                text = formatTimestamp(invitation.createdAt),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFA726).copy(alpha = 0.2f)
        ) {
            Text(
                text = "Menunggu",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFA726),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun JoinRequestCard(
    request: JoinRequestModel,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DodgerBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = request.requesterName.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.requesterName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = request.requesterEmail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
            Text(
                text = formatTimestamp(request.createdAt),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onApprove,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF27AE60)
            ),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(32.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Setuju")
            }
        }

        Button(
            onClick = onReject,
            enabled = !isLoading,
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
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp.toDate().time
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "$minutes menit yang lalu"
        hours < 24 -> "$hours jam yang lalu"
        days < 7 -> "$days hari yang lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(timestamp.toDate())
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