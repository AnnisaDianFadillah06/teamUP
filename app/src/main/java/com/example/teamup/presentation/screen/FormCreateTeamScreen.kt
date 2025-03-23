package com.example.teamup.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.teamup.common.theme.*
import com.example.teamup.data.model.TeamModel
import com.example.teamup.data.viewmodels.TeamViewModel
import com.example.teamup.data.viewmodels.TeamViewModelFactory
import com.example.teamup.di.Injection
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormCreateTeamScreen(
    navController: NavController,
    viewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(
            Injection.provideTeamRepository()
        )
    )
){
    var teamName by remember { mutableStateOf("") }
    var teamDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedBranch by remember { mutableStateOf("") }
    var showBranchDropdown by remember { mutableStateOf(false) }
    var maxMembers by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(true) }

    // For image selection
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Handle selected image
        selectedImageUri = uri
    }


    // List options for dropdowns
    val categoryOptions = listOf("Teknologi", "Sains", "Desain", "Bisnis", "Olahraga", "Edukasi", "Lainnya")
    val branchOptions = listOf("Web Development", "Mobile App", "UI/UX Design", "Data Science", "IoT", "AI", "Robotik", "Game Development")

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buat Tim Lomba",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
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
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header text
            Text(
                text = "Buat Tim Lomba",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description text
            Text(
                text = "Mulai membuat tim lomba yang kompetitif dengan mudah dan cepat. Lakukan langkah nyata demi masa depan yang cerah",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SoftGray2
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
// Team Avatar Selection
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Display selected image
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .build()
                        ),
                        contentDescription = "Team Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display placeholder icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Select Image",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add Photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Team Profile Picture",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category dropdown
            Text(
                text = "Kategori Lomba",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showCategoryDropdown = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory.isEmpty()) "Kategori Lomba" else selectedCategory,
                            color = if (selectedCategory.isEmpty()) SoftGray2 else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = SoftGray2
                        )
                    }
                }

                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                selectedCategory = option
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Branch dropdown
            Text(
                text = "Cabang Lomba",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showBranchDropdown = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedBranch.isEmpty()) "Cabang Lomba" else selectedBranch,
                            color = if (selectedBranch.isEmpty()) SoftGray2 else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = SoftGray2
                        )
                    }
                }

                DropdownMenu(
                    expanded = showBranchDropdown,
                    onDismissRequest = { showBranchDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    branchOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                selectedBranch = option
                                showBranchDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team Name
            Text(
                text = "Nama Tim",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ),
                placeholder = { Text("Masukkan nama tim", color = SoftGray2) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DodgerBlue,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max members
            Text(
                text = "Jumlah Maksimal Anggota",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = maxMembers,
                onValueChange = { maxMembers = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ),
                placeholder = { Text("Masukkan jumlah maksimal anggota", color = SoftGray2) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DodgerBlue,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Team description
            Text(
                text = "Deskripsi Tim",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = teamDescription,
                onValueChange = { teamDescription = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ),
                placeholder = { Text("Deskripsi tentang tim anda", color = SoftGray2) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DodgerBlue,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Team privacy options
            Text(
                text = "Privasi Tim",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Open option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isPrivate,
                    onClick = { isPrivate = false },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = DodgerBlue
                    )
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Anggota Bisa Join Tanpa Izin Admin",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SoftGray2
                        )
                    )
                }
            }

            // Private option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isPrivate,
                    onClick = { isPrivate = true },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = DodgerBlue
                    )
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Private",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Anggota Harus Mendapat Izin Admin",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SoftGray2
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = {
                    val avatarResId = selectedImageUri?.toString() ?: "default_avatar"
                    viewModel.addTeam(teamName, teamDescription, "$selectedCategory - $selectedBranch", avatarResId)


                    // Navigate back or to team detail on success
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DodgerBlue
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = teamName.isNotEmpty() && selectedCategory.isNotEmpty() && selectedBranch.isNotEmpty()
            ) {
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}