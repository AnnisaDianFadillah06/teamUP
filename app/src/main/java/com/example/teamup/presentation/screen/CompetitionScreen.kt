package com.example.teamup.presentation.screen

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.route.Routes
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.teamup.data.viewmodels.CompetitionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionScreen(
    navController: NavHostController)
 {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kompetisi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_cancel_24),
                contentDescription = "No Competition",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No competition available yet",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate(Routes.AddCompetition.routes)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Competition")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompetitionScreen(
    viewModel: CompetitionViewModel = viewModel(),
    onCreateCompetition: () -> Unit,
    onBackClick: () -> Unit
) {
    var namaLomba by remember { mutableStateOf("") }
    var cabangLomba by remember { mutableStateOf("") }
    var tanggalPelaksanaan by remember { mutableStateOf("") }
    var deskripsiLomba by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    // Date picker setup
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }.time
            tanggalPelaksanaan = dateFormatter.format(selectedDate)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Kompetisi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = namaLomba,
                onValueChange = { namaLomba = it },
                label = { Text("Nama Lomba") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = cabangLomba,
                onValueChange = { cabangLomba = it },
                label = { Text("Cabang Lomba") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                )
            )

            // Date Picker with Icon
            OutlinedTextField(
                value = tanggalPelaksanaan,
                onValueChange = { tanggalPelaksanaan = it },
                label = { Text("Tanggal Pelaksanaan") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        datePicker.show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "Pilih Tanggal"
                        )
                    }
                }
            )

            OutlinedTextField(
                value = deskripsiLomba,
                onValueChange = { deskripsiLomba = it },
                label = { Text("Deskripsi Lomba") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                singleLine = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                )
            )

            // File Upload Section with Dynamic File Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedFileUri?.let { "File: ${it.lastPathSegment}" }
                        ?: "Upload File Pendukung",
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    filePickerLauncher.launch("*/*")
                }) {
                    Text("Upload")
                }
            }

            Button(
                onClick = {
                    val competitionData = CompetitionModel(
                        namaLomba,
                        cabangLomba,
                        tanggalPelaksanaan,
                        deskripsiLomba
                    )
                    viewModel.addCompetition(competitionData)
                    onCreateCompetition()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = namaLomba.isNotBlank() &&
                        cabangLomba.isNotBlank() &&
                        tanggalPelaksanaan.isNotBlank()
            ) {
                Text("Buat Kompetisi")
            }
        }
    }
}