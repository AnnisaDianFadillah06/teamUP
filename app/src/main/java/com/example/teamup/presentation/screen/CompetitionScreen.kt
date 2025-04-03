package com.example.teamup.presentation.screen

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.route.Routes
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.teamup.data.viewmodels.CompetitionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection




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
    navController: NavController,
    viewModel: CompetitionViewModel = viewModel(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var namaLomba by remember { mutableStateOf("") }
    var cabangLomba by remember { mutableStateOf("") }
    var tanggalPelaksanaan by remember { mutableStateOf("") }
    var deskripsiLomba by remember { mutableStateOf("") }
    var jumlahTim by remember { mutableStateOf("0") }

    // Effect untuk navigasi setelah sukses
    LaunchedEffect(key1 = uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Routes.CompetitionList.routes) {
                popUpTo(Routes.AddCompetition.routes) { inclusive = true }
            }
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Kompetisi Baru") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = namaLomba,
                onValueChange = { namaLomba = it },
                label = { Text("Nama Lomba") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = cabangLomba,
                onValueChange = { cabangLomba = it },
                label = { Text("Cabang Lomba") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = tanggalPelaksanaan,
                onValueChange = { tanggalPelaksanaan = it },
                label = { Text("Tanggal Pelaksanaan (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = deskripsiLomba,
                onValueChange = { deskripsiLomba = it },
                label = { Text("Deskripsi Lomba") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = jumlahTim,
                onValueChange = {
                    // Only allow numeric input
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        jumlahTim = it
                    }
                },
                label = { Text("Jumlah Tim") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Placeholder for file upload
            Text(
                text = "Catatan: Fitur unggah berkas pendukung akan tersedia pada versi berikutnya.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    viewModel.addCompetition(
                        namaLomba = namaLomba,
                        cabangLomba = cabangLomba,
                        tanggalPelaksanaan = tanggalPelaksanaan,
                        deskripsiLomba = deskripsiLomba,
                        jumlahTim = jumlahTim.toIntOrNull() ?: 0
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = namaLomba.isNotBlank() && cabangLomba.isNotBlank() &&
                        tanggalPelaksanaan.isNotBlank() && !uiState.isLoading
            ) {
                Text("Buat Kompetisi")
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}