
//EditExperiencesScreen.kt
package com.example.teamup.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.teamup.data.model.Experience
import com.example.teamup.data.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExperiencesScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userData by profileViewModel.userData.collectAsState()

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
                        navController.navigate("add_experience")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Add", tint = White)
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
            userData?.let { profile ->
                if (profile.experiences.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(profile.experiences) { experience ->
                            ExperienceEditItem(
                                experience = experience,
                                onEditClick = {
                                    navController.navigate("edit_experience/${experience.id}")
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada pengalaman kerja",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
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
                    fontSize = 16.sp
                )
                Text(
                    experience.company,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                val dateRange = if (experience.isCurrent) {
                    "${experience.startDate} - Saat ini"
                } else {
                    "${experience.startDate} - ${experience.endDate}"
                }
                Text(
                    dateRange,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (experience.location.isNotEmpty()) {
                    Text(
                        experience.location,
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
