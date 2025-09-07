//AddEditExperienceScreen.kt
package com.example.teamup.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.user.ExperienceViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExperienceScreen(
    navController: NavController,
    experienceId: String? = null,
    experienceViewModel: ExperienceViewModel = viewModel()
) {
    val isEditing = experienceId != null
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Observe ViewModel states
    val currentExperience by experienceViewModel.currentExperience.collectAsState()
    val isLoading by experienceViewModel.isLoading.collectAsState()
    val errorMessage by experienceViewModel.errorMessage.collectAsState()

    // Form states
    var position by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var isCurrentRole by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var locationType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showJobTypeDropdown by remember { mutableStateOf(false) }
    var showLocationTypeDropdown by remember { mutableStateOf(false) }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var skillsText by remember { mutableStateOf("") }

    // For showing save success/error messages
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val jobTypes = listOf("Full-time", "Part-time", "Contract", "Freelance", "Internship")
    val locationTypes = listOf("On-site", "Remote", "Hybrid")

    // Media picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        mediaUris = mediaUris + uris
    }

    // Load existing data if editing
    LaunchedEffect(experienceId) {
        if (isEditing && experienceId != null && currentUser?.uid != null) {
            experienceViewModel.loadExperience(currentUser.uid, experienceId)
        }
    }

    // Populate form when data is loaded
    LaunchedEffect(currentExperience) {
        currentExperience?.let { exp ->
            position = exp.position
            jobType = exp.jobType
            company = exp.company
            isCurrentRole = exp.isCurrentRole
            startDate = exp.startDate
            endDate = exp.endDate
            location = exp.location
            locationType = exp.locationType
            description = exp.description
            skills = exp.skills
            skillsText = exp.skills.joinToString(", ")
        }
    }

    // Show error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarMessage = it
            showSnackbar = true
            experienceViewModel.clearError()
        }
    }

    // Check if form is valid for saving
    val isFormValid = position.isNotEmpty() &&
            company.isNotEmpty() &&
            startDate.isNotEmpty() &&
            (isCurrentRole || endDate.isNotEmpty()) && // perbaiki logika ini
            currentUser?.uid != null


    // Save function
    val saveExperience = {
        currentUser?.uid?.let { userId ->
            // Pastikan ID experience ada saat editing
            val finalExperienceId = if (isEditing) {
                experienceId ?: currentExperience?.id
            } else {
                null // null untuk experience baru
            }

            experienceViewModel.saveExperience(
                userId = userId,
                position = position,
                jobType = jobType,
                company = company,
                startDate = startDate,
                endDate = if (isCurrentRole) "" else endDate,
                isCurrentRole = isCurrentRole,
                location = location,
                locationType = locationType,
                description = description,
                skills = skills,
                mediaUris = mediaUris,
                experienceId = finalExperienceId // gunakan finalExperienceId
            ) { success ->
                if (success) {
                    snackbarMessage = if (isEditing) "Pengalaman berhasil diperbarui" else "Pengalaman berhasil ditambahkan"
                    showSnackbar = true
                    navController.popBackStack()
                } else {
                    snackbarMessage = "Gagal menyimpan pengalaman"
                    showSnackbar = true
                }
            }
        } ?: run {
            snackbarMessage = "Error: User tidak terautentikasi"
            showSnackbar = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit pengalaman" else "Tambah pengalaman")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    // Save button in top bar
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 16.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = saveExperience,
                            enabled = isFormValid
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = if (isEditing) "Update" else "Save",
                                tint = if (isFormValid) White else White.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White
                )
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form validation message (moved to top)
            if (!isFormValid && !isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = when {
                            currentUser?.uid == null -> "Error: User tidak terautentikasi"
                            position.isEmpty() -> "Posisi harus diisi"
                            company.isEmpty() -> "Perusahaan harus diisi"
                            startDate.isEmpty() -> "Tanggal mulai harus diisi"
                            !isCurrentRole && endDate.isEmpty() -> "Tanggal berakhir harus diisi"
                            else -> "Mohon lengkapi semua field yang wajib"
                        },
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }

            // Current User Info (for debugging - remove in production)
            if (currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "User: ${currentUser.email ?: "Unknown"}\nUID: ${currentUser.uid}",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = Color.Green
                    )
                }
            }

            // Position Field
            Column {
                Text(
                    "Posisi*",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { if (it.length <= 100) position = it },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (position.isNotEmpty()) {
                            IconButton(onClick = { position = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    supportingText = { Text("${position.length}/100") },
                    isError = position.isEmpty()
                )
            }

            // Job Type Dropdown
            Column {
                Text(
                    "Jenis pekerjaan",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                ExposedDropdownMenuBox(
                    expanded = showJobTypeDropdown,
                    onExpandedChange = { showJobTypeDropdown = !showJobTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = jobType.ifEmpty { "Silakan pilih" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showJobTypeDropdown) }
                    )
                    ExposedDropdownMenu(
                        expanded = showJobTypeDropdown,
                        onDismissRequest = { showJobTypeDropdown = false }
                    ) {
                        jobTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    jobType = type
                                    showJobTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Company Field
            Column {
                Text(
                    "Perusahaan atau organisasi*",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Company logo placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            company.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = company,
                        onValueChange = { if (it.length <= 100) company = it },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            if (company.isNotEmpty()) {
                                IconButton(onClick = { company = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        isError = company.isEmpty()
                    )
                }
                Text(
                    "${company.length}/100",
                    fontSize = 12.sp,
                    color = if (company.isEmpty()) Color.Red else Color.Gray,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                )
            }

            // Current Role Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCurrentRole,
                    onCheckedChange = { isCurrentRole = it },
                    colors = CheckboxDefaults.colors(checkedColor = DodgerBlue)
                )
                Spacer(Modifier.width(8.dp))
                Text("Ini adalah peran saya pada saat ini")
            }

            // Start Date
            Column {
                Text(
                    "Tanggal mulai*",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Agustus 2023") },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Date")
                    },
                    isError = startDate.isEmpty()
                )
            }

            // End Date (if not current role)
            if (!isCurrentRole) {
                Column {
                    Text(
                        "Tanggal berakhir*",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Desember 2023") },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Date")
                        },
                        isError = !isCurrentRole && endDate.isEmpty()
                    )
                }
            }

            // Location
            Column {
                Text(
                    "Lokasi",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { if (it.length <= 100) location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Mis: London, Inggris") },
                    supportingText = { Text("${location.length}/100") }
                )
            }

            // Location Type Dropdown
            Column {
                Text(
                    "Jenis lokasi",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                ExposedDropdownMenuBox(
                    expanded = showLocationTypeDropdown,
                    onExpandedChange = { showLocationTypeDropdown = !showLocationTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = locationType.ifEmpty { "Silakan pilih" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationTypeDropdown) }
                    )
                    ExposedDropdownMenu(
                        expanded = showLocationTypeDropdown,
                        onDismissRequest = { showLocationTypeDropdown = false }
                    ) {
                        locationTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    locationType = type
                                    showLocationTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Pilih jenis lokasi (mis: jarak jauh)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Description
            Column {
                Text(
                    "Deskripsi",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 2000) description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    supportingText = { Text("${description.length}/2.000") }
                )
            }

            // Skills Section with Popup
            Column {
                Text(
                    "Keahlian",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tambahkan keahlian yang relevan dengan posisi ini",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = {
                        skillsText = skills.joinToString(", ")
                        showSkillsDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DodgerBlue.copy(alpha = 0.1f),
                        contentColor = DodgerBlue
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(8.dp))
                    Text(if (skills.isEmpty()) "Tambah keahlian" else "Edit keahlian (${skills.size})")
                }

                // Display current skills
                if (skills.isNotEmpty()) {
                    Text(
                        text = skills.joinToString(", "),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Media Section
            Column {
                Text(
                    "Media",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tambahkan media seperti gambar atau situs. Pelajari lebih lanjut tentang jenis file media yang didukung",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = { mediaPickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DodgerBlue.copy(alpha = 0.1f),
                        contentColor = DodgerBlue
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah media")
                }

                if (mediaUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mediaUris) { uri ->
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        mediaUris = mediaUris.filter { it != uri }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Remove",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Delete Experience Button (only when editing)
            if (isEditing) {
                Button(
                    onClick = {
                        currentUser?.uid?.let { userId ->
                            experienceId?.let { expId ->
                                experienceViewModel.deleteExperience(userId, expId) { success ->
                                    if (success) {
                                        snackbarMessage = "Pengalaman berhasil dihapus"
                                        showSnackbar = true
                                        navController.popBackStack()
                                    } else {
                                        snackbarMessage = "Gagal menghapus pengalaman"
                                        showSnackbar = true
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        contentColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Hapus pengalaman")
                }
            }

            // Add some bottom padding to ensure content is not cut off
            Spacer(Modifier.height(32.dp))
        }

        // Skills Dialog
        if (showSkillsDialog) {
            Dialog(onDismissRequest = { showSkillsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Tambah Keahlian",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Pisahkan setiap keahlian dengan koma (,)",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        OutlinedTextField(
                            value = skillsText,
                            onValueChange = { skillsText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Contoh: Kotlin, Android Development, Firebase") },
                            maxLines = 5
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showSkillsDialog = false }
                            ) {
                                Text("Batal")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    skills = skillsText.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                    showSkillsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                            ) {
                                Text("Simpan", color = White)
                            }
                        }
                    }
                }
            }
        }
    }
}