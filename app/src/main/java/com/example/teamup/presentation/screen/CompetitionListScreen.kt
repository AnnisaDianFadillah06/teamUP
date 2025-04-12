//package com.example.teamup.presentation.screen
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.example.teamup.data.model.CompetitionModel
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CompetitionListScreen(
//    navController: NavController,
//    viewModel: CompetitionViewModel = viewModel(
//        factory = CompetitionViewModelFactory(
//            Injection.provideCompetitionRepository()
//        )
//    )
//) {
//    val uiState by viewModel.uiState.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Daftar Kompetisi") }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { navController.navigate(Routes.AddCompetition.routes) }
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Buat Kompetisi")
//            }
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//        ) {
//            if (uiState.isLoading && uiState.competitions.isEmpty()) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else if (uiState.competitions.isEmpty()) {
//                Text(
//                    text = "Belum ada kompetisi. Klik + untuk membuat kompetisi baru.",
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp)
//                )
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(uiState.competitions) { competition ->
//                        CompetitionCardList(competition = competition)
//                    }
//                }
//            }
//
//            uiState.errorMessage?.let { error ->
//                Text(
//                    text = error,
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(16.dp)
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CompetitionCardList(competition: CompetitionModel) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//        ) {
//            Text(
//                text = competition.namaLomba,
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = competition.deskripsiLomba,
//                style = MaterialTheme.typography.bodyMedium,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Cabang: ${competition.cabangLomba}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                Text(
//                    text = "Tanggal: ${competition.tanggalPelaksanaan}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Jumlah Tim: ${competition.jumlahTim}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                // Format the timestamp
//                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//                val formattedDate = try {
//                    dateFormat.format(competition.createdAt.toDate())
//                } catch (e: Exception) {
//                    "Unknown date"
//                }
//
//                Text(
//                    text = formattedDate,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//    }
//}