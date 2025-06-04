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
import com.example.teamup.data.viewmodels.InviteSelectMemberViewModel
import com.example.teamup.data.viewmodels.SharedMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftInviteSelectMemberScreen(
    navController: NavController,
    sharedViewModel: SharedMemberViewModel // Gunakan SharedViewModel
) {
    val selectedMembers by sharedViewModel.selectedMembers.collectAsState()
    val selectedList = remember { mutableStateListOf<ProfileModel>() }

    // Update selectedList ketika selectedMembers berubah
    LaunchedEffect(selectedMembers) {
        selectedList.clear()
        selectedList.addAll(selectedMembers)
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<ProfileModel?>(null) }
    var showSendConfirmDialog by remember { mutableStateOf(false) }

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (selectedList.isEmpty()) {
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
                // List anggota yang dipilih
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

            // Tombol kirim undangan
            Button(
                onClick = {
                    if (selectedList.isNotEmpty()) {
                        showSendConfirmDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = selectedList.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Undang (${selectedList.size})",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
                    text = "Kamu akan mengirim undangan ke ${selectedList.size} calon anggota.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Tambahkan logika pengiriman undangan
                        // Setelah berhasil kirim, clear selected members
                        sharedViewModel.clearSelectedMembers()
                        showSendConfirmDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Undang")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSendConfirmDialog = false }
                ) {
                    Text("Kembali")
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
            // Profile image - handle both URL and resource
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

            // Remove button
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