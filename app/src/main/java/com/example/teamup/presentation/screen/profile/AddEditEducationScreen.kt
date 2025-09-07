//AddEditEducationScreen.kt
package com.example.teamup.presentation.screen.profile

import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.user.EducationViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEducationScreen(
    navController: NavController,
    educationId: String? = null, // PERBAIKAN: nullable parameter
    educationViewModel: EducationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditMode = !educationId.isNullOrEmpty() // PERBAIKAN: check nullable

    // Form states
    var school by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var fieldOfStudy by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // TAMBAHAN BARU:
    var currentSemester by remember { mutableStateOf("") }
    var currentLevel by remember { mutableStateOf("") }
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isCurrentlyStudying by remember { mutableStateOf(false) }

    var showDegreeDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val degrees = listOf(
        "Associate's degree",
        "Bachelor's degree",
        "Master's degree",
        "Doctoral degree",
        "Diploma",
        "Certificate",
        "SMA/SMK",
        "D3",
        "S1",
        "S2",
        "S3"
    )

    // Media picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        mediaUris = mediaUris + uris
    }

    // Observe ViewModel states
    val isLoading by educationViewModel.isLoading.collectAsState()
    val vmErrorMessage by educationViewModel.errorMessage.collectAsState()
    val currentEducation by educationViewModel.currentEducation.collectAsState()

    // Handle error messages
    LaunchedEffect(vmErrorMessage) {
        vmErrorMessage?.let {
            errorMessage = it
            showErrorMessage = true
            educationViewModel.clearError()
        }
    }

    // Load existing education data if editing - PERBAIKAN
    LaunchedEffect(educationId) {
        if (isEditMode && educationId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                educationViewModel.loadEducation(userId, educationId)
            } else {
                Log.e("LoadEducation", "User ID is null. Cannot load education.")
                errorMessage = "Gagal memuat data: User tidak ditemukan"
                showErrorMessage = true
            }
        } else {
            // Clear jika mode add
            educationViewModel.clearCurrentEducation()
        }
    }

    // Update form fields when currentEducation changes
    LaunchedEffect(currentEducation) {
        currentEducation?.let { edu ->
            school = edu.school
            degree = edu.degree
            fieldOfStudy = edu.fieldOfStudy
            startDate = edu.startDate
            endDate = edu.endDate
            isCurrentlyStudying = edu.isCurrentlyStudying
            grade = edu.grade
            activities = edu.activities
            description = edu.description
            // TAMBAHAN BARU:
            currentSemester = edu.currentSemester
            currentLevel = edu.currentLevel
        }
    }

    // Handle success navigation
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    // Check if form is valid for save button
    val isFormValid = remember(school, startDate, endDate, isCurrentlyStudying) {
        school.isNotBlank() &&
                startDate.isNotBlank() &&
                (isCurrentlyStudying || endDate.isNotBlank())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Pendidikan" else "Tambah Pendidikan") // PERBAIKAN: Konsisten dengan isEditMode
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isFormValid && !isLoading) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid

                                if (userId != null) {
                                    educationViewModel.saveEducation(
                                        userId = userId,
                                        school = school.trim(),
                                        degree = degree.trim(),
                                        fieldOfStudy = fieldOfStudy.trim(),
                                        startDate = startDate.trim(),
                                        endDate = if (isCurrentlyStudying) "" else endDate.trim(),
                                        grade = grade.trim(),
                                        activities = activities.trim(),
                                        description = description.trim(),
                                        mediaUris = mediaUris,
                                        isCurrentlyStudying = isCurrentlyStudying,
                                        currentSemester = currentSemester.trim(),    // TAMBAH
                                        currentLevel = currentLevel.trim(),          // TAMBAH
                                        educationId = if (isEditMode) educationId else null
                                    ) { success ->
                                        if (success) {
                                            showSuccessMessage = true
                                        } else {
                                            showErrorMessage = true
                                        }
                                    }
                                } else {
                                    errorMessage = "User tidak ditemukan. Silakan login ulang."
                                    showErrorMessage = true
                                    Log.e("SaveEducation", "User ID is null. Cannot save education.")
                                }
                            }
                        },
                        enabled = isFormValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Simpan", color = if (isFormValid) White else White.copy(alpha = 0.6f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White2)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // School Field
                Column {
                    Text(
                        "Sekolah*",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = school,
                        onValueChange = { if (it.length <= 100) school = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mis: Politeknik Negeri Bandung") },
                        trailingIcon = {
                            if (school.isNotEmpty()) {
                                IconButton(onClick = { school = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = { Text("${school.length}/100") },
                        enabled = !isLoading,
                        isError = school.isBlank() && school.isNotEmpty()
                    )
                }

                // Degree Dropdown
                Column {
                    Text(
                        "Gelar",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showDegreeDropdown,
                        onExpandedChange = {
                            if (!isLoading) {
                                showDegreeDropdown = !showDegreeDropdown
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = degree.ifEmpty { "Silakan pilih" },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDegreeDropdown)
                            },
                            enabled = !isLoading
                        )
                        ExposedDropdownMenu(
                            expanded = showDegreeDropdown,
                            onDismissRequest = { showDegreeDropdown = false }
                        ) {
                            degrees.forEach { degreeOption ->
                                DropdownMenuItem(
                                    text = { Text(degreeOption) },
                                    onClick = {
                                        degree = degreeOption
                                        showDegreeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Field of Study
                Column {
                    Text(
                        "Bidang studi",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fieldOfStudy,
                        onValueChange = { if (it.length <= 100) fieldOfStudy = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mis: Teknik Informatika") },
                        trailingIcon = {
                            if (fieldOfStudy.isNotEmpty()) {
                                IconButton(onClick = { fieldOfStudy = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = { Text("${fieldOfStudy.length}/100") },
                        enabled = !isLoading
                    )
                }

                // Start Date
                Column {
                    Text(
                        "Tanggal mulai*",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("2023") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (!isLoading) {
                                        showStartDatePicker = true
                                    }
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Date")
                            }
                        },
                        enabled = !isLoading,
                        isError = startDate.isBlank() && startDate.isNotEmpty()
                    )
                }

                // Currently Studying Checkbox
                // Currently Studying Checkbox (tetap sama)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCurrentlyStudying,
                        onCheckedChange = {
                            if (!isLoading) {
                                isCurrentlyStudying = it
                                if (it) endDate = ""
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = DodgerBlue),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Saya sedang bersekolah di sini",
                        fontSize = 14.sp
                    )
                }

// PERBAIKAN: Tampilkan field tingkat dan semester HANYA jika sedang bersekolah
                if (isCurrentlyStudying) {
                    // Tingkat/Kelas Field
                    Column {
                        Text(
                            "Tingkat/Kelas (opsional)",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentLevel,
                            onValueChange = { if (it.length <= 50) currentLevel = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Mis: Tingkat 3, Kelas 12") },
                            enabled = !isLoading
                        )
                    }

                    // Semester Field
                    Column {
                        Text(
                            "Semester (opsional)",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentSemester,
                            onValueChange = { if (it.length <= 50) currentSemester = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Mis: Semester 5") },
                            enabled = !isLoading
                        )
                    }
                }

// End Date (hanya tampil jika TIDAK sedang bersekolah)
                if (!isCurrentlyStudying) {
                    Column {
                        Text(
                            "Tanggal berakhir*",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("2026") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isLoading) {
                                            showEndDatePicker = true
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                                }
                            },
                            enabled = !isLoading,
                            isError = !isCurrentlyStudying && endDate.isBlank() && endDate.isNotEmpty()
                        )
                    }
                }

                // Grade
                Column {
                    Text(
                        "Nilai",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { if (it.length <= 50) grade = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mis: 3.8 GPA") },
                        trailingIcon = {
                            if (grade.isNotEmpty()) {
                                IconButton(onClick = { grade = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = { Text("${grade.length}/50") },
                        enabled = !isLoading
                    )
                }

                // Activities and Societies
                Column {
                    Text(
                        "Aktivitas dan komunitas",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = activities,
                        onValueChange = { if (it.length <= 500) activities = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mis: Ketua HIMAKOM, Member IEEE") },
                        minLines = 2,
                        trailingIcon = {
                            if (activities.isNotEmpty()) {
                                IconButton(onClick = { activities = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = { Text("${activities.length}/500") },
                        enabled = !isLoading
                    )
                }

                // Description
                Column {
                    Text(
                        "Deskripsi",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 2000) description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ceritakan pengalaman pendidikan Anda...") },
                        minLines = 3,
                        trailingIcon = {
                            if (description.isNotEmpty()) {
                                IconButton(onClick = { description = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        supportingText = { Text("${description.length}/2.000") },
                        enabled = !isLoading
                    )
                }

                // Media Section
                Column {
                    Text(
                        "Media",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tambahkan media seperti gambar atau situs. Pelajari lebih lanjut tentang jenis file media yang didukung",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Media Button
                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                mediaPickerLauncher.launch("*/*")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Media")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah media")
                    }

                    // Display selected media
                    if (mediaUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            mediaUris = mediaUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.7f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Success/Error Messages
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { showSuccessMessage = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Data pendidikan berhasil disimpan!")
                }
            }

            if (showErrorMessage) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { showErrorMessage = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(errorMessage.ifEmpty { "Terjadi kesalahan saat menyimpan data" })
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(millis))
                    onDateSelected(date)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}