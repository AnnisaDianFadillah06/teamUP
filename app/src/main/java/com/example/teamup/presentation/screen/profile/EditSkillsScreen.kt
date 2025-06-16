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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Code
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
import com.example.teamup.data.model.user.Skill
import com.example.teamup.data.viewmodels.user.SkillViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSkillsScreen(
    navController: NavController,
    skillViewModel: SkillViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val skills by skillViewModel.skills.collectAsState()
    val isLoading by skillViewModel.isLoading.collectAsState()
    val errorMessage by skillViewModel.errorMessage.collectAsState()

    // Load skills when screen opens
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            skillViewModel.loadUserSkills(userId)
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You can show a snackbar or toast here
            skillViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keahlian") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Clear current skill when adding new
                        skillViewModel.clearCurrentSkill()
                        // Navigate to AddSkill without parameter (mode add)
                        navController.navigate(Routes.AddSkill.createRoute())
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
                skills.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(skills) { skill ->
                            SkillEditItem(
                                skill = skill,
                                onEditClick = {
                                    // Navigate to form edit with skillId
                                    navController.navigate(Routes.AddSkill.createRoute(skill.id))
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
                            text = "Belum ada keahlian",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                skillViewModel.clearCurrentSkill()
                                navController.navigate(Routes.AddSkill.createRoute())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Keahlian Pertama", color = White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillEditItem(
    skill: Skill,
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
            // Skill icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DodgerBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = "Skill",
                    tint = DodgerBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skill.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                // Skill level with color coding
                val levelColor = when (skill.level.lowercase()) {
                    "beginner" -> Color(0xFF4CAF50) // Green
                    "intermediate" -> Color(0xFFFF9800) // Orange
                    "advanced" -> Color(0xFF2196F3) // Blue
                    "expert" -> Color(0xFF9C27B0) // Purple
                    else -> Color.Gray
                }

                Text(
                    text = when (skill.level.lowercase()) {
                        "beginner" -> "Pemula"
                        "intermediate" -> "Menengah"
                        "advanced" -> "Lanjutan"
                        "expert" -> "Ahli"
                        else -> skill.level
                    },
                    color = levelColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Source information
                if (!skill.fromExperienceId.isNullOrEmpty()) {
                    Text(
                        text = "Dari Pengalaman Kerja",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else if (!skill.fromEducationId.isNullOrEmpty()) {
                    Text(
                        text = "Dari Pendidikan",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Endorsement info
                if (skill.endorsementCount > 0) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Endorsement",
                            tint = Color(0xFFFFD700), // Gold
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${skill.endorsementCount} endorsement",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
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