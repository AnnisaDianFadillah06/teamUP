package com.example.teamup.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.teamup.R
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.viewmodels.SharedMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftInviteSelectMemberScreen(
    navController: NavController,
    sharedViewModel: SharedMemberViewModel,
    teamId: String = "default_team_id",
    teamName: String = "Tim Saya"
) {
    val selectedMembers by sharedViewModel.selectedMembers.collectAsState()
    val sendInviteState by sharedViewModel.sendInviteState.collectAsState()
    val selectedList = remember { mutableStateListOf<ProfileModel>() }

    // Update selectedList when selectedMembers changes
    LaunchedEffect(selectedMembers) {
        selectedList.clear()
        selectedList.addAll(selectedMembers)
    }

    // Handle send invite state changes
    var showSuccessDialog by remember { mutableStateOf(false) }
    LaunchedEffect(sendInviteState) {
        when (sendInviteState) {
            is SharedMemberViewModel.SendInviteState.Success -> {
                showSuccessDialog = true // Show success dialog
            }
            is SharedMemberViewModel.SendInviteState.Error -> {
                // Error handling remains in UI below
            }
            else -> {}
        }
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<ProfileModel?>(null) }
    var showSendConfirmDialog by remember { mutableStateOf(false) }

    // Create SnackbarHostState for managing Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Draft Undangan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = Color.Red.copy(alpha = 0.9f),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Show error message if exists
            if (sendInviteState is SharedMemberViewModel.SendInviteState.Error) {
                val errorState = sendInviteState as SharedMemberViewModel.SendInviteState.Error
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorState.message,
                            color = Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { sharedViewModel.resetSendInviteState() }
                        ) {
                            Text("Tutup", color = Color.Red)
                        }
                    }
                }

                // Show Snackbar for error
                LaunchedEffect(errorState) {
                    snackbarHostState.showSnackbar(
                        message = errorState.message,
                        actionLabel = "Tutup",
                        duration = SnackbarDuration.Long
                    )
                    sharedViewModel.resetSendInviteState()
                }
            }

            if (selectedList.isEmpty()) {
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
                            text = "Tidak ada anggota yang dipilih",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Kembali ke Pilih Anggota")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(selectedList) { member ->
                        SelectedMemberItem(
                            member = member,
                            onRemove = {
                                memberToDelete = member
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedList.isNotEmpty()) {
                        showSendConfirmDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = selectedList.isNotEmpty() && sendInviteState !is SharedMemberViewModel.SendInviteState.Loading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (sendInviteState is SharedMemberViewModel.SendInviteState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengirim...")
                    }
                } else {
                    Text(
                        text = "Undang (${selectedList.size})",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Dialog konfirmasi hapus anggota
    if (showDeleteConfirmDialog && memberToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                memberToDelete = null
            },
            title = {
                Text(text = "Hapus dari draft undangan")
            },
            text = {
                Text(
                    text = "Apakah kamu yakin ingin menghapus ${memberToDelete?.name} dari draft undangan?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        memberToDelete?.let { member ->
                            selectedList.remove(member)
                            sharedViewModel.removeSelectedMember(member.id)
                        }
                        showDeleteConfirmDialog = false
                        memberToDelete = null
                    }
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        memberToDelete = null
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog konfirmasi kirim undangan
    if (showSendConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSendConfirmDialog = false },
            title = {
                Text(text = "Kirim Undangan")
            },
            text = {
                Text(
                    text = "Kamu akan mengirim undangan ke ${selectedList.size} calon anggota untuk bergabung.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sharedViewModel.sendInvitations(teamId, teamName)
                        showSendConfirmDialog = false
                    }
                ) {
                    Text("Undang")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSendConfirmDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog untuk undangan berhasil terkirim
    if (showSuccessDialog && sendInviteState is SharedMemberViewModel.SendInviteState.Success) {
        val successState = sendInviteState as SharedMemberViewModel.SendInviteState.Success
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                sharedViewModel.resetSendInviteState()
                navController.popBackStack() // Navigate back after dismissing
            },
            title = {
                Text(text = "Undangan Terkirim")
            },
            text = {
                Text(
                    text = successState.message,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        sharedViewModel.resetSendInviteState()
                        navController.popBackStack() // Navigate back after confirming
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SelectedMemberItem(
    member: ProfileModel,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (member.profilePictureUrl.isNotEmpty()) {
                AsyncImage(
                    model = member.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.captain_icon),
                    error = painterResource(id = R.drawable.captain_icon)
                )
            } else {
                Image(
                    painter = painterResource(id = if (member.imageResId != 0) member.imageResId else R.drawable.captain_icon),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

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
                Text(
                    text = "${member.university} • ${member.major}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                if (member.skills.isNotEmpty()) {
                    Text(
                        text = member.skills.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color.Red
                )
            }
        }
    }
}