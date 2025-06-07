package com.example.teamup.presentation.screen.competition

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import com.example.teamup.data.viewmodels.CompetitionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionDetailScreen(
    navController: NavController,
    competitionId: String,
    competitionViewModel: CompetitionViewModel
) {
    val context = LocalContext.current
    val uiState by competitionViewModel.uiState.collectAsState()

    // Find competition from ViewModel state
    val competition = uiState.competitions.find { it.id == competitionId }

    // Mock user authentication - in real app, get from AuthViewModel
    val currentUserId = "alyanis" // Your current login
    val isOwner = true // Mock: assume user owns all competitions for demo

    LaunchedEffect(competitionId) {
        // Refresh data if competition not found
        if (competition == null) {
            competitionViewModel.refreshData()
        }
    }

    when {
        uiState.isLoading && competition == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        competition == null -> {
            // Competition not found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kompetisi tidak ditemukan",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Kembali")
                    }
                }
            }
        }

        else -> {
            // ✅ Show competition detail
            CompetitionDetailContent(
                navController = navController,
                competition = competition,
                isOwner = isOwner,
                onEditClick = {
                    // Navigate to edit or show edit dialog
                    Toast.makeText(context, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompetitionDetailContent(
    navController: NavController,
    competition: CompetitionModel,
    isOwner: Boolean,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kompetisi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    // Share button
                    IconButton(onClick = {
                        val shareText = "Lihat kompetisi: ${competition.namaLomba}\n\n${competition.deskripsiLomba}"
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Kompetisi"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            CompetitionDetailBottomBar(
                competition = competition,
                isOwner = isOwner,
                onEditClick = onEditClick,
                onDownloadClick = {
                    if (competition.fileUrl.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(competition.fileUrl))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Tidak ada file untuk didownload", Toast.LENGTH_SHORT).show()
                    }
                },
                onJoinClick = {
                    Toast.makeText(context, "Berhasil mendaftar ${competition.namaLomba}!", Toast.LENGTH_LONG).show()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // ✅ Competition Image (Dynamic)
                if (competition.imageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = competition.imageUrl,
                                contentDescription = competition.namaLomba,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Status badge overlay
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopEnd),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Visibility status
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = competition.visibilityStatus,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when(competition.visibilityStatus) {
                                            CompetitionVisibilityStatus.PUBLISHED.value -> MaterialTheme.colorScheme.primaryContainer
                                            CompetitionVisibilityStatus.DRAFT.value -> MaterialTheme.colorScheme.secondaryContainer
                                            CompetitionVisibilityStatus.CANCELLED.value -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                // ✅ Title and Status (Dynamic)
                Column {
                    Text(
                        text = competition.namaLomba,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { },
                            label = { Text(competition.visibilityStatus) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when(competition.visibilityStatus) {
                                    CompetitionVisibilityStatus.PUBLISHED.value -> MaterialTheme.colorScheme.primaryContainer
                                    CompetitionVisibilityStatus.DRAFT.value -> MaterialTheme.colorScheme.secondaryContainer
                                    CompetitionVisibilityStatus.CANCELLED.value -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text(competition.activityStatus) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when(competition.activityStatus) {
                                    CompetitionActivityStatus.ACTIVE.value -> MaterialTheme.colorScheme.primaryContainer
                                    CompetitionActivityStatus.INACTIVE.value -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                    }
                }
            }

            item {
                // ✅ Competition Info (Dynamic)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informasi Kompetisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Execution date
                        DetailInfoRow(
                            icon = Icons.Default.Event,
                            label = "Tanggal Pelaksanaan",
                            value = competition.tanggalPelaksanaan
                        )

                        // Registration deadline (Dynamic)
                        competition.tanggalTutupPendaftaran?.let { deadline ->
                            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                            val formattedDeadline = dateFormat.format(deadline.toDate())

                            DetailInfoRow(
                                icon = Icons.Default.AccessTime,
                                label = "Batas Pendaftaran",
                                value = "$formattedDeadline${if (competition.autoCloseEnabled) " (Otomatis)" else ""}"
                            )
                        }

                        // Creation date (Dynamic)
                        val creationFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        val createdDate = creationFormat.format(competition.createdAt.toDate())

                        DetailInfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Dibuat pada",
                            value = createdDate
                        )
                    }
                }
            }

            item {
                // ✅ Description (Dynamic)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = competition.deskripsiLomba,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                // ✅ Files (Dynamic)
                if (competition.fileUrl.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "File Lampiran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Panduan Lomba")
                                }

                                TextButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(competition.fileUrl))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompetitionDetailBottomBar(
    competition: CompetitionModel,
    isOwner: Boolean,
    onEditClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isOwner -> {
                    // ✅ Owner actions
                    if (competition.fileUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                }
                competition.activityStatus == CompetitionActivityStatus.ACTIVE.value &&
                        competition.visibilityStatus == CompetitionVisibilityStatus.PUBLISHED.value -> {
                    // ✅ User can join
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daftar Kompetisi")
                    }
                }
                else -> {
                    // ✅ Competition closed
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kompetisi Ditutup")
                    }
                }
            }
        }
    }
}