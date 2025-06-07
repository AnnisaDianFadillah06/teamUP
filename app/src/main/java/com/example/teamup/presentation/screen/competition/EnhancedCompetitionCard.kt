package com.example.teamup.presentation.screen.competition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // ✅ Mock user ownership - in real app, get from auth
    val currentUserId = "alyanis" // Your current login
    val isOwner = true // Mock: assume user owns all competitions for now

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDetailClick(competition) }, // ✅ Click to view detail
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header with title and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = competition.namaLomba,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    // ✅ Edit button - only for owners
                    if (isOwner) {
                        IconButton(
                            onClick = { onEditClick(competition) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Competition",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // View detail button
                    IconButton(
                        onClick = { onDetailClick(competition) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Visibility status badge
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

                // Activity status badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = competition.activityStatus,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when(competition.activityStatus) {
                            CompetitionActivityStatus.ACTIVE.value -> MaterialTheme.colorScheme.primaryContainer
                            CompetitionActivityStatus.INACTIVE.value -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = competition.deskripsiLomba,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Competition details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Execution date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pelaksanaan: ${competition.tanggalPelaksanaan}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Registration deadline if available
                competition.tanggalTutupPendaftaran?.let { deadline ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        val formattedDeadline = dateFormat.format(deadline.toDate())

                        Text(
                            text = "Batas daftar: $formattedDeadline",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Cabang lomba
                if (associatedCabangList.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cabang: ${associatedCabangList.joinToString(", ") { it.namaCabang }}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // File attachments
            if (competition.imageUrl.isNotBlank() || competition.fileUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (competition.imageUrl.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Has Image",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (competition.fileUrl.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Has File",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}