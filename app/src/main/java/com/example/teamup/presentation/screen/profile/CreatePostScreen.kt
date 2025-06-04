package com.example.teamup.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var postContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    // Launcher untuk memilih gambar
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Buat Postingan",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (postContent.isNotBlank()) {
                                isPosting = true
                                // TODO: Implement posting logic
                                // For now, just simulate posting and navigate back
                                navController.popBackStack()
                            }
                        },
                        enabled = postContent.isNotBlank() && !isPosting
                    ) {
                        if (isPosting) {
                            Text(
                                "Posting",
                                color = SoftGray2,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                "Posting",
                                color = if (postContent.isNotBlank()) DodgerBlue else SoftGray2,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White2)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // User Profile Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DodgerBlue),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for user avatar - you can replace with actual image
                    Text(
                        "AN",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Semua orang",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        // Dropdown icon would go here
                    }
                }
            }

            // Post Content TextField
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                placeholder = {
                    Text(
                        "Apa yang ingin Anda bicarakan?",
                        color = SoftGray2,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Selected Image Preview
            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Remove image button
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image picker button
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Add image",
                        tint = SoftGray2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Calendar/Schedule button
                IconButton(
                    onClick = { /* TODO: Implement schedule functionality */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Schedule post",
                        tint = SoftGray2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More options button
                IconButton(
                    onClick = { /* TODO: Implement more options */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "More options",
                        tint = SoftGray2,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}