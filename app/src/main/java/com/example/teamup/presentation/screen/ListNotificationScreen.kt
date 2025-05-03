package com.example.teamup.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.data.model.NotificationModel
import com.example.teamup.data.viewmodels.NotificationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ListNotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel
) {
    // Load notifications when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    val unreadNotifications by viewModel.unreadNotifications.collectAsState()
    val readNotifications by viewModel.readNotifications.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Belum Dibaca", "Telah Dibaca")

    // For bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentNotification by remember { mutableStateOf<NotificationModel?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifikasi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = DodgerBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                )

                                // Show count badge for unread notifications
                                if (index == 0 && unreadNotifications.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(DodgerBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unreadNotifications.size.toString(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Show count badge for read notifications
                                if (index == 1 && readNotifications.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = readNotifications.size.toString(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Show notifications based on selected tab
            when (selectedTabIndex) {
                0 -> {
                    // Unread notifications
                    if (unreadNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada notifikasi yang belum dibaca",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        NotificationList(
                            notifications = unreadNotifications,
                            onNotificationClick = { notification ->
                                if (notification.type == NotificationModel.Type.JOIN_REQUEST) {
                                    currentNotification = notification
                                    showBottomSheet = true
                                } else {
                                    // Mark as read
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        )
                    }
                }
                1 -> {
                    // Read notifications
                    if (readNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada notifikasi yang telah dibaca",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        NotificationList(
                            notifications = readNotifications,
                            onNotificationClick = { notification ->
                                // No action needed for read notifications, maybe show details?
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for join requests
    if (showBottomSheet && currentNotification != null) {
        LaunchedEffect(Unit) {
            bottomSheetState.show()
        }

        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    showBottomSheet = false
                    currentNotification = null
                }
            },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            JoinRequestBottomSheet(
                notification = currentNotification!!,
                onAccept = {
                    viewModel.handleJoinRequest(currentNotification!!.id, true)
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheet = false
                        currentNotification = null
                    }
                },
                onReject = {
                    viewModel.handleJoinRequest(currentNotification!!.id, false)
                    scope.launch {
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheet = false
                        currentNotification = null
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<NotificationModel>,
    onNotificationClick: (NotificationModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(notifications) { notification ->
            NotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: NotificationModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = notification.senderImageResId),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.senderName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (notification.additionalInfo != null) {
                    Text(
                        text = notification.additionalInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun JoinRequestBottomSheet(
    notification: NotificationModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // User info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = notification.senderImageResId),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = notification.senderName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                if (notification.additionalInfo != null) {
                    Text(
                        text = "(${notification.additionalInfo})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Request message
        Text(
            text = "Request: ${notification.senderName} mengajukan untuk bergabung dengan tim ${notification.additionalInfo ?:""} Anda, Terima?",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = DodgerBlue
                )
            ) {
                Text(text = "Tolak")
            }

            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DodgerBlue,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Terima")
            }
        }
    }
}

// Helper function to format timestamp
fun formatTimestamp(date: Date): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }

    return when {
        // Today
        now.get(Calendar.DATE) == then.get(Calendar.DATE) &&
                now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Yesterday
        now.get(Calendar.DATE) - then.get(Calendar.DATE) == 1 &&
                now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            "Kemarin ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        }
        // Within a week
        now.get(Calendar.DATE) - then.get(Calendar.DATE) < 7 &&
                now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))
            "${dayFormat.format(date)} ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        }
        // Same year
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            SimpleDateFormat("d MMM, HH:mm", Locale("id", "ID")).format(date)
        }
        // Different year
        else -> {
            SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")).format(date)
        }
    }
}