package com.example.teamup.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.*
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.viewmodels.TeamDetailViewModel
import com.example.teamup.data.viewmodels.TeamDetailViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTeamScreen(
    navController: NavController,
    teamId: String = "1",
    isJoined: Boolean = false,
    isFull: Boolean = false,
    viewModel: TeamDetailViewModel = viewModel(
        factory = TeamDetailViewModelFactory(Injection.provideTeamRepository())
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val team by viewModel.team.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val teamAdmin by viewModel.teamAdmin.collectAsState()
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Load team data when screen launches
    LaunchedEffect(teamId) {
        viewModel.loadTeamData(teamId)
    }

    // Show error messages as Toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // PERBAIKAN: Logika untuk menampilkan semua anggota
    val allTeamMembers = remember(teamMembers, teamAdmin) {
        println("DEBUG: Creating allTeamMembers")
        println("DEBUG: teamMembers size = ${teamMembers.size}")
        println("DEBUG: teamAdmin = ${teamAdmin?.fullName}")

        val adminUserId = teamAdmin?.userId

        // Buat list dari semua teamMembers, tandai yang mana admin
        val membersList = teamMembers.map { member ->
            val isAdmin = adminUserId != null && member.userId == adminUserId
            println("DEBUG: Member ${member.fullName} - isAdmin: $isAdmin")
            Pair(member, isAdmin)
        }

        // Sort: admin dulu, kemudian alphabetical
        val sortedList = membersList.sortedWith(
            compareByDescending<Pair<UserProfileData, Boolean>> { it.second }
                .thenBy { it.first.fullName }
        )

        println("DEBUG: Final allTeamMembers size = ${sortedList.size}")
        sortedList.forEach { (member, isAdmin) ->
            println("DEBUG: Final list - ${member.fullName} (Admin: $isAdmin)")
        }

        sortedList
    }

    // Filtering members based on search query
    val filteredAllMembers = remember(allTeamMembers, searchQuery) {
        if (searchQuery.isEmpty()) {
            allTeamMembers
        } else {
            allTeamMembers.filter { (member, _) ->
                member.fullName.contains(searchQuery, ignoreCase = true) ||
                        member.email.contains(searchQuery, ignoreCase = true) ||
                        member.university.contains(searchQuery, ignoreCase = true) ||
                        member.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Team Header
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Team Image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri ?: team?.imageUrl ?: R.drawable.captain_icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Team Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (viewModel.isCurrentUserAdmin()) {
                                            showBottomSheet = true
                                        }
                                    }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Team Name
                            Text(
                                text = team?.name ?: "Team Name",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            // Team Description
                            Text(
                                text = team?.description ?: "Team Description",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )

                            // Team Category
                            Text(
                                text = team?.category ?: "Team Category",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Member count
                            Text(
                                text = "${team?.memberCount ?: 0}/${team?.maxMembers ?: 5} members",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            val isUserInTeam = viewModel.isCurrentUserInTeam()

                            if (!isUserInTeam) {
                                Button(
                                    onClick = { /* Handle join team */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !(team?.isFull ?: false),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DodgerBlue,
                                        disabledContainerColor = Color.Gray
                                    )
                                ) {
                                    Text(text = if (team?.isFull == true) "Full Team" else "Join")
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                Routes.ChatGroup.createRoute(
                                                    teamId = teamId,
                                                    teamName = team?.name ?: "Team Chat"
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.comments),
                                                contentDescription = "Message",
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Group Messages")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { navController.navigate(Routes.Invite.routes) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = !(team?.isFull ?: false),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2F80ED),
                                            disabledContainerColor = Color.Gray
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_right_24),
                                                contentDescription = if (team?.isFull == true) "Full" else "Invite",
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = if (team?.isFull == true) "Full Team" else "Invite Members")
                                        }
                                    }
                                }
                            }
                        }

                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Members") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = Color(0xFFF5F5F5),
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Members Section Header
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Team Members (${filteredAllMembers.size})",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            // Debug info (hapus di production)
                            Text(
                                text = "Debug: Found ${teamMembers.size} members, Admin: ${teamAdmin?.fullName ?: "None"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (filteredAllMembers.isEmpty() && searchQuery.isNotEmpty()) {
                                Text(
                                    text = "No members matching '$searchQuery'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else if (filteredAllMembers.isEmpty()) {
                                Text(
                                    text = "No members in this team yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // List of all team members
                    items(filteredAllMembers) { (member, isAdmin) ->
                        TeamMemberItem(
                            member = member,
                            isAdmin = isAdmin,
                            onChatClick = {
                                // Navigate to chat with member
                            }
                        )
                    }
                }
            }
        }

        // Bottom Sheet for Team Photo
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
                BottomSheetOption(
                    onImageSelected = { selectedUri ->
                        imageUri = selectedUri
                        viewModel.updateTeamPhoto(teamId, selectedUri)
                    },
                    dismissBottomSheet = { showBottomSheet = false }
                )
            }
        }
    }
}

@Composable
fun TeamMemberItem(
    member: UserProfileData,
    isAdmin: Boolean = false,
    onChatClick: () -> Unit
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
                .data(member.profilePictureUrl.ifEmpty { R.drawable.captain_icon })
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DodgerBlue.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = DodgerBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = member.email,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (member.university.isNotEmpty()) {
                Text(
                    text = "${member.university} - ${member.major}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Chat Icon
        IconButton(onClick = onChatClick) {
            Icon(
                painter = painterResource(id = R.drawable.comments),
                contentDescription = "Chat",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun BottomSheetOption(
    onImageSelected: (Uri) -> Unit,
    dismissBottomSheet: () -> Unit
) {
    val context = LocalContext.current
    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val uri = saveBitmapToCacheAndGetUri(context, it)
            onImageSelected(uri)
            dismissBottomSheet()
        }
    }

    val launcherGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onImageSelected(it)
            dismissBottomSheet()
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable {
                launcherCamera.launch()
            }
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable {
                launcherGallery.launch("image/*")
            }
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose from Gallery")
            }
        }
    }
}

fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}