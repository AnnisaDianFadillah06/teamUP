//EditEducationsScreen.kt (Perbaikan)
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
import androidx.compose.material.icons.filled.School
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
import com.example.teamup.data.model.user.Education
import com.example.teamup.data.model.user.getDisplayPeriod
import com.example.teamup.data.model.user.getDisplayTitle
import com.example.teamup.data.viewmodels.user.EducationViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEducationsScreen(
    navController: NavController,
    educationViewModel: EducationViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val educations by educationViewModel.educations.collectAsState()
    val isLoading by educationViewModel.isLoading.collectAsState()
    val errorMessage by educationViewModel.errorMessage.collectAsState()

    // Load educations when screen opens - PERBAIKAN: gunakan method yang benar
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            educationViewModel.loadEducations(userId) // Method yang benar sesuai ViewModel
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You can show a snackbar or toast here
            educationViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendidikan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Clear current education when adding new
                        educationViewModel.clearCurrentEducation()
                        // Navigate to AddEducation without parameter (mode add)
                        navController.navigate("add_education") // PERBAIKAN: route yang lebih sederhana
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
                educations.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(educations) { education ->
                            EducationEditItem(
                                education = education,
                                onEditClick = {
                                    // Set current education untuk edit
                                    educationViewModel.setCurrentEducation(education)
                                    // Navigate to form edit dengan educationId
                                    navController.navigate("add_education/${education.id}") // PERBAIKAN: route dengan ID
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
                            text = "Belum ada data pendidikan",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                educationViewModel.clearCurrentEducation()
                                navController.navigate("add_education")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Pendidikan Pertama", color = White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EducationEditItem(
    education: Education,
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
            // School icon placeholder (tetap sama)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DodgerBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "School",
                    tint = DodgerBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = education.getDisplayTitle(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = education.school,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = education.getDisplayPeriod(),
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                // TAMBAHAN: Tampilkan tingkat dan semester jika sedang belajar
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
                            color = DodgerBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Sedang Belajar",
                        color = DodgerBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (education.grade.isNotEmpty()) {
                    Text(
                        text = "GPA: ${education.grade}",
                        color = Color.Gray,
                        fontSize = 12.sp
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