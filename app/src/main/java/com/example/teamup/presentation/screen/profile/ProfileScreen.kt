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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.UserProfileData
import com.example.teamup.data.viewmodels.user.ProfileViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    // Collect education data
    val userEducations by profileViewModel.userEducations.collectAsState()

    // Get current education data (sama seperti di ProfileSettingsScreen)
    val currentEducation = userEducations.firstOrNull { it.isCurrentlyStudying }
        ?: userEducations.maxByOrNull { it.createdAt }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            // Load all profile data separately
            profileViewModel.getUserData(userId)
            profileViewModel.loadUserActivities(userId)
            profileViewModel.loadUserExperiences(userId)
            profileViewModel.loadUserEducations(userId)
            profileViewModel.loadUserSkills(userId)
        }
    }

    // Collect state from ViewModel
    val userData by profileViewModel.userData.collectAsState()
    val activities by profileViewModel.activities.collectAsState()
    val experiences by profileViewModel.experiences.collectAsState()
    val educations by profileViewModel.educations.collectAsState()
    val skills by profileViewModel.skills.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            ProfileTopBar(navController = navController)
        },
        contentWindowInsets = WindowInsets(0), // Remove default content insets
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(paddingValues)
                .navigationBarsPadding() // Add navigation bar padding
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DodgerBlue
                )
            } else {
                userData?.let { profile ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 40.dp) // Add bottom padding for better spacing
                    ) {
                        item {
                            ProfileHeader(
                                profile = profile,
                                skills = skills.take(2),
                                currentEducation = currentEducation
                            )
                        }

                        // Activities Section - Now using separate activities list
                        item {
                            ProfileSectionWithActions(
                                title = "Aktivitas",
                                itemCount = activities.size,
                                onAddClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.CreatePost.routes}")
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            navController.navigate(Routes.CreatePost.createRoute(userId))
                                        } else {
                                            Log.e("Navigation", "User ID is null, can't navigate to CreatePost")
                                            // Bisa tampilkan snackbar, dialog, dll.
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }                                },
                                onEditClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.EditActivities.routes}")
                                        navController.navigate(Routes.EditActivities.routes)
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                if (activities.isNotEmpty()) {
                                    activities.take(5).forEachIndexed { index, activity ->
                                        ActivityItem(activity = activity)
                                        if (index < activities.take(5).size - 1) {
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                    if (activities.size > 5) {
                                        Spacer(Modifier.height(8.dp))
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

                        // Experience Section - PERBAIKAN NAVIGASI
                        item {
                            ProfileSectionWithActions(
                                title = "Pengalaman",
                                itemCount = experiences.size,
                                onAddClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.AddExperience.createRoute("")}")
                                        navController.navigate(Routes.AddExperience.createRoute(""))
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEditClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.EditExperiences.routes}")
                                        navController.navigate(Routes.EditExperiences.routes)
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                if (experiences.isNotEmpty()) {
                                    experiences.take(5).forEachIndexed { index, experience ->
                                        ExperienceItem(experience = experience)
                                        if (index < experiences.take(5).size - 1) {
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                    if (experiences.size > 5) {
                                        Spacer(Modifier.height(8.dp))
                                        TextButton(onClick = {
                                            // Navigate to all experiences
                                        }) {
                                            Text("Tampilkan semua pengalaman →")
                                        }
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

                        // Education Section - PERBAIKAN NAVIGASI
                        item {
                            ProfileSectionWithActions(
                                title = "Pendidikan",
                                itemCount = educations.size,
                                onAddClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.AddEducation.createRoute("")}")
                                        navController.navigate(Routes.AddEducation.createRoute(""))
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEditClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.EditEducations.routes}")
                                        navController.navigate(Routes.EditEducations.routes)
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                if (educations.isNotEmpty()) {
                                    educations.take(5).forEachIndexed { index, education ->
                                        EducationItem(education = education)
                                        if (index < educations.take(5).size - 1) {
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                    if (educations.size > 5) {
                                        Spacer(Modifier.height(8.dp))
                                        TextButton(onClick = {
                                            // Navigate to all educations
                                        }) {
                                            Text("Tampilkan semua pendidikan →")
                                        }
                                    }
                                } else {
                                    Text(
                                        "Belum ada informasi pendidikan",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Skills Section - Now using separate skills list
                        item {
                            ProfileSectionWithActions(
                                title = "Keahlian",  // UBAH dari "Keahlian"
                                itemCount = skills.size,
                                onAddClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.AddSkill.routes}")
                                        navController.navigate(Routes.AddSkill.routes)
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEditClick = {
                                    try {
                                        Log.d("ProfileNavigation", "Navigating to: ${Routes.EditSkills.routes}")
                                        navController.navigate(Routes.EditSkills.routes)
                                    } catch (e: Exception) {
                                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                                        Toast.makeText(context, "Tidak dapat membuka halaman: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            ) {
                                if (skills.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        skills.forEach { skill ->
                                            AssistChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        text = skill,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                },
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
                    }
                } ?: Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Profile information not available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DodgerBlue, // Gunakan warna theme yang sama
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Title
            Text(
                text = "Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )

            // Settings Icon
            IconButton(
                onClick = {
                    try {
                        Log.d("ProfileNavigation", "Attempting to navigate to: ${Routes.ProfileSettings.routes}")
                        navController.navigate(Routes.ProfileSettings.routes)
                    } catch (e: Exception) {
                        Log.e("ProfileNavigation", "Navigation error: ${e.message}", e)
                        // Handle error
                    }
                }
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                activity.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            val allMediaUrls = activity.getAllMediaUrls()
            if (allMediaUrls.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allMediaUrls.take(3)) { mediaUrl ->
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
            Spacer(Modifier.height(8.dp))
            Text(
                formatTimestamp(activity.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun ExperienceItem(experience: Experience) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
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
                val dateRange = if (experience.isCurrentRole) {
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
}

@Composable
fun EducationItem(education: Education) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // School logo placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DodgerBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    education.school.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DodgerBlue
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    education.school,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (education.degree.isNotEmpty()) {
                    Text(
                        education.degree,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                if (education.fieldOfStudy.isNotEmpty()) {
                    Text(
                        education.fieldOfStudy,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (education.startDate.isNotEmpty()) {
                    val dateRange = if (education.isCurrentlyStudying) {
                        "${education.startDate} - Sekarang"
                    } else {
                        "${education.startDate} - ${education.endDate}"
                    }
                    Text(
                        dateRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Helper function for timestamp formatting
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Baru saja" // Less than 1 minute
        diff < 3_600_000 -> "${diff / 60_000} menit yang lalu" // Less than 1 hour
        diff < 86_400_000 -> "${diff / 3_600_000} jam yang lalu" // Less than 1 day
        diff < 604_800_000 -> "${diff / 86_400_000} hari yang lalu" // Less than 1 week
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun ProfileHeader(
    profile: UserProfileData,
    skills: List<String> = emptyList(),
    currentEducation: Education? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture - Left side
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, DodgerBlue, CircleShape)
                ) {
                    if (profile.profilePictureUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(profile.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DodgerBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.fullName.take(2).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DodgerBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Profile Information - Right side
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Full Name
                    Text(
                        text = profile.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        lineHeight = 13.sp  // Ditambahkan untuk mengurangi jarak vertikal default
                    )

                    // Username - langsung tanpa spacing
                    Text(
                        text = "@${profile.username}",
                        fontSize = 14.sp,
                        color = DodgerBlue,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // University - only show if exists
                    currentEducation?.let { education ->
                        if (education.school.isNotEmpty()) {
                            Text(
                                text = education.school,
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (education.fieldOfStudy.isNotEmpty()) {
                            Text(
                                text = education.fieldOfStudy,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        // TAMBAHAN: Tingkat dan Semester di bawah field of study
                        if (education.isCurrentlyStudying) {
                            val academicInfo = buildString {
                                if (education.currentLevel.isNotEmpty()) {
                                    append(education.currentLevel)
                                }
                                if (education.currentSemester.isNotEmpty()) {
                                    if (isNotEmpty()) append(" • ")
                                    append(education.currentSemester)
                                }
                            }

                            if (academicInfo.isNotEmpty()) {
                                Text(
                                    text = academicInfo,
                                    fontSize = 12.sp,
                                    color = DodgerBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Email menggunakan ContactInfoRow
                    ContactInfoRow(
                        icon = Icons.Default.Email,
                        text = profile.email
                    )

                    // Phone - only show if exists
                    if (profile.phone.isNotEmpty()) {
                        ContactInfoRow(
                            icon = Icons.Default.Phone,
                            text = profile.phone
                        )
                    }

                    // Location - only show if exists
                    if (profile.location.isNotEmpty()) {
                        ContactInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = profile.location
                        )
                    }
                }
            }

            // Bottom section below profile picture
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bio
                if (profile.bio.isNotEmpty()) {
                    Text(
                        text = profile.bio,
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }

                // PERUBAHAN: Specialization/Role - langsung text profesional tanpa judul dan bubble
                if (profile.specialization.isNotEmpty()) {
                    Text(
                        text = profile.specialization,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DodgerBlue,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    text: String,
    horizontalSpacing: Dp = 6.dp,
    verticalPadding: Dp = 2.dp,
    iconSize: Dp = 16.dp,
    fontSize: TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.padding(vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Wrap icon in Box dengan fixed height
        Box(
            modifier = Modifier
                .size(iconSize)
                .wrapContentSize(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SoftGray2,
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.width(horizontalSpacing))

        // Wrap text in Box dengan height yang lebih tinggi untuk descender
        Box(
            modifier = Modifier.height(iconSize + 4.dp), // Tambah 4dp untuk descender
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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