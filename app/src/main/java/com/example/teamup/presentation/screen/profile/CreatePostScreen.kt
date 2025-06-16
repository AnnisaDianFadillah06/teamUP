package com.example.teamup.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.user.ActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    userId: String,
    activityId: String? = null, // PERBAIKAN: Tambahkan parameter untuk edit mode
    activityViewModel: ActivityViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var postContent by remember { mutableStateOf("") }
    var selectedVisibility by remember { mutableStateOf("public") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDocumentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showVisibilityDropdown by remember { mutableStateOf(false) }

    // PERBAIKAN: Tambahkan state untuk edit mode
    val isEditMode = activityId != null
    val currentActivity by activityViewModel.currentActivity.collectAsState()

    // Observe ViewModel states
    val isLoading by activityViewModel.isLoading.collectAsState()
    val errorMessage by activityViewModel.errorMessage.collectAsState()

    // PERBAIKAN: Load activity data jika edit mode
    LaunchedEffect(activityId) {
        if (isEditMode && activityId != null) {
            // Jika currentActivity belum diset, load dari repository
            if (currentActivity == null) {
                activityViewModel.loadActivity(userId, activityId)
            }
        }
    }

    // PERBAIKAN: Populate form dengan data activity yang akan diedit
    LaunchedEffect(currentActivity) {
        currentActivity?.let { activity ->
            postContent = activity.content
            selectedVisibility = activity.visibility
            // Note: selectedImageUris tidak bisa diisi dengan URL yang sudah ada
            // Karena Uri dan URL berbeda. Untuk edit, user perlu pilih ulang gambar
            // atau Anda bisa menampilkan gambar existing sebagai preview saja
        }
    }

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // You can show a snackbar or toast here
            activityViewModel.clearError()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = selectedImageUris + uris
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedVideoUris = selectedVideoUris + uris
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedDocumentUris = selectedDocumentUris + uris
    }


    // Visibility options
    val visibilityOptions = listOf(
        "public" to "Semua orang",
        "connections" to "Koneksi",
        "private" to "Hanya saya"
    )

    fun getAllSelectedMediaUris(): List<Uri> {
        return selectedImageUris + selectedVideoUris + selectedDocumentUris
    }

    // PERBAIKAN: Function untuk create/update post
    fun savePost() {
        if (postContent.isNotBlank()) {
            coroutineScope.launch {
                val allMediaUris = getAllSelectedMediaUris()

                if (isEditMode && currentActivity != null) {
                    // Update existing activity
                    val updatedActivity = currentActivity!!.copy(
                        content = postContent,
                        visibility = selectedVisibility,
                        updatedAt = System.currentTimeMillis()
                    )
                    activityViewModel.updateActivity(
                        userId = userId,
                        activity = updatedActivity,
                        newMediaUris = allMediaUris.takeIf { it.isNotEmpty() }
                    ) { success ->
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                } else {
                    // Create new activity
                    activityViewModel.createActivityWithMedia(
                        userId = userId,
                        content = postContent,
                        mediaUris = allMediaUris.takeIf { it.isNotEmpty() },
                        visibility = selectedVisibility
                    ) { success ->
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // PERBAIKAN: Ubah title berdasarkan mode
                        if (isEditMode) "Edit Postingan" else "Buat Postingan",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Clear current activity saat keluar dari edit mode
                        if (isEditMode) {
                            activityViewModel.clearCurrentActivity()
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { savePost() }, // PERBAIKAN: Ganti dengan savePost
                        enabled = postContent.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = DodgerBlue
                                )
                                Text(
                                    if (isEditMode) "Menyimpan..." else "Posting...",
                                    color = SoftGray2,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                if (isEditMode) "Simpan" else "Posting",
                                color = if (postContent.isNotBlank()) DodgerBlue else SoftGray2,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // User Profile Section with Visibility Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DodgerBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AN",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Visibility Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showVisibilityDropdown,
                        onExpandedChange = { showVisibilityDropdown = it }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .menuAnchor()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = visibilityOptions.find { it.first == selectedVisibility }?.second
                                    ?: "Semua orang",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select visibility",
                                modifier = Modifier.size(20.dp),
                                tint = SoftGray2
                            )
                        }

                        ExposedDropdownMenu(
                            expanded = showVisibilityDropdown,
                            onDismissRequest = { showVisibilityDropdown = false }
                        ) {
                            visibilityOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedVisibility = value
                                        showVisibilityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // PERBAIKAN: Tampilkan existing media jika edit mode
            if (isEditMode && currentActivity?.hasMedia() == true) {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        "Media yang sudah ada:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    currentActivity?.getAllMediaUrls()?.chunked(2)?.forEach { rowUrls ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowUrls.forEach { url ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(120.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Existing media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            if (rowUrls.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Post Content TextField
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                placeholder = {
                    Text(
                        "Apa yang ingin Anda bicarakan?",
                        color = SoftGray2,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media Action Buttons - Pindah ke atas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image picker
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            DodgerBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Tambah foto",
                        tint = if (isLoading) SoftGray2.copy(alpha = 0.5f) else DodgerBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Video picker
                IconButton(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            DodgerBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = "Tambah video",
                        tint = if (isLoading) SoftGray2.copy(alpha = 0.5f) else DodgerBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Document picker
                IconButton(
                    onClick = { documentPickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            DodgerBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Tambah dokumen",
                        tint = if (isLoading) SoftGray2.copy(alpha = 0.5f) else DodgerBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Add text labels below icons for better UX
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Foto",
                    fontSize = 12.sp,
                    color = SoftGray2,
                    modifier = Modifier.width(44.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Video",
                    fontSize = 12.sp,
                    color = SoftGray2,
                    modifier = Modifier.width(44.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Dokumen",
                    fontSize = 12.sp,
                    color = SoftGray2,
                    modifier = Modifier.width(44.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Selected Media Previews
            if (selectedImageUris.isNotEmpty() || selectedVideoUris.isNotEmpty() || selectedDocumentUris.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        "Media yang dipilih (${getAllSelectedMediaUris().size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Images Preview
                    if (selectedImageUris.isNotEmpty()) {
                        Text(
                            "Foto (${selectedImageUris.size})",
                            fontSize = 12.sp,
                            color = SoftGray2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        selectedImageUris.chunked(2).forEach { rowUris ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowUris.forEach { uri ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(120.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "Selected image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            IconButton(
                                                onClick = {
                                                    selectedImageUris = selectedImageUris.filter { it != uri }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .background(
                                                        Color.Black.copy(alpha = 0.5f),
                                                        CircleShape
                                                    )
                                                    .size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove image",
                                                    tint = White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (rowUris.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Videos Preview
                    if (selectedVideoUris.isNotEmpty()) {
                        Text(
                            "Video (${selectedVideoUris.size})",
                            fontSize = 12.sp,
                            color = SoftGray2,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )

                        selectedVideoUris.forEach { uri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.VideoLibrary,
                                            contentDescription = "Video",
                                            tint = DodgerBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Video dipilih",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedVideoUris = selectedVideoUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove video",
                                            tint = White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Documents Preview
                    if (selectedDocumentUris.isNotEmpty()) {
                        Text(
                            "Dokumen (${selectedDocumentUris.size})",
                            fontSize = 12.sp,
                            color = SoftGray2,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )

                        selectedDocumentUris.forEach { uri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Document",
                                            tint = DodgerBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Dokumen dipilih",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedDocumentUris = selectedDocumentUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove document",
                                            tint = White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Character count indicator (pindah ke paling bawah sebelum closing bracket)
            if (postContent.isNotEmpty()) {
                Text(
                    text = "${postContent.length}/1000",
                    fontSize = 12.sp,
                    color = if (postContent.length > 1000) Color.Red else SoftGray2,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )
            }

        }
    }
}