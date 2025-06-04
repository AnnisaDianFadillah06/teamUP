// CompleteProfileScreen.kt
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.data.model.Education
import com.example.teamup.data.viewmodels.AuthViewModel
import com.example.teamup.data.viewmodels.ProfileViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun CompleteProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(
        navController.getBackStackEntry(Routes.Register.routes)
    )
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Get registration data from AuthViewModel
    val registrationData = authViewModel.registrationData

    // Pre-fill form with registration data
    var fullName by remember { mutableStateOf(registrationData?.fullName ?: "") }
    var username by remember { mutableStateOf(registrationData?.username ?: "") }
    var email by remember { mutableStateOf(registrationData?.email ?: "") }
    var phone by remember { mutableStateOf(registrationData?.phone ?: "") }

    // New fields to collect
    var university by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var skillList by remember { mutableStateOf(emptyList<String>()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(2.dp, DodgerBlue, CircleShape)
                        .clickable { pickImageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Photo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display pre-filled user info
                if (!fullName.isBlank()) {
                    Text(fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(email, fontSize = 14.sp, color = Color.Gray)
                    Text("@$username", fontSize = 14.sp, color = DodgerBlue)
                    if (!phone.isBlank()) {
                        Text(phone, fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // University
                OutlinedTextField(
                    value = university,
                    onValueChange = { university = it },
                    label = { Text("University") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Major
                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("Major") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add Skill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = skill,
                        onValueChange = { skill = it },
                        label = { Text("Add Skill") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (skill.isNotBlank() && !skillList.contains(skill)) {
                            skillList = skillList + skill
                            skill = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Skill")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Skills Chips
                if (skillList.isNotEmpty()) {
                    Text("Your Skills", fontSize = 16.sp, fontWeight = FontWeight.Medium)

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skillList.forEach { skillItem ->
                            AssistChip(
                                onClick = { },
                                label = { Text(skillItem) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        skillList = skillList.filterNot { it == skillItem }
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = DodgerBlue.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (university.isBlank() || major.isBlank() || skillList.isEmpty() || imageUri == null) {
                            Toast.makeText(context, "Please complete all fields and add a profile picture", Toast.LENGTH_SHORT).show()
                        } else {
                            currentUser?.uid?.let { uid ->
                                if (registrationData != null) {
                                    // If we have registration data, save complete profile with all data
                                    profileViewModel.saveUserProfile(
                                        userId = uid,
                                        fullName = fullName,
                                        username = username,
                                        email = email,
                                        phone = phone,
                                        skills = skillList,
                                        imageUri = imageUri!!
                                    ){ success ->
                                        if (success) {
                                            // After saving basic profile, save education data
                                            profileViewModel.saveCompleteProfile(
                                                userId = uid,
                                                school = university,
                                                degree = "", // You can add a degree field if needed
                                                fieldOfStudy = major,
                                                skills = skillList,
                                                imageUri = imageUri!!
                                            ) { educationSuccess ->
                                                if (educationSuccess) {
                                                    Toast.makeText(context, "Profile completed successfully!", Toast.LENGTH_SHORT).show()
                                                    navController.navigate(Routes.Dashboard.routes) {
                                                        popUpTo(Routes.CompleteProfile.routes) { inclusive = true }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Failed to save education data. Please try again.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    // If no registration data (e.g. coming from edit profile), just update the profile
                                    profileViewModel.saveCompleteProfile(
                                        userId = uid,
                                        school = university,
                                        degree = "", // You can add a degree field if needed
                                        fieldOfStudy = major,
                                        skills = skillList,
                                        imageUri = imageUri!!
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                            navController.navigate(Routes.Dashboard.routes) {
                                                popUpTo(Routes.CompleteProfile.routes) { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Complete Profile")
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DodgerBlue)
                }
            }

            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }
        }
    }
}