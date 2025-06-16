//ProfileSettingsScreen.kt
package com.example.teamup.presentation.screen.profile

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.sources.ProfileItem
import com.example.teamup.data.viewmodels.user.ProfileViewModel
import com.example.teamup.presentation.components.LogoutDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.style.TextAlign
import com.example.teamup.data.viewmodels.user.UserViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()

) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Edit mode states
    var isEditMode by remember { mutableStateOf(false) }
    // Logout state
    var isLoggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()



    // Collect all state flows
    val userData by profileViewModel.userData.collectAsState()
    val userEducations by profileViewModel.userEducations.collectAsState()
    val userSkills by profileViewModel.userSkills.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    // Load all user data and observe changes
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            profileViewModel.getUserData(userId)
            profileViewModel.loadUserEducations(userId)
            profileViewModel.loadUserSkills(userId)
        }
    }

    // Re-fetch data when returning from edit mode to ensure fresh data
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            currentUser?.uid?.let { userId ->
                profileViewModel.getUserData(userId)
                profileViewModel.loadUserEducations(userId)
                profileViewModel.loadUserSkills(userId)
            }
        }
    }

    // Image picker states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Profile edit states - only for fields that are actually supported
    var fullNameEdit by remember { mutableStateOf("") }
    var usernameEdit by remember { mutableStateOf("") }
    var phoneEdit by remember { mutableStateOf("") }
    var bioEdit by remember { mutableStateOf("") }
    var locationEdit by remember { mutableStateOf("") }
    var universityEdit by remember { mutableStateOf("") }
    var majorEdit by remember { mutableStateOf("") }
    var skillsEdit by remember { mutableStateOf("") }

    // Initialize edit fields when data changes
    LaunchedEffect(userData, userEducations, userSkills) {
        userData?.let { data ->
            fullNameEdit = data.fullName
            usernameEdit = data.username
            phoneEdit = data.phone
            bioEdit = data.bio
            locationEdit = data.location
        }

        // Get current education data
        val currentEducation = userEducations.firstOrNull { it.isCurrentlyStudying }
            ?: userEducations.maxByOrNull { it.createdAt }
        currentEducation?.let { education ->
            universityEdit = education.school
            majorEdit = education.fieldOfStudy
        }

        // Get skills data
        skillsEdit = userSkills.joinToString(", ") { it.name }
    }

    fun handleLogout(scope: CoroutineScope) {
        if (isLoggingOut) return

        isLoggingOut = true
        Log.d("ProfileSettings", "Starting logout process")

        // Clear session
        SessionManager.clearSession(context) { success ->
            Log.d("ProfileSettings", "Session cleared: $success")

            // Clear additional preferences
            try {
                val prefs = context.getSharedPreferences("teamup_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().commit()

                val userPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                userPrefs.edit().clear().commit()

                Log.d("ProfileSettings", "Additional preferences cleared")
            } catch (e: Exception) {
                Log.e("ProfileSettings", "Error clearing preferences", e)
            }

            // Langsung exit aplikasi
            scope.launch {
                try {
                    Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show()
                    delay(1000) // Beri waktu untuk toast
                    Log.d("ProfileSettings", "Exiting application")
                    exitProcess(0)
                } catch (e: Exception) {
                    Log.e("ProfileSettings", "Exit failed", e)
                    exitProcess(0)
                }
            }
        }
    }


    // Handle profile update
    val saveProfileChanges = {
        currentUser?.uid?.let { userId ->
            val currentUserData = userData ?: return@let

            val updatedUserData = currentUserData.copy(
                fullName = fullNameEdit,
                username = usernameEdit,
                phone = phoneEdit,
                bio = bioEdit,
                location = locationEdit,
                updatedAt = System.currentTimeMillis()
            )

            // Use userViewModel instance instead of static call
            userViewModel.updateUser(updatedUserData, selectedImageUri) { profileSuccess ->
                if (profileSuccess) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    // Save education data jika ada
                    if (universityEdit.isNotBlank() || majorEdit.isNotBlank()) {
                        profileViewModel.saveEducationFromProfile(
                            userId = userId,
                            school = universityEdit,
                            degree = "",
                            fieldOfStudy = majorEdit
                        ) { _ -> }
                    }

                    // Save skills jika ada
                    if (skillsEdit.isNotBlank()) {
                        profileViewModel.saveSkillsFromProfile(userId, skillsEdit) { _ -> }
                    }

                    // Keluar dari edit mode dan refresh data
                    isEditMode = false
                    selectedImageUri = null
                    profileViewModel.getUserData(userId)
                    profileViewModel.loadUserEducations(userId)
                    profileViewModel.loadUserSkills(userId)
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle profile item clicks
    val handleProfileItemClick = { label: String ->
        when (label) {
            "Edit Profile" -> {
                isEditMode = true
            }

            "Notifications" -> {
                navController.navigate("notifications")
            }

//            else -> {
//                Toast.makeText(context, R.string.in_dev, Toast.LENGTH_SHORT).show()
//            }
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
                            selectedImageUri = null // Reset selected image
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
                    if (isEditMode) {
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
                                        when {
                                            selectedImageUri != null -> {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        selectedImageUri
                                                    ),
                                                    contentDescription = "Selected Profile Picture",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            !userData?.profilePictureUrl.isNullOrEmpty() -> {
                                                Image(
                                                    painter = rememberAsyncImagePainter(userData!!.profilePictureUrl),
                                                    contentDescription = "Profile Picture",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            else -> {
                                                Image(
                                                    painter = painterResource(R.drawable.ic_profile),
                                                    contentDescription = "Default Profile Picture",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
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

                        // Form Fields - Only supported fields
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

                                    // Bio
                                    OutlinedTextField(
                                        value = bioEdit,
                                        onValueChange = { bioEdit = it },
                                        label = { Text("Bio") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        maxLines = 4
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Location
                                    OutlinedTextField(
                                        value = locationEdit,
                                        onValueChange = { locationEdit = it },
                                        label = { Text("Location") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
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

                                    // Major/Field of Study
                                    OutlinedTextField(
                                        value = majorEdit,
                                        onValueChange = { majorEdit = it },
                                        label = { Text("Field of Study") },
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
                        item { ProfileBody(onItemClick = handleProfileItemClick) }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoggingOut) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Ruby,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Logging out...",
                                            color = Ruby,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = { showLogoutDialog = true },
                                        enabled = !isLoggingOut
                                    ) {
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
        LogoutDialog(
            onDismiss = {
                if (!isLoggingOut) {
                    showLogoutDialog = false
                }
            },
            onConfirm = {
                showLogoutDialog = false
                handleLogout(scope)
            },
        )
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
        // Profile Picture
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
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full Name
        Text(
            text = userData?.fullName ?: "User",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Username
        if (!userData?.username.isNullOrEmpty()) {
            Text(
                text = "@${userData!!.username}",
                fontSize = 14.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Email
        Text(
            text = userData?.email ?: "email@example.com",
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            color = Color.Gray
        )

        // Bio
        if (!userData?.bio.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userData!!.bio,
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // Location
        if (!userData?.location.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_location), // Ganti dengan icon location yang ada
                    contentDescription = "Location",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userData!!.location,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfileBody(onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = White)) {
            ProfileItem.data.forEach { item ->
                BodyRow(
                    icon = item.Icon,
                    label = item.Label,
                    onClick = { onItemClick(item.Label) }
                )
            }
        }
    }
}

// Helper function to save skills and finish update
private fun saveSkilsAndFinish(
    userId: String,
    skillsEdit: String,
    profileViewModel: ProfileViewModel,
    context: Context,
    selectedImageUri: Uri?,
    onComplete: () -> Unit
) {
    if (skillsEdit.isNotBlank()) {
        profileViewModel.saveSkillsFromProfile(userId, skillsEdit) { skillsSuccess ->
            val message = if (skillsSuccess) {
                "Profile updated successfully"
            } else {
                "Profile updated, but some skills could not be saved"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onComplete()
        }
    } else {
        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}



@Composable
fun BodyRow(icon: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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