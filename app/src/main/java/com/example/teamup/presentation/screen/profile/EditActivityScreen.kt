package com.example.teamup.presentation.screen.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.SoftGray2
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.user.Activity
import com.example.teamup.data.viewmodels.user.ActivityViewModel
import com.example.teamup.route.Routes
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    navController: NavController,
    activityViewModel: ActivityViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userActivities by activityViewModel.userActivities.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()
    val errorMessage by activityViewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Load activities when screen opens
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            activityViewModel.loadUserActivities(userId)
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You can show a snackbar or toast here
            activityViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aktivitas/Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        activityViewModel.clearCurrentActivity()
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            navController.navigate(Routes.CreatePost.createRoute(userId))
                        } else {
                            Log.e("Navigation", "User ID is null, can't navigate to CreatePost")
                        }
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
                userActivities.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userActivities) { activity ->
                            ActivityEditItem(
                                activity = activity,
                                onEditClick = {
                                    activityViewModel.setCurrentActivity(activity)
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        navController.navigate(Routes.CreatePost.createRoute(userId, activity.id))
                                    } else {
                                        Log.e("Navigation", "User ID is null, can't navigate to CreatePost")
                                    }
                                },
                                onDeleteClick = {
                                    showDeleteDialog = activity.id
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
                            text = "Belum ada aktivitas/post",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                activityViewModel.clearCurrentActivity()
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    navController.navigate(Routes.CreatePost.createRoute(userId))
                                } else {
                                    Log.e("Navigation", "User ID is null, can't navigate to CreatePost")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DodgerBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Post Pertama", color = White)
                        }
                    }
                }
            }

            // Delete confirmation dialog
            showDeleteDialog?.let { activityId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Konfirmasi Hapus") },
                    text = { Text("Apakah Anda yakin ingin menghapus aktivitas ini? Tindakan ini tidak dapat dibatalkan.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                currentUser?.uid?.let { userId ->
                                    activityViewModel.deleteActivity(userId, activityId)
                                }
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityEditItem(
    activity: Activity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val createdDate = dateFormat.format(Date(activity.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Post icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DodgerBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (activity.hasMedia()) Icons.Default.Image else Icons.Default.Article,
                    contentDescription = "Post",
                    tint = DodgerBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.content.take(100) + if (activity.content.length > 100) "..." else "",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = createdDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                // Visibility indicator
                Text(
                    text = when (activity.visibility) {
                        "public" -> "Publik"
                        "connections" -> "Koneksi"
                        "private" -> "Pribadi"
                        else -> "Publik"
                    },
                    color = DodgerBlue,
                    fontSize = 11.sp
                )

                // Engagement stats
                if (activity.likesCount > 0 || activity.commentsCount > 0) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activity.likesCount > 0) {
                            Text(
                                text = "${activity.likesCount} likes",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        if (activity.commentsCount > 0) {
                            Text(
                                text = "${activity.commentsCount} komentar",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Media indicator
                if (activity.hasMedia()) {
                    Text(
                        text = "${activity.getAllMediaUrls().size} media",
                        color = DodgerBlue,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = SoftGray2
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}