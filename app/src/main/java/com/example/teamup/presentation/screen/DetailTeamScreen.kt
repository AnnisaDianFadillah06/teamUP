package com.example.teamup.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.*
import com.example.teamup.data.model.TeamMemberModel
import com.example.teamup.route.Routes
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTeamScreen(
    navController: NavController,
    teamId: String = "1",
    isJoined: Boolean = false,
    isFull: Boolean = false
) {
    val teamMembers = remember {
        listOf(
            TeamMemberModel(id = "1", userId = "1234", teamId = "team_1", name = "Annisa Dian", profileImage = R.drawable.captain_icon, role = "Admin", email = "annisadian01@gmail.com"),
            TeamMemberModel(id = "2", userId = "5678", teamId = "team_1", name = "Alya Narina (Anda)", profileImage = R.drawable.captain_icon, role = "Member", email = "alyanarina@gmail.com")
        )
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Team Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri ?: R.drawable.captain_icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Team Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable {
                                showBottomSheet = true
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Team Name
                    Text(
                        text = "Al Fath",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Team Description
                    Text(
                        text = "KMIPN - Cipta Inovasi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons based on join status
                    if (!isJoined) {
                        // Not joined yet
                        Button(
                            onClick = { /* TODO: Handle join team */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isFull,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DodgerBlue,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text(text = if (isFull) "Full Tim" else "Join")
                        }
                    } else {
                        // Already joined
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // First button: Message Group
                            Button(
                                onClick = {
                                    navController.navigate(
                                        Routes.ChatGroup.createRoute(
                                            teamId = teamId,
                                            teamName = "Al Fath" // You might want to make this dynamic based on actual team data
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
                                    Text(text = "Pesan Grup")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Second button: Invite or Full Team
                            Button(
                                onClick = { navController.navigate(Routes.Invite.routes) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isFull,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2F80ED),
                                    disabledContainerColor = Color.Gray
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_right_24),
                                        contentDescription = if (isFull) "Full" else "Invite",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (isFull) "Full Tim" else "Undang Anggota")
                                }
                            }
                        }
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Cari Anggota") },
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

                // Admin Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    val adminMembers = teamMembers.filter { it.role == "Admin" }
                    adminMembers.forEach { member ->
                        TeamMemberItem(member)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Members Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Anggota",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    val regularMembers = teamMembers.filter { it.role == "Member" }
                    regularMembers.forEach { member ->
                        TeamMemberItem(member)
                    }

                    // Pending request section if needed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF7ED))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Permintaan Bergabung telah Berhasil",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF27AE60)
                                )
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_right_24),
                                contentDescription = "Close",
                                tint = Color(0xFF27AE60)
                            )
                        }
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
                    },
                    dismissBottomSheet = { showBottomSheet = false }
                )
            }
        }
    }
}

@Composable
fun TeamMemberItem(member: TeamMemberModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

        // Chat Icon
        IconButton(onClick = { /* TODO: Open chat with member */ }) {
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
    onImageSelected: (Uri) -> Unit, // ganti navigate dengan callback
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
                Text("Ambil Gambar")
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
                Text("Ambil dari Galeri")
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
