//AddEditEducationScreen.kt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEducationScreen(
    navController: NavController,
    isEditing: Boolean = false,
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Form states
    var school by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var fieldOfStudy by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isCurrentlyStudying by remember { mutableStateOf(false) }

    var showDegreeDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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

    // Load existing education data if editing
    LaunchedEffect(isEditing) {
        if (isEditing) {
            val userData = profileViewModel.userData.value
            userData?.education?.let { edu ->
                school = edu.school
                degree = edu.degree
                fieldOfStudy = edu.fieldOfStudy
                startDate = edu.startDate
                endDate = edu.endDate
                isCurrentlyStudying = edu.isCurrentlyStudying
                grade = edu.grade
                activities = edu.activities
                description = edu.description
                // mediaUris: Tidak bisa langsung di-load jika dari URL, perlu decode atau converter
            }
        }

    }

    // Date picker states
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit pendidikan" else "Tambah pendidikan")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White
                )
            )
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
                    supportingText = { Text("${school.length}/100") }
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
                    onExpandedChange = { showDegreeDropdown = !showDegreeDropdown }
                ) {
                    OutlinedTextField(
                        value = degree.ifEmpty { "Silakan pilih" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDegreeDropdown) }
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
                    supportingText = { Text("${fieldOfStudy.length}/100") }
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
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Date")
                        }
                    }
                )
            }

            // Currently Studying Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isCurrentlyStudying,
                    onCheckedChange = {
                        isCurrentlyStudying = it
                        if (it) endDate = ""
                    },
                    colors = CheckboxDefaults.colors(checkedColor = DodgerBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Saya sedang bersekolah di sini",
                    fontSize = 14.sp
                )
            }

            // End Date (only show if not currently studying)
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
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Date")
                            }
                        }
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
                    supportingText = { Text("${grade.length}/50") }
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
                    supportingText = { Text("${activities.length}/500") }
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
                    supportingText = { Text("${description.length}/2.000") }
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
                    onClick = { mediaPickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
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

            // Save Button
            Button(
                onClick = {
                    // Save education data
                    profileViewModel.saveEducationData(
                        school = school,
                        degree = degree,
                        fieldOfStudy = fieldOfStudy,
                        startDate = startDate,
                        endDate = if (isCurrentlyStudying) "Saat ini" else endDate,
                        grade = grade,
                        activities = activities,
                        description = description,
                        mediaUris = mediaUris
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue),
                enabled = school.isNotEmpty() && startDate.isNotEmpty()
            ) {
                Text("Simpan", color = White, fontWeight = FontWeight.Medium)
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