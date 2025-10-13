package com.example.teamup.presentation.screen.competition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.teamup.data.model.CabangLombaModel
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EnhancedCompetitionCard(
    competition: CompetitionModel,
    associatedCabangList: List<CabangLombaModel>,
    onEditClick: (CompetitionModel) -> Unit,
    onDetailClick: (CompetitionModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clickable { onDetailClick(competition) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // ✅ ENHANCED: Header with title and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Competition title
                    Text(
                        text = competition.namaLomba,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show creation date
                    Text(
                        text = "Dibuat: ${getFormattedCreationDate(competition)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // ✅ ACTION BUTTONS: Edit + View for everyone
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // ✅ Edit button for everyone
                    IconButton(
                        onClick = {
                            println("🔍 Edit clicked for: ${competition.namaLomba}")
                            onEditClick(competition)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kompetisi",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // ✅ View detail button
                    IconButton(
                        onClick = { onDetailClick(competition) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Lihat Detail",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ BEAUTIFUL: Status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Visibility status
                StatusBadge(
                    text = competition.visibilityStatus,
                    backgroundColor = when(competition.visibilityStatus) {
                        CompetitionVisibilityStatus.PUBLISHED.value -> Color(0xFF4CAF50)
                        CompetitionVisibilityStatus.DRAFT.value -> Color(0xFFFF9800)
                        CompetitionVisibilityStatus.CANCELLED.value -> Color(0xFFF44336)
                        else -> Color(0xFF9E9E9E)
                    }
                )

                // Activity status
                StatusBadge(
                    text = competition.activityStatus,
                    backgroundColor = when(competition.activityStatus) {
                        CompetitionActivityStatus.ACTIVE.value -> Color(0xFF2196F3)
                        CompetitionActivityStatus.INACTIVE.value -> Color(0xFF9E9E9E)
                        else -> Color(0xFF9E9E9E)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ BEAUTIFUL: Description
            Text(
                text = competition.deskripsiLomba,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ BEAUTIFUL: Competition details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Execution date
                DetailRow(
                    icon = Icons.Default.Event,
                    label = "Pelaksanaan",
                    value = competition.tanggalPelaksanaan,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                // Registration deadline
                competition.tanggalTutupPendaftaran?.let { deadline ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val formattedDeadline = dateFormat.format(deadline.toDate())

                    DetailRow(
                        icon = Icons.Default.AccessTime,
                        label = "Batas daftar",
                        value = formattedDeadline,
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                }

                // Cabang lomba
                if (associatedCabangList.isNotEmpty()) {
                    DetailRow(
                        icon = Icons.Default.Category,
                        label = "Cabang",
                        value = associatedCabangList.joinToString(", ") { it.namaCabang },
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        maxLines = 2
                    )
                }
            }

            // ✅ BEAUTIFUL: File attachments
            if (competition.imageUrl.isNotBlank() || competition.fileUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lampiran:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (competition.imageUrl.isNotBlank()) {
                            AttachmentChip(
                                icon = Icons.Default.Image,
                                text = "Gambar",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (competition.fileUrl.isNotBlank()) {
                            AttachmentChip(
                                icon = Icons.Default.AttachFile,
                                text = "Dokumen",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ Helper function for creation date
@Composable
private fun getFormattedCreationDate(competition: CompetitionModel): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(competition.createdAt.toDate())
}

// ✅ BEAUTIFUL: Status badge
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

// ✅ BEAUTIFUL: Detail row
@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    maxLines: Int = 1
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ✅ BEAUTIFUL: Attachment chip
@Composable
private fun AttachmentChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}