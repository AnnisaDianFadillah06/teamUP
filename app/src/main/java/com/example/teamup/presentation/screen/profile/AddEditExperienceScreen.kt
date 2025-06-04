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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
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
fun AddEditExperienceScreen(
    navController: NavController,
    experienceId: String? = null,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val isEditing = experienceId != null

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
    var skills by remember { mutableStateOf("") }
    var mediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showJobTypeDropdown by remember { mutableStateOf(false) }
    var showLocationTypeDropdown by remember { mutableStateOf(false) }

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
        if (isEditing && experienceId != null) {
            // Load experience data by ID from ViewModel
            // This would need to be implemented in ProfileViewModel
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
                    supportingText = { Text("${position.length}/100") }
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
                        }
                    )
                }
                Text(
                    "${company.length}/100",
                    fontSize = 12.sp,
                    color = Color.Gray,
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
                    }
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
                        placeholder = { Text("Saat ini") },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Date")
                        }
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

            // Professional Profile Motto
            Column {
                Text(
                    "Motto profesional profil",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = skills,
                    onValueChange = { if (it.length <= 220) skills = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Siswa di Politeknik Negeri Bandung") },
                    supportingText = {
                        Text("Ditampilkan di bawah nama Anda di bagian atas profil ${skills.length}/220")
                    }
                )
            }

            // Skills Section
            Column {
                Text(
                    "Keahlian",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sebaiknya tambahkan 5 paling banyak yang digunakan di posisi ini. Akan ditampilkan juga di bagian Keahlian.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = { /* Handle add skills */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DodgerBlue.copy(alpha = 0.1f),
                        contentColor = DodgerBlue
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(8.dp))
                    Text("Tambahkan keahlian")
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
                    onClick = { /* Handle delete */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        contentColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hapus pengalaman")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    // Handle save experience
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue),
                modifier = Modifier.fillMaxWidth(),
                enabled = position.isNotEmpty() && company.isNotEmpty() && startDate.isNotEmpty()
            ) {
                Text("Simpan", color = White)
            }
        }
    }
}