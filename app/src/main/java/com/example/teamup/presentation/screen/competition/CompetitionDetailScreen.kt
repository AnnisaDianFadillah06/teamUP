package com.example.teamup.presentation.screen.competition

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.teamup.route.Routes
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
                competitionViewModel = competitionViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompetitionDetailContent(
    navController: NavController,
    competition: CompetitionModel,
    competitionViewModel: CompetitionViewModel
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Kompetisi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // ✅ Edit button for everyone
                    IconButton(
                        onClick = {
                            // Navigate to edit competition form
                            competitionViewModel.selectCompetitionForEdit(competition)
                            navController.navigate(Routes.Competition.routes) {
                                popUpTo(Routes.Competition.routes) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            CompetitionDetailBottomBar(
                competition = competition,
                onEditClick = {
                    // Navigate to edit form
                    competitionViewModel.selectCompetitionForEdit(competition)
                    navController.navigate(Routes.Competition.routes) {
                        popUpTo(Routes.Competition.routes) { inclusive = true }
                    }
                },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                // ✅ BEAUTIFUL: Competition Image with overlay
                if (competition.imageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = competition.imageUrl,
                                contentDescription = competition.namaLomba,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Status badges overlay
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopEnd),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusBadge(
                                    text = competition.visibilityStatus,
                                    backgroundColor = when(competition.visibilityStatus) {
                                        CompetitionVisibilityStatus.PUBLISHED.value -> Color(0xFF4CAF50)
                                        CompetitionVisibilityStatus.DRAFT.value -> Color(0xFFFF9800)
                                        CompetitionVisibilityStatus.CANCELLED.value -> Color(0xFFF44336)
                                        else -> Color(0xFF9E9E9E)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // ✅ BEAUTIFUL: Title and Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = competition.namaLomba,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(
                                text = competition.visibilityStatus,
                                backgroundColor = when(competition.visibilityStatus) {
                                    CompetitionVisibilityStatus.PUBLISHED.value -> Color(0xFF4CAF50)
                                    CompetitionVisibilityStatus.DRAFT.value -> Color(0xFFFF9800)
                                    CompetitionVisibilityStatus.CANCELLED.value -> Color(0xFFF44336)
                                    else -> Color(0xFF9E9E9E)
                                }
                            )
                            StatusBadge(
                                text = competition.activityStatus,
                                backgroundColor = when(competition.activityStatus) {
                                    CompetitionActivityStatus.ACTIVE.value -> Color(0xFF2196F3)
                                    CompetitionActivityStatus.INACTIVE.value -> Color(0xFF9E9E9E)
                                    else -> Color(0xFF9E9E9E)
                                }
                            )
                        }
                    }
                }
            }

            item {
                // ✅ BEAUTIFUL: Competition Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Informasi Kompetisi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Execution date
                        DetailInfoRow(
                            icon = Icons.Default.Event,
                            label = "Tanggal Pelaksanaan",
                            value = competition.tanggalPelaksanaan,
                            iconColor = MaterialTheme.colorScheme.primary
                        )

                        // Registration deadline
                        competition.tanggalTutupPendaftaran?.let { deadline ->
                            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                            val formattedDeadline = dateFormat.format(deadline.toDate())

                            Spacer(modifier = Modifier.height(12.dp))
                            DetailInfoRow(
                                icon = Icons.Default.AccessTime,
                                label = "Batas Pendaftaran",
                                value = "$formattedDeadline${if (competition.autoCloseEnabled) " (Otomatis)" else ""}",
                                iconColor = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Creation date
                        val creationFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        val createdDate = creationFormat.format(competition.createdAt.toDate())

                        Spacer(modifier = Modifier.height(12.dp))
                        DetailInfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Dibuat pada",
                            value = createdDate,
                            iconColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            item {
                // ✅ BEAUTIFUL: Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = competition.deskripsiLomba,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                // ✅ BEAUTIFUL: File Attachments
                if (competition.fileUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "File Lampiran",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Panduan Lomba",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(competition.fileUrl))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Download",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom spacing
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CompetitionDetailBottomBar(
    competition: CompetitionModel,
    onEditClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ SIMPLIFIED: Edit button for everyone + Join/Download based on status
            when {
                competition.activityStatus == CompetitionActivityStatus.ACTIVE.value &&
                        competition.visibilityStatus == CompetitionVisibilityStatus.PUBLISHED.value -> {
                    // Active competition - show Edit + Join
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daftar")
                    }
                }
                competition.fileUrl.isNotBlank() -> {
                    // Has file - show Edit + Download
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }
                }
                else -> {
                    // Default - just edit button
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Kompetisi")
                    }
                }
            }
        }
    }
}