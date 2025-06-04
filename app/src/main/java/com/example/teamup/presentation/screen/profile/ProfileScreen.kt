//profilescreen.kt
package com.example.teamup.presentation.screen.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.Activity
import com.example.teamup.data.model.Experience
import com.example.teamup.data.model.UserProfileData
import com.example.teamup.data.viewmodels.ProfileViewModel
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

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { profileViewModel.getUserData(it) }
    }

    val userData by profileViewModel.userData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White
                ),
                actions = {
                    IconButton(onClick = {
                        try {
                            Log.d("ProfileNavigation", "Attempting to navigate to: ${Routes.ProfileSettings.routes}")
                            navController.navigate(Routes.ProfileSettings.routes)
                        } catch (e: Exception) {
                            Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                            Toast.makeText(context, "Cannot navigate to settings: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = White)
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
                userData?.let { profile ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { ProfileHeader(profile) }

                        // Activities Section
                        item {
                            ProfileSectionWithActions(
                                title = "Aktivitas",
                                itemCount = profile.activities.size,
                                onAddClick = {
                                    navController.navigate("create_post")
                                },
                                onEditClick = {
                                    navController.navigate("edit_activities")
                                }
                            ) {
                                if (profile.activities.isNotEmpty()) {
                                    profile.activities.take(3).forEach { activity ->
                                        ActivityItem(activity = activity)
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (profile.activities.size > 3) {
                                        TextButton(onClick = {
                                            navController.navigate("all_activities")
                                        }) {
                                            Text("Tampilkan semua posting →")
                                        }
                                    }
                                } else {
                                    Text(
                                        "Belum ada aktivitas",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Experience Section
                        item {
                            ProfileSectionWithActions(
                                title = "Pengalaman",
                                itemCount = profile.experiences.size,
                                onAddClick = {
                                    navController.navigate("add_experience")
                                },
                                onEditClick = {
                                    navController.navigate("edit_experiences")
                                }
                            ) {
                                if (profile.experiences.isNotEmpty()) {
                                    profile.experiences.forEach { experience ->
                                        ExperienceItem(experience = experience)
                                        Spacer(Modifier.height(12.dp))
                                    }
                                } else {
                                    Text(
                                        "Belum ada pengalaman kerja",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Education Section (Updated)
                        item {
                            ProfileSectionWithActions(
                                title = "Pendidikan",
                                itemCount = if ((profile.education?.school ?: "").isNotEmpty()) 1 else 0,
                                onAddClick = {
                                    navController.navigate("add_education")
                                },
                                onEditClick = {
                                    navController.navigate("edit_education")
                                }
                            ) {
                                val school = profile.education?.school.orEmpty()
                                val field = profile.education?.fieldOfStudy.orEmpty()

                                if (school.isNotEmpty() && field.isNotEmpty()) {
                                    Text(school, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(field)
                                } else {
                                    Text(
                                        "Belum ada informasi pendidikan",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }

                        }

                        // Skills Section (Updated)
                        item {
                            ProfileSectionWithActions(
                                title = "Keahlian",
                                itemCount = profile.skills.size,
                                onAddClick = {
                                    navController.navigate("add_skills")
                                },
                                onEditClick = {
                                    navController.navigate("edit_skills")
                                }
                            ) {
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
                                        "Belum ada keahlian yang ditambahkan",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Contact Information Section (unchanged)
                        item {
                            ProfileSection(title = "Informasi Kontak") {
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
}

@Composable
fun ProfileSectionWithActions(
    title: String,
    itemCount: Int,
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold)
                    if (itemCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$itemCount",
                            color = DodgerBlue,
                            fontSize = 14.sp
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = DodgerBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (itemCount > 0) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = SoftGray2,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Column {
        Text(
            activity.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (activity.mediaUrls.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activity.mediaUrls.take(3)) { mediaUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(mediaUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            formatTimestamp(activity.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ExperienceItem(experience: Experience) {
    Row {
        // Company logo placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                experience.company.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                experience.position,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                experience.company,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            val dateRange = if (experience.isCurrent) {
                "${experience.startDate} - Saat ini"
            } else {
                "${experience.startDate} - ${experience.endDate}"
            }
            Text(
                dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (experience.location.isNotEmpty()) {
                Text(
                    experience.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper function
fun formatTimestamp(timestamp: Long): String {
    // Implementation for formatting timestamp
    return "Baru saja" // Placeholder
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