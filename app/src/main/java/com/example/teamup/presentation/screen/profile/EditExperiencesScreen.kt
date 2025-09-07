//EditExperiencesScreen.kt
package com.example.teamup.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.user.Experience
import com.example.teamup.data.viewmodels.user.ExperienceViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExperiencesScreen(
    navController: NavController,
    experienceViewModel: ExperienceViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val experiences by experienceViewModel.experiences.collectAsState()
    val isLoading by experienceViewModel.isLoading.collectAsState()
    val errorMessage by experienceViewModel.errorMessage.collectAsState()

    // Load experiences when screen opens
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            experienceViewModel.loadExperiences(userId)
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You can show a snackbar or toast here
            experienceViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengalaman") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Clear current experience when adding new
                        experienceViewModel.clearCurrentExperience()
                        // Navigate to AddExperience without parameter (mode add)
                        navController.navigate(Routes.AddExperience.routes.replace("/{experienceId}", ""))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = White)
                    }
                },
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
                .background(White2)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DodgerBlue)
                    }
                }
                experiences.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(experiences) { experience ->
                            ExperienceEditItem(
                                experience = experience,
                                onEditClick = {
                                    // Navigate to form edit with experienceId
                                    navController.navigate(Routes.AddExperience.createRoute(experience.id))
                                }
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada pengalaman kerja",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                experienceViewModel.clearCurrentExperience()
                                navController.navigate(Routes.AddExperience.routes.replace("/{experienceId}", ""))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Pengalaman Pertama", color = White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceEditItem(
    experience: Experience,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company logo placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = experience.company.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = experience.position,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = experience.company,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                val dateRange = if (experience.isCurrentRole) {
                    "${experience.startDate} - Saat ini"
                } else {
                    "${experience.startDate} - ${experience.endDate}"
                }
                Text(
                    text = dateRange,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (experience.location.isNotEmpty()) {
                    Text(
                        text = experience.location,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                // Show job type if available
                if (experience.jobType.isNotEmpty()) {
                    Text(
                        text = experience.jobType,
                        color = DodgerBlue,
                        fontSize = 12.sp
                    )
                }
                // Show skills if available
                if (experience.skills.isNotEmpty()) {
                    Text(
                        text = "Skills: ${experience.skills.joinToString(", ")}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = SoftGray2
                )
            }
        }
    }
}