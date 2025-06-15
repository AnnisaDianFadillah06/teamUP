//profilescreen.kt
package com.example.teamup.presentation.screen.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.viewmodels.ProfileViewModel
import com.example.teamup.presentation.components.LogoutDialog
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { profileViewModel.getUserData(it) }
    }

    val userData by profileViewModel.userData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    val handleLogout = {
        FirebaseAuth.getInstance().signOut()
        context.getSharedPreferences("teamup_prefs", android.content.Context.MODE_PRIVATE)
            .edit().clear().apply()
        navController.navigate(Routes.LoginV5.routes) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
        Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            // ✅ FIXED: Match HomeScreenV5 navbar style exactly
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DodgerBlue,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp), // ✅ Same padding as HomeScreenV5
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ Title with same style as "TeamUp" in HomeScreenV5
                    Text(
                        text = "Profile",
                        fontSize = 20.sp, // ✅ Same as "TeamUp"
                        fontWeight = FontWeight.Bold, // ✅ Same as "TeamUp"
                        color = White
                    )

                    // ✅ Settings button (same position as notification icon)
                    IconButton(onClick = {
                        try {
                            Log.d("ProfileNavigation", "Attempting to navigate to: ${Routes.ProfileSettings.routes}")
                            navController.navigate(Routes.ProfileSettings.routes)
                        } catch (e: Exception) {
                            Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                            Toast.makeText(context, "Cannot navigate to settings: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = White,
                            modifier = Modifier.size(24.dp) // ✅ Same size as notification icon
                        )
                    }
                }
            }
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
                userData?.let { profile ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { ProfileHeader(profile) }

                        item {
                            ProfileSection(title = "Education") {
                                if (profile.university.isNotEmpty() && profile.major.isNotEmpty()) {
                                    Text(profile.university, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(profile.major)
                                } else {
                                    Text(
                                        "No education details added yet",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        item {
                            ProfileSection(title = "Skills") {
                                if (profile.skills.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        profile.skills.forEach { skill ->
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(skill) },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = DodgerBlue.copy(alpha = 0.1f)
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "No skills added yet",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        item {
                            ProfileSection(title = "Contact Information") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = "Email", tint = SoftGray2)
                                    Spacer(Modifier.width(12.dp))
                                    Text(profile.email, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (profile.phone.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = "Phone", tint = SoftGray2)
                                        Spacer(Modifier.width(12.dp))
                                        Text(profile.phone, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = { showLogoutDialog = true }) {
                                    Text("Log Out", color = Color.Red)
                                }
                            }
                        }
                    }
                } ?: Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Profile information not available")
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(onDismiss = { showLogoutDialog = false }, onConfirm = handleLogout)
    }
}

@Composable
fun ProfileHeader(profile: UserProfileData) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(White)
            .padding(bottom = 16.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, DodgerBlue, CircleShape)
            ) {
                if (profile.profilePictureUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profile.profilePictureUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(32.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(profile.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("@${profile.username}", fontSize = 16.sp, color = DodgerBlue)
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}