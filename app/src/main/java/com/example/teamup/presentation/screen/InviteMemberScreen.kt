package com.example.teamup.presentation.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.data.model.MemberInviteModel
import com.example.teamup.data.model.MemberInviteModelV2
import com.example.teamup.data.repositories.InviteMemberRepositoryV2
import com.example.teamup.data.repositories.NotificationRepositoryV2
import com.example.teamup.data.sources.remote.FirebaseNotificationDataSourceV2
import com.example.teamup.data.viewmodels.InviteMemberViewModel
import com.example.teamup.data.viewmodels.NotificationViewModelV2
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(
    navController: NavController,
    inviteMemberViewModel: InviteMemberViewModel = viewModel(
        factory = InviteMemberViewModel.Factory(
            InviteMemberRepositoryV2.getInstance(),
            NotificationRepositoryV2.getInstance(FirebaseNotificationDataSourceV2.getInstance())
        )
    )
) {
    val pendingInvitations by inviteMemberViewModel.pendingInvitations.collectAsState()
    val waitingInvitations by inviteMemberViewModel.waitingInvitations.collectAsState()
    val isLoading by inviteMemberViewModel.isLoading.collectAsState()
    val actionState by inviteMemberViewModel.actionState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Permintaan", "Menunggu")

    // Handle action state changes
    LaunchedEffect(actionState) {
        when (actionState) {
            is InviteMemberViewModel.ActionState.Success -> {
                // Show success message (could use SnackBar)
                inviteMemberViewModel.resetActionState()
            }
            is InviteMemberViewModel.ActionState.Error -> {
                // Show error message (could use SnackBar)
                inviteMemberViewModel.resetActionState()
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
                onClick = { navController.navigate(Routes.InviteSelect.routes) },
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

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DodgerBlue)
                }
            } else {
                // Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        // Permintaan Tab (Invitations received by current user)
                        if (pendingInvitations.isEmpty()) {
                            // Show empty state
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tidak ada permintaan bergabung",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            MemberRequestsListV2(
                                members = pendingInvitations,
                                showActionButtons = true,
                                onAccept = { inviteId ->
                                    inviteMemberViewModel.acceptInvitation(inviteId)
                                },
                                onReject = { inviteId ->
                                    inviteMemberViewModel.rejectInvitation(inviteId)
                                },
                                isActionLoading = actionState is InviteMemberViewModel.ActionState.Loading
                            )
                        }
                    }
                    1 -> {
                        // Menunggu Tab (Invitations sent by current user)
                        if (waitingInvitations.isEmpty()) {
                            // Show empty state
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tidak ada undangan yang menunggu",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            MemberRequestsListV2(
                                members = waitingInvitations,
                                showActionButtons = false,
                                onAccept = { },
                                onReject = { },
                                isActionLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberRequestsListV2(
    members: List<MemberInviteModelV2>,
    showActionButtons: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    isActionLoading: Boolean
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
                MemberRequestItemV2(
                    member = member,
                    showActionButtons = showActionButtons,
                    onAccept = onAccept,
                    onReject = onReject,
                    isActionLoading = isActionLoading
                )
            }
        }
    }
}

@Composable
fun MemberRequestItemV2(
    member: MemberInviteModelV2,
    showActionButtons: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    isActionLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member Avatar
        if (member.profileImageUrl.isNotEmpty()) {
            AsyncImage(
                model = member.profileImageUrl,
                contentDescription = "Member Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.captain_icon),
                error = painterResource(id = R.drawable.captain_icon)
            )
        } else {
            Image(
                painter = painterResource(id = if (member.profileImageRes != 0) member.profileImageRes else R.drawable.captain_icon),
                contentDescription = "Member Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }

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
                onClick = { onAccept(member.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF27AE60)
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                enabled = !isActionLoading
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Setuju")
                }
            }

            Button(
                onClick = { onReject(member.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEB5757)
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                enabled = !isActionLoading
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Tolak")
                }
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