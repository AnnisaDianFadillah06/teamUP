package com.example.teamup.presentation.components.competition

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.utils.uploadCompetitionWithMedia
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.viewmodels.CompetitionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompetitionForm(
    viewModel: CompetitionViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var namaLomba by remember { mutableStateOf("") }
    var newCabangLomba by remember { mutableStateOf("") }
    val cabangLombaList = remember { mutableStateListOf<String>() }
    var tanggalPelaksanaan by remember { mutableStateOf("") }
    var deskripsiLomba by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Status fields
    var visibilityStatusExpanded by remember { mutableStateOf(false) }
    var activityStatusExpanded by remember { mutableStateOf(false) }
    var selectedVisibilityStatus by remember { mutableStateOf(CompetitionVisibilityStatus.PUBLISHED.value) }
    var selectedActivityStatus by remember { mutableStateOf(CompetitionActivityStatus.ACTIVE.value) }

    // New deadline date fields
    var tanggalTutupPendaftaran by remember { mutableStateOf("") }
    var autoCloseEnabled by remember { mutableStateOf(false) }

    // State for Alert Dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val deadlineCalendar = Calendar.getInstance()

    // Date pickers
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tanggalPelaksanaan = format.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val deadlineDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            deadlineCalendar.set(year, month, dayOfMonth)
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    deadlineCalendar.set(Calendar.MINUTE, minute)
                    deadlineCalendar.set(Calendar.SECOND, 59)
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    tanggalTutupPendaftaran = format.format(deadlineCalendar.time)
                },
                deadlineCalendar.get(Calendar.HOUR_OF_DAY),
                deadlineCalendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        deadlineCalendar.get(Calendar.YEAR),
        deadlineCalendar.get(Calendar.MONTH),
        deadlineCalendar.get(Calendar.DAY_OF_MONTH)
    )

    // File pickers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    selectedImageUri = it
                }
            } catch (e: Exception) {
                errorMessage = "Tidak dapat mengakses gambar: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    selectedFileUri = it
                    context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) {
                                selectedFileName = cursor.getString(nameIndex)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Tidak dapat mengakses file: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    // Effects
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            errorMessage = error
            showErrorDialog = true
            viewModel.clearError()
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Pemberitahuan") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // ✅ FIXED: Remove Scaffold wrapper and use direct LazyColumn with proper bottom spacing
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = namaLomba,
                onValueChange = { namaLomba = it },
                label = { Text("Nama Lomba") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Multiple cabang lomba section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cabang-cabang Lomba",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCabangLomba,
                        onValueChange = { newCabangLomba = it },
                        label = { Text("Cabang Lomba Baru") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (newCabangLomba.isNotBlank()) {
                                cabangLombaList.add(newCabangLomba)
                                newCabangLomba = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Cabang Lomba",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (cabangLombaList.isNotEmpty()) {
                    Text(
                        text = "Cabang Lomba yang ditambahkan:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cabangLombaList.forEachIndexed { index, cabang ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cabang,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { cabangLombaList.removeAt(index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Cabang Lomba",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = tanggalPelaksanaan,
                onValueChange = { tanggalPelaksanaan = it },
                label = { Text("Tanggal Pelaksanaan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                }
            )
        }

        item {
            OutlinedTextField(
                value = deskripsiLomba,
                onValueChange = { deskripsiLomba = it },
                label = { Text("Deskripsi Lomba") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        // Status selection
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Status Kompetisi",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Visibility Status dropdown
        item {
            ExposedDropdownMenuBox(
                expanded = visibilityStatusExpanded,
                onExpandedChange = { visibilityStatusExpanded = !visibilityStatusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedVisibilityStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Visibilitas") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = visibilityStatusExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = visibilityStatusExpanded,
                    onDismissRequest = { visibilityStatusExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CompetitionVisibilityStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.value) },
                            onClick = {
                                selectedVisibilityStatus = status.value
                                visibilityStatusExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Activity Status dropdown
        item {
            ExposedDropdownMenuBox(
                expanded = activityStatusExpanded,
                onExpandedChange = { activityStatusExpanded = !activityStatusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedActivityStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Aktivitas") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityStatusExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = activityStatusExpanded,
                    onDismissRequest = { activityStatusExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CompetitionActivityStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.value) },
                            onClick = {
                                selectedActivityStatus = status.value
                                activityStatusExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Registration deadline section
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Batas Pendaftaran",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = if (tanggalTutupPendaftaran.isNotEmpty()) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = inputFormat.parse(tanggalTutupPendaftaran)
                    outputFormat.format(date!!)
                } else "",
                onValueChange = { },
                label = { Text("Tanggal & Waktu Tutup Pendaftaran") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { deadlineDatePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Deadline Date"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = autoCloseEnabled,
                    onCheckedChange = { autoCloseEnabled = it }
                )
                Text(
                    text = "Otomatis ubah status menjadi Inactive saat melewati batas pendaftaran",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Media section
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Media dan Dokumen",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Image upload
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Foto Lomba",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedImageUri)
                                    .error(R.drawable.ic_baseline_cancel_24)
                                    .build(),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Upload Image",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Klik untuk upload gambar",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // File upload
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dokumen Lomba",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filePicker.launch("application/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedFileUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilePresent,
                                    contentDescription = "File",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedFileName.ifEmpty { "File terpilih" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = {
                                    selectedFileUri = null
                                    selectedFileName = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove File",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilePresent,
                                contentDescription = "Upload File",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Klik untuk upload dokumen",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // ✅ CRITICAL: Submit button with proper styling
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isUploading = true
                    uploadCompetitionWithMedia(
                        context = context,
                        imageUri = selectedImageUri,
                        fileUri = selectedFileUri,
                        namaLomba = namaLomba,
                        cabangLomba = cabangLombaList.toList(),
                        tanggalPelaksanaan = tanggalPelaksanaan,
                        deskripsiLomba = deskripsiLomba,
                        visibilityStatus = selectedVisibilityStatus,
                        activityStatus = selectedActivityStatus,
                        tanggalTutupPendaftaran = tanggalTutupPendaftaran.takeIf { it.isNotEmpty() },
                        autoCloseEnabled = autoCloseEnabled,
                        viewModel = viewModel,
                        onComplete = { isUploading = false }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = namaLomba.isNotBlank() && cabangLombaList.isNotEmpty() &&
                        tanggalPelaksanaan.isNotBlank() && !uiState.isLoading && !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading || isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Buat Kompetisi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Loading indicator
        item {
            if (uiState.isLoading || isUploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mengupload...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ✅ CRITICAL: Bottom spacer to avoid BottomNavigation overlap
        item {
            Spacer(modifier = Modifier.height(40.dp)) // Extra space for BottomNav clearance
        }
    }
}