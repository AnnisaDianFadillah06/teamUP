//ProfileSettingsScreen.kt
package com.example.teamup.presentation.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.R
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.Ruby
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.common.utils.SessionManager
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.sources.ProfileItem
import com.example.teamup.data.viewmodels.ProfileViewModel
import com.example.teamup.presentation.components.LogoutDialog
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Edit mode states
    var isEditMode by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { profileViewModel.getUserData(it) }
    }

    val userData by profileViewModel.userData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    // Image picker states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Profile edit states
    var fullNameEdit by remember { mutableStateOf("") }
    var usernameEdit by remember { mutableStateOf("") }
    var phoneEdit by remember { mutableStateOf("") }
    var universityEdit by remember { mutableStateOf("") }
    var majorEdit by remember { mutableStateOf("") }
    var skillsEdit by remember { mutableStateOf("") }

    // Initialize edit fields with current data
    LaunchedEffect(userData) {
        userData?.let {
            fullNameEdit = it.fullName
            usernameEdit = it.username
            phoneEdit = it.phone
            universityEdit = it.university
            majorEdit = it.major
            skillsEdit = it.skills.joinToString(", ")
        }
    }

    val handleLogout = {
        FirebaseAuth.getInstance().signOut()
        context.getSharedPreferences("teamup_prefs", android.content.Context.MODE_PRIVATE)
            .edit().clear().apply()
        SessionManager.clearSession(context)
        Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show()
        navController.navigate(Routes.LoginV5.routes) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    // Handle profile update
    val saveProfileChanges = {
        currentUser?.uid?.let { userId ->
            val skillsList = skillsEdit.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (selectedImageUri != null) {
                // If image was changed
                profileViewModel.saveCompleteProfile(
                    userId = userId,
                    university = universityEdit,
                    major = majorEdit,
                    skills = skillsList,
                    imageUri = selectedImageUri!!
                ) { success ->
                    if (success) {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        isEditMode = false
                    } else {
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Basic profile update without changing the image
                profileViewModel.saveUserProfile(
                    userId = userId,
                    fullName = fullNameEdit,
                    username = usernameEdit,
                    email = userData?.email ?: "",
                    phone = phoneEdit,
                    university = universityEdit,
                    major = majorEdit,
                    skills = skillsList,
                    imageUri = null
                ) { success ->
                    if (success) {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        isEditMode = false
                    } else {
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Profile" else "Profile Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditMode) {
                            isEditMode = false
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White,
                    navigationIconContentColor = White
                ),
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = White)
                        }
                    } else {
                        TextButton(
                            onClick = {
                                saveProfileChanges()
                            }
                        ) {
                            Text("Save", color = White)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DodgerBlue
                )
            } else {
                if (isEditMode) {
                    // Edit Profile Mode
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            // Profile Picture Edit
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, DodgerBlue, CircleShape)
                                            .clickable { imagePicker.launch("image/*") }
                                    ) {
                                        val imageToShow = selectedImageUri ?: userData?.profilePictureUrl

                                        if (selectedImageUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(selectedImageUri),
                                                contentDescription = "Selected Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (!userData?.profilePictureUrl.isNullOrEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(userData!!.profilePictureUrl),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(R.drawable.captain_icon),
                                                contentDescription = "Default Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Change Picture",
                                            tint = White,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .background(DodgerBlue, CircleShape)
                                                .padding(4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap to change profile picture",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Form Fields
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Full Name
                                    OutlinedTextField(
                                        value = fullNameEdit,
                                        onValueChange = { fullNameEdit = it },
                                        label = { Text("Full Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Username
                                    OutlinedTextField(
                                        value = usernameEdit,
                                        onValueChange = { usernameEdit = it },
                                        label = { Text("Username") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Phone
                                    OutlinedTextField(
                                        value = phoneEdit,
                                        onValueChange = { phoneEdit = it },
                                        label = { Text("Phone") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // University
                                    OutlinedTextField(
                                        value = universityEdit,
                                        onValueChange = { universityEdit = it },
                                        label = { Text("University") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Major
                                    OutlinedTextField(
                                        value = majorEdit,
                                        onValueChange = { majorEdit = it },
                                        label = { Text("Major") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Skills
                                    OutlinedTextField(
                                        value = skillsEdit,
                                        onValueChange = { skillsEdit = it },
                                        label = { Text("Skills (comma separated)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // View Profile Mode
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { ProfileHeader(userData) }
                        item { ProfileBody() }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = { showLogoutDialog = true }) {
                                    Text(
                                        text = "Log Out",
                                        color = Ruby,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Show error message if any
            errorMessage?.let {
                if (it.isNotEmpty()) {
                    LaunchedEffect(errorMessage) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        profileViewModel.clearError()
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(onDismiss = { showLogoutDialog = false }, onConfirm = { handleLogout() })
    }
}

@Composable
fun ProfileHeader(userData: UserProfileData?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, DodgerBlue, CircleShape)
        ) {
            if (!userData?.profilePictureUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(userData!!.profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.captain_icon),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userData?.fullName ?: "User",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userData?.email ?: "email@example.com",
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal
        )
    }
}

@Composable
fun ProfileBody() {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = White)) {
            ProfileItem.data.forEach { item ->
                BodyRow(icon = item.Icon, label = item.Label)
            }
        }
    }
}

@Composable
fun BodyRow(icon: Int, label: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Toast
                    .makeText(context, R.string.in_dev, Toast.LENGTH_SHORT)
                    .show()
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = SoftGray2,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold)
        }
        Icon(
            painter = painterResource(R.drawable.ic_baseline_keyboard_arrow_right_24),
            contentDescription = "",
            tint = SoftGray2,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSettingsScreenPreview() {
    ProfileSettingsScreen(rememberNavController())
}