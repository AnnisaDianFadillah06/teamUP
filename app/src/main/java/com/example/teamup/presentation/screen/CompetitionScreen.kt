package com.example.teamup.presentation.screen

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.teamup.R
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.BottomNavigationBar
import com.example.teamup.route.NavigationItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.dp
import androidx.navigation.NavOptionsBuilder
import com.example.teamup.common.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionScreen(
    navController: NavHostController,
    viewModel: CompetitionViewModel = viewModel(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    // Handle hardware back button
    BackHandler(enabled = showAddForm) {
        showAddForm = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showAddForm) "Buat Kompetisi Baru" else "Kompetisi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (showAddForm) {
                        IconButton(onClick = { showAddForm = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB when not in add form mode
            if (!showAddForm) {
                FloatingActionButton(onClick = { showAddForm = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Competition")
                }
            }
        },
        bottomBar = {
            // Custom Bottom Navigation that handles Competition tab click specially when in form view
            if (showAddForm) {
                // Custom BottomNavigationBar for when we're in the form
                CustomBottomNavigationBar(
                    navController = navController,
                    onCompetitionClick = { showAddForm = false }
                )
            } else {
                // Regular BottomNavigationBar for the main screen
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        if (showAddForm) {
            AddCompetitionForm(
                viewModel = viewModel,
                onSuccess = { showAddForm = false },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CompetitionListContent(
                uiState = uiState,
                onAddClick = { showAddForm = true },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    onCompetitionClick: () -> Unit
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Sail,
        NavigationItem.Wishlist,
        NavigationItem.Competition,
        NavigationItem.Profile,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = White, contentColor = DodgerBlue) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item == NavigationItem.Competition) {
                        // When in form and competition is clicked, just go back to competition screen
                        onCompetitionClick()
                    } else {
                        // For other tabs, navigate normally
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier
                            .width(18.dp)
                            .height(20.dp),
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = DodgerBlueShade,
                    selectedIconColor = White,
                    unselectedIconColor = IceBlue
                )
            )
        }
    }
}

// Rest of the code remains the same

@Composable
fun CompetitionListContent(
    uiState: CompetitionViewModel.CompetitionUiState,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading && uiState.competitions.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.competitions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    text = "Belum ada kompetisi tersedia",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Buat Kompetisi")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.competitions) { competition ->
                    // Use fully qualified name to avoid conflict with CompetitionListScreen.kt
                    com.example.teamup.presentation.screen.CompetitionCardScreen(competition = competition)
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun CompetitionCardScreen(competition: CompetitionModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = competition.namaLomba,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = competition.deskripsiLomba,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cabang: ${competition.cabangLomba}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Tanggal: ${competition.tanggalPelaksanaan}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jumlah Tim: ${competition.jumlahTim}",
                    style = MaterialTheme.typography.bodySmall
                )

                // Format the timestamp
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                val formattedDate = try {
                    dateFormat.format(competition.createdAt.toDate())
                } catch (e: Exception) {
                    "Unknown date"
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Display image if available
            if (competition.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                // You can use an Image composable with Coil or Glide here
                Text(
                    text = "Ada gambar tersedia",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AddCompetitionForm(
    viewModel: CompetitionViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var namaLomba by remember { mutableStateOf("") }
    var cabangLomba by remember { mutableStateOf("") }
    var tanggalPelaksanaan by remember { mutableStateOf("") }
    var deskripsiLomba by remember { mutableStateOf("") }
    var jumlahTim by remember { mutableStateOf("0") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Effect untuk navigasi setelah sukses
    LaunchedEffect(key1 = uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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

        // Date picker field
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

        // Image picker
        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedImageUri != null) "Ganti Gambar" else "Pilih Gambar")
        }

        selectedImageUri?.let { uri ->
            Text(
                text = "Gambar dipilih: ${uri.lastPathSegment ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                if (selectedImageUri != null) {
                    // Upload image first then add competition
                    uploadImageAndAddCompetition(
                        selectedImageUri!!,
                        namaLomba,
                        cabangLomba,
                        tanggalPelaksanaan,
                        deskripsiLomba,
                        jumlahTim.toIntOrNull() ?: 0,
                        viewModel
                    )
                } else {
                    // Add competition without image
                    viewModel.addCompetition(
                        namaLomba = namaLomba,
                        cabangLomba = cabangLomba,
                        tanggalPelaksanaan = tanggalPelaksanaan,
                        deskripsiLomba = deskripsiLomba,
                        jumlahTim = jumlahTim.toIntOrNull() ?: 0
                    )
                }
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

private fun uploadImageAndAddCompetition(
    imageUri: Uri,
    namaLomba: String,
    cabangLomba: String,
    tanggalPelaksanaan: String,
    deskripsiLomba: String,
    jumlahTim: Int,
    viewModel: CompetitionViewModel
) {
    val storageRef = Firebase.storage.reference
    val imageRef = storageRef.child("competitions/${UUID.randomUUID()}.jpg")

    imageRef.putFile(imageUri)
        .continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()
                viewModel.addCompetition(
                    namaLomba = namaLomba,
                    cabangLomba = cabangLomba,
                    tanggalPelaksanaan = tanggalPelaksanaan,
                    deskripsiLomba = deskripsiLomba,
                    imageUrl = downloadUrl,
                    jumlahTim = jumlahTim
                )
            }
        }
}