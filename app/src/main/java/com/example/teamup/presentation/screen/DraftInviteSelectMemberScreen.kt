package com.example.teamup.presentation.screen

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.teamup.R
import com.example.teamup.data.model.InvitationModel
import com.example.teamup.data.model.ProfileModel
import com.example.teamup.data.repositories.user.UserRepository
import com.example.teamup.data.viewmodels.InvitationUiState
import com.example.teamup.data.viewmodels.InvitationViewModel
import com.example.teamup.data.viewmodels.SharedMemberViewModel
import com.example.teamup.di.Injection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftInviteSelectMemberScreen(
    teamId: String,
    teamName: String,
    navController: NavController,
    sharedViewModel: SharedMemberViewModel,
    invitationViewModel: InvitationViewModel = Injection.provideInvitationViewModel(),
    userRepository: UserRepository = Injection.provideUserRepository()
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()

    val selectedMembers by sharedViewModel.selectedMembers.collectAsState()
    val invitationState by invitationViewModel.uiState.collectAsState()

    val selectedList = remember { mutableStateListOf<ProfileModel>() }

    // ✅ STATE untuk menyimpan user profile data
    var currentUserName by remember { mutableStateOf("") }
    var currentUserEmail by remember { mutableStateOf("") }
    var isLoadingUserData by remember { mutableStateOf(true) }

    // ✅ FETCH USER DATA saat screen load
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            isLoadingUserData = true
            scope.launch {
                try {
                    val userProfile = userRepository.getUser(userId)
                    currentUserName = userProfile?.fullName ?: currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Admin"
                    currentUserEmail = userProfile?.email ?: currentUser.email ?: ""

                    Log.d("DraftInvite", "✅ User loaded: name=$currentUserName, email=$currentUserEmail")
                } catch (e: Exception) {
                    Log.e("DraftInvite", "❌ Error loading user: ${e.message}")
                    currentUserName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Admin"
                    currentUserEmail = currentUser.email ?: ""
                } finally {
                    isLoadingUserData = false
                }
            }
        }
    }

    LaunchedEffect(selectedMembers) {
        selectedList.clear()
        selectedList.addAll(selectedMembers)
    }

    // ✅ HANDLE INVITATION STATE
    LaunchedEffect(invitationState) {
        when (val state = invitationState) {
            is InvitationUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                invitationViewModel.resetState()
                sharedViewModel.clearSelectedMembers()

                navController.navigate("invite_member/$teamId/$teamName") {
                    popUpTo("invite_member/$teamId/$teamName") {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
            is InvitationUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                invitationViewModel.resetState()
            }
            else -> {}
        }
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

            // ✅ TOMBOL KIRIM UNDANGAN
            Button(
                onClick = {
                    if (selectedList.isNotEmpty() && !isLoadingUserData) {
                        showSendConfirmDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = selectedList.isNotEmpty() &&
                        !isLoadingUserData &&
                        invitationState !is InvitationUiState.Loading,
                shape = RoundedCornerShape(8.dp)
            ) {
                when {
                    isLoadingUserData -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    invitationState is InvitationUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Text(
                            text = "Undang (${selectedList.size})",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog konfirmasi hapus
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

    // ✅ DIALOG KONFIRMASI KIRIM (FIXED - USE REAL USER DATA)
    if (showSendConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSendConfirmDialog = false },
            title = {
                Text(text = "Kirim Undangan")
            },
            text = {
                Column {
                    Text(
                        text = "Kamu akan mengirim undangan ke ${selectedList.size} calon anggota.",
                        textAlign = TextAlign.Center
                    )
                    // Debug info (hapus di production)
                    if (currentUserName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dari: $currentUserName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentUser?.let { user ->
                            // ✅ LOG untuk debugging
                            Log.d("DraftInvite", "Sending invitations:")
                            Log.d("DraftInvite", "  - Sender ID: ${user.uid}")
                            Log.d("DraftInvite", "  - Sender Name: $currentUserName")
                            Log.d("DraftInvite", "  - Sender Email: $currentUserEmail")

                            // ✅ Create COMPLETE InvitationModel objects with real data
                            val completeInvitations = selectedList.map { member ->
                                InvitationModel(
                                    teamId = teamId,
                                    teamName = teamName,
                                    senderId = user.uid,
                                    senderName = currentUserName, // ✅ REAL NAME from Firestore
                                    senderEmail = currentUserEmail, // ✅ REAL EMAIL from Firestore
                                    recipientId = member.id,
                                    recipientName = member.name,
                                    recipientEmail = member.email,
                                    createdAt = Timestamp.now(),
                                    updatedAt = Timestamp.now()
                                )
                            }

                            invitationViewModel.sendInvitations(
                                teamId = teamId,
                                teamName = teamName,
                                senderId = user.uid,
                                senderName = currentUserName, // ✅ PASS REAL NAME
                                senderEmail = currentUserEmail, // ✅ PASS REAL EMAIL
                                recipientsList = completeInvitations
                            )
                        }
                        showSendConfirmDialog = false
                    },
                    enabled = currentUserName.isNotEmpty() // ✅ Only enable if user data loaded
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