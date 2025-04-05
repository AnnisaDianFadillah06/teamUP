package com.example.teamup.presentation.screen

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.DodgerBlueShade
import com.example.teamup.common.theme.IceBlue
import com.example.teamup.common.theme.White
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
                    painter = painterResource(id = R.drawable.task2),
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
                    CompetitionCardScreen(competition = competition)
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
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = competition.imageUrl)
                            .error(R.drawable.ic_baseline_cancel_24)
                            .build()
                    ),
                    contentDescription = "Competition Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Display file info if available
            if (competition.fileUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilePresent,
                        contentDescription = "File Attached",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "File terlampir",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // State for Alert Dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // State for loading progress
    var uploadProgress by remember { mutableFloatStateOf(0f) }

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

    // Create image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Verify we can read the image
                context.contentResolver.openInputStream(it)?.use { stream ->
                    // Successfully opened the stream, which means we have permission
                    selectedImageUri = it
                }
            } catch (e: Exception) {
                // Handle permission error
                errorMessage = "Tidak dapat mengakses gambar: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    // Create file picker launcher
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Verify we can read the file
                context.contentResolver.openInputStream(it)?.use { stream ->
                    // Successfully opened the stream, which means we have permission
                    selectedFileUri = it

                    // Extract file name
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
                // Handle permission error
                errorMessage = "Tidak dapat mengakses file: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    // Effect untuk navigasi setelah sukses
    LaunchedEffect(key1 = uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    // Effect untuk menampilkan error message di Alert Dialog
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            errorMessage = error
            showErrorDialog = true
            // Reset error message after displaying
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

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            item {
                OutlinedTextField(
                    value = cabangLomba,
                    onValueChange = { cabangLomba = it },
                    label = { Text("Cabang Lomba") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
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

            item {
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
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Media dan Dokumen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                // Image upload section
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
                            // The image preview with an option to change
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

                                // Add a button to remove/change the image
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

            item {
                // File upload section
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
                            // File selected layout with remove option
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

                                    // Add a button to remove the file
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isUploading = true
                        uploadProgress = 0f
                        uploadCompetitionWithMedia(
                            context = context,
                            imageUri = selectedImageUri,
                            fileUri = selectedFileUri,
                            namaLomba = namaLomba,
                            cabangLomba = cabangLomba,
                            tanggalPelaksanaan = tanggalPelaksanaan,
                            deskripsiLomba = deskripsiLomba,
                            jumlahTim = jumlahTim.toIntOrNull() ?: 0,
                            viewModel = viewModel,
                            onComplete = { isUploading = false }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = namaLomba.isNotBlank() && cabangLomba.isNotBlank() &&
                            tanggalPelaksanaan.isNotBlank() && !uiState.isLoading && !isUploading
                ) {
                    Text("Buat Kompetisi")
                }
            }

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
        }
    }
}

private fun uploadCompetitionWithMedia(
    context: Context,
    imageUri: Uri?,
    fileUri: Uri?,
    namaLomba: String,
    cabangLomba: String,
    tanggalPelaksanaan: String,
    deskripsiLomba: String,
    jumlahTim: Int,
    viewModel: CompetitionViewModel,
    onComplete: () -> Unit
) {
    val storageRef = Firebase.storage.reference
    var imageUrl = ""
    var fileUrl = ""
    var imageUploaded = false
    var fileUploaded = false
    var uploadErrors = 0

    // Function to check if both uploads are complete and add competition
    fun checkAndAddCompetition() {
        // If there are upload errors, don't add the competition
        if (uploadErrors > 0) {
            onComplete()
            return
        }

        val shouldAddWithImage = imageUri != null
        val shouldAddWithFile = fileUri != null

        // If both required files are uploaded or not needed, add the competition
        if ((shouldAddWithImage && imageUploaded || !shouldAddWithImage) &&
            (shouldAddWithFile && fileUploaded || !shouldAddWithFile)) {
            viewModel.addCompetition(
                namaLomba = namaLomba,
                cabangLomba = cabangLomba,
                tanggalPelaksanaan = tanggalPelaksanaan,
                deskripsiLomba = deskripsiLomba,
                imageUrl = imageUrl,
                fileUrl = fileUrl,
                jumlahTim = jumlahTim
            )
            onComplete()
        }
    }

    // If no files to upload, add competition directly
    if (imageUri == null && fileUri == null) {
        viewModel.addCompetition(
            namaLomba = namaLomba,
            cabangLomba = cabangLomba,
            tanggalPelaksanaan = tanggalPelaksanaan,
            deskripsiLomba = deskripsiLomba,
            jumlahTim = jumlahTim
        )
        onComplete()
        return
    }

    // Upload image if available
    if (imageUri != null) {
        try {
            // Get content resolver to verify access
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri)

            if (mimeType?.startsWith("image/") == true) {
                // Create a temporary file to handle potential content URI issues
                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream?.use { input ->
                    val imageRef = storageRef.child("competitions/images/${UUID.randomUUID()}.jpg")

                    // Start the upload task
                    imageRef.putStream(input)
                        .addOnFailureListener { exception ->
                            uploadErrors++
                            viewModel.setError("Gagal mengupload gambar: ${exception.message}")
                            onComplete()
                        }
                        .addOnSuccessListener {
                            // Get download URL after successful upload
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                imageUrl = uri.toString()
                                imageUploaded = true
                                checkAndAddCompetition()
                            }.addOnFailureListener { exception ->
                                uploadErrors++
                                viewModel.setError("Gagal mendapatkan URL gambar: ${exception.message}")
                                onComplete()
                            }
                        }
                }
            } else {
                uploadErrors++
                viewModel.setError("Format file gambar tidak didukung")
                onComplete()
            }
        } catch (e: Exception) {
            uploadErrors++
            viewModel.setError("Error akses file gambar: ${e.message}")
            onComplete()
        }
    } else {
        imageUploaded = true
    }

    // Upload file if available
    if (fileUri != null) {
        try {
            // Get content resolver
            val contentResolver = context.contentResolver

            // Get file name and extension
            var fileName = "document"
            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }

            // Handle the upload with input stream
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val fileRef = storageRef.child("competitions/files/${UUID.randomUUID()}_$fileName")

                // Start upload task
                fileRef.putStream(inputStream)
                    .addOnFailureListener { exception ->
                        uploadErrors++
                        viewModel.setError("Gagal mengupload file: ${exception.message}")
                        onComplete()
                    }
                    .addOnSuccessListener {
                        // Get download URL after successful upload
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            fileUrl = uri.toString()
                            fileUploaded = true
                            checkAndAddCompetition()
                        }.addOnFailureListener { exception ->
                            uploadErrors++
                            viewModel.setError("Gagal mendapatkan URL file: ${exception.message}")
                            onComplete()
                        }
                    }
            } ?: run {
                uploadErrors++
                viewModel.setError("Tidak dapat mengakses file")
                onComplete()
            }
        } catch (e: Exception) {
            uploadErrors++
            viewModel.setError("Error akses dokumen: ${e.message}")
            onComplete()
        }
    } else {
        fileUploaded = true
    }
}