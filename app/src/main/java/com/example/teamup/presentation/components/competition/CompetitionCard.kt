//package com.example.teamup.presentation.components.competition
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.FilePresent
//import androidx.compose.material.icons.filled.Timer
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.teamup.R
//import com.example.teamup.data.model.CabangLombaModel
//import com.example.teamup.data.model.CompetitionModel
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//@Composable
//fun CompetitionCard(
//    competition: CompetitionModel,
//    associatedCabangList: List<CabangLombaModel>,
//    onEditClick: (CompetitionModel) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onEditClick(competition) },
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = competition.namaLomba,
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.weight(1f)
//                )
//
//                // Edit button
//                IconButton(
//                    onClick = { onEditClick(competition) },
//                    modifier = Modifier.size(32.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Edit,
//                        contentDescription = "Edit Competition",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = competition.deskripsiLomba,
//                style = MaterialTheme.typography.bodyMedium,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = Icons.Default.DateRange,
//                    contentDescription = "Tanggal Pelaksanaan",
//                    modifier = Modifier.size(16.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = competition.tanggalPelaksanaan,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            // Display deadline if available
//            competition.tanggalTutupPendaftaran?.let { deadline ->
//                Spacer(modifier = Modifier.height(4.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Timer,
//                        contentDescription = "Batas Pendaftaran",
//                        modifier = Modifier.size(16.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
//                    val formattedDeadline = dateFormat.format(deadline.toDate())
//
//                    Text(
//                        text = "Batas pendaftaran: $formattedDeadline ${if(competition.autoCloseEnabled) "(Otomatis)" else ""}",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                // Format the timestamp
//                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//                val formattedDate = try {
//                    dateFormat.format(competition.createdAt.toDate())
//                } catch (e: Exception) {
//                    "Unknown date"
//                }
//
//                Text(
//                    text = formattedDate,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            // Display visibility status indicator
//            competition.visibilityStatus.let { status ->
//                Spacer(modifier = Modifier.height(4.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    val statusColor = when(status) {
//                        "Published" -> MaterialTheme.colorScheme.primary
//                        "Draft" -> MaterialTheme.colorScheme.tertiary
//                        "Cancelled" -> MaterialTheme.colorScheme.error
//                        else -> MaterialTheme.colorScheme.outline
//                    }
//
//                    Text(
//                        text = "• $status",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = statusColor
//                    )
//                }
//            }
//
//            // Display activity status indicator
//            competition.activityStatus.let { status ->
//                Spacer(modifier = Modifier.height(2.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    val statusColor = when(status) {
//                        "Active" -> MaterialTheme.colorScheme.primary
//                        "Inactive" -> MaterialTheme.colorScheme.error
//                        else -> MaterialTheme.colorScheme.outline
//                    }
//
//                    Text(
//                        text = "• $status",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = statusColor
//                    )
//                }
//            }
//
//            // Show cabang from the provided list that matches this competition
//            if (associatedCabangList.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(4.dp))
//                val cabangList = associatedCabangList.joinToString(", ") { it.namaCabang }
//                Text(
//                    text = "Cabang: $cabangList",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            // Display image if available
//            if (competition.imageUrl.isNotBlank()) {
//                Spacer(modifier = Modifier.height(8.dp))
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(competition.imageUrl)
//                        .error(R.drawable.ic_baseline_cancel_24)
//                        .build(),
//                    contentDescription = "Competition Image",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(150.dp)
//                        .clip(RoundedCornerShape(8.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Display file info if available
//            if (competition.fileUrl.isNotBlank()) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 4.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.FilePresent,
//                        contentDescription = "File Attached",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "File terlampir",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//        }
//    }
//}