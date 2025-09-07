// ===== AddEditSkillScreen.kt - SCREEN GABUNGAN UNTUK ADD/EDIT =====
package com.example.teamup.presentation.screen.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.user.SkillViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSkillScreen(
    navController: NavController,
    skillId: String? = null, // null = add mode, not null = edit mode
    skillViewModel: SkillViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Determine if we're in edit mode
    val isEditMode = !skillId.isNullOrEmpty()

    var skillName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("Beginner") }
    var showSuggestions by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Collect state dari ViewModel sebagai read-only
    val isLoading by skillViewModel.isLoading.collectAsState()
    val errorMessage by skillViewModel.errorMessage.collectAsState()
    val skillSuggestions by skillViewModel.skillSuggestions.collectAsState()
    val currentSkill by skillViewModel.currentSkill.collectAsState()

    val skillLevels = listOf("Beginner", "Intermediate", "Advanced", "Expert")

    // Load skill data if in edit mode
    LaunchedEffect(isEditMode, skillId) {
        if (isEditMode && skillId != null && currentUser?.uid != null) {
            skillViewModel.loadSkill(currentUser.uid, skillId)
        } else {
            skillViewModel.clearCurrentSkill()
        }
    }

    // Update form fields when current skill is loaded
    LaunchedEffect(currentSkill) {
        currentSkill?.let { skill ->
            skillName = skill.name
            selectedLevel = skill.level
        }
    }

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            skillViewModel.clearError()
        }
    }

    // Search suggestions when skillName changes (only in add mode)
    LaunchedEffect(skillName) {
        if (!isEditMode && skillName.length >= 2) {
            skillViewModel.searchSkillSuggestions(skillName)
            showSuggestions = true
        } else {
            showSuggestions = false
            skillViewModel.clearSuggestions()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Keahlian") },
            text = { Text("Apakah Anda yakin ingin menghapus keahlian ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        if (skillId != null && currentUser?.uid != null) {
                            skillViewModel.deleteSkill(
                                userId = currentUser.uid,
                                skillId = skillId
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Keahlian berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            }
                        }
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Keahlian" else "Tambah Keahlian") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DodgerBlue,
                    titleContentColor = White,
                    navigationIconContentColor = White
                ),
                actions = {
                    // Delete button (only in edit mode)
                    if (isEditMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = White)
                        }
                    }

                    // Save button
                    IconButton(
                        onClick = {
                            if (skillName.isNotBlank() && currentUser?.uid != null) {
                                if (isEditMode && skillId != null) {
                                    // Update existing skill
                                    skillViewModel.updateSkill(
                                        userId = currentUser.uid,
                                        skillId = skillId,
                                        name = skillName.trim(),
                                        level = selectedLevel
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Keahlian berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                            navController.navigateUp()
                                        }
                                    }
                                } else {
                                    // Add new skill
                                    skillViewModel.addSkill(
                                        userId = currentUser.uid,
                                        name = skillName.trim(),
                                        level = selectedLevel
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Keahlian berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                            navController.navigateUp()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Mohon isi nama keahlian", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = skillName.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = White)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Skill Name Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nama Keahlian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = skillName,
                        onValueChange = { skillName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan nama keahlian") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DodgerBlue,
                            focusedLabelColor = DodgerBlue
                        ),
                        trailingIcon = {
                            if (skillName.isNotEmpty()) {
                                IconButton(onClick = {
                                    skillName = ""
                                    showSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    // Skill Suggestions (only in add mode)
                    if (!isEditMode && showSuggestions && skillSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Saran keahlian:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            modifier = Modifier.height(120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(skillSuggestions) { suggestion ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = White2
                                    ),
                                    onClick = {
                                        skillName = suggestion
                                        showSuggestions = false
                                    }
                                ) {
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skill Level Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Level Keahlian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    skillLevels.forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLevel == level,
                                onClick = { selectedLevel = level },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DodgerBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getLevelDescription(level),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (skillName.isNotBlank() && currentUser?.uid != null) {
                        if (isEditMode && skillId != null) {
                            // Update existing skill
                            skillViewModel.updateSkill(
                                userId = currentUser.uid,
                                skillId = skillId,
                                name = skillName.trim(),
                                level = selectedLevel
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Keahlian berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            }
                        } else {
                            // Add new skill
                            skillViewModel.addSkill(
                                userId = currentUser.uid,
                                name = skillName.trim(),
                                level = selectedLevel
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Keahlian berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Mohon isi nama keahlian", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DodgerBlue
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = skillName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isEditMode) "Perbarui Keahlian" else "Simpan Keahlian")
            }
        }
    }
}

private fun getLevelDescription(level: String): String {
    return when (level) {
        "Beginner" -> "Pemula - Baru memulai mempelajari keahlian ini"
        "Intermediate" -> "Menengah - Memiliki pengalaman dan pemahaman dasar"
        "Advanced" -> "Lanjutan - Berpengalaman dan mampu menangani tugas kompleks"
        "Expert" -> "Ahli - Sangat berpengalaman dan mampu mengajar orang lain"
        else -> ""
    }
}