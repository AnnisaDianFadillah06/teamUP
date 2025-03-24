package com.example.teamup.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.route.NavigationItem
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionScreen(
    onAddCompetitionClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competition") },
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
                onClick = onAddCompetitionClick,
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
    onCreateCompetition: (CompetitionModel) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var namaLomba by remember { mutableStateOf("") }
    var cabangLomba by remember { mutableStateOf("") }
    var tanggalPelaksanaan by remember { mutableStateOf("") }
    var deskripsiLomba by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Kompetisi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
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
            Text(
                text = "Mulai membuat lomba yang kompetitif dengan mudah dan cepat. Lakukan langkah nyata dan dapatkan gelar!",
                style = MaterialTheme.typography.bodyMedium
            )

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

            // Date Picker for Tanggal Pelaksanaan
            OutlinedTextField(
                value = tanggalPelaksanaan,
                onValueChange = { tanggalPelaksanaan = it },
                label = { Text("Tanggal Pelaksanaan") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Show Date Picker */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Select Date"
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

            // File Upload Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Upload File Pendukung", modifier = Modifier.weight(1f))
                Button(onClick = { /* TODO: Implement File Upload */ }) {
                    Text("Upload")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val competitionData = CompetitionModel(
                        namaLomba,
                        cabangLomba,
                        tanggalPelaksanaan,
                        deskripsiLomba
                    )
                    onCreateCompetition(competitionData)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buat Kompetisi")
            }
        }
    }
}
