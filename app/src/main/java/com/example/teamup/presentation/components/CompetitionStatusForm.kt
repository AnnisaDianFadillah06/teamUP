package com.example.teamup.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.teamup.data.model.CompetitionActivityStatus
import com.example.teamup.data.model.CompetitionModel
import com.example.teamup.data.model.CompetitionVisibilityStatus
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionStatusForm(
    competition: CompetitionModel?,
    onVisibilityStatusChange: (String) -> Unit,
    onActivityStatusChange: (String) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onAutoCloseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var visibilityExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    val visibilityOptions = CompetitionVisibilityStatus.values().map { it.value }
    val activityOptions = CompetitionActivityStatus.values().map { it.value }

    // Format date for display and editing
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    var deadlineString by remember {
        mutableStateOf(
            competition?.tanggalTutupPendaftaran?.toDate()?.let { dateFormat.format(it) } ?: ""
        )
    }

    var autoCloseEnabled by remember {
        mutableStateOf(competition?.autoCloseEnabled ?: false)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Pengaturan Status Kompetisi",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility Status Dropdown
            Text(
                text = "Status Tampilan",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = visibilityExpanded,
                onExpandedChange = { visibilityExpanded = it }
            ) {
                OutlinedTextField(
                    value = competition?.visibilityStatus ?: visibilityOptions.first(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = visibilityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = visibilityExpanded,
                    onDismissRequest = { visibilityExpanded = false }
                ) {
                    visibilityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onVisibilityStatusChange(option)
                                visibilityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Status Dropdown
            Text(
                text = "Status Aktivitas",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = activityExpanded,
                onExpandedChange = { activityExpanded = it }
            ) {
                OutlinedTextField(
                    value = competition?.activityStatus ?: activityOptions.first(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = activityExpanded,
                    onDismissRequest = { activityExpanded = false }
                ) {
                    activityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onActivityStatusChange(option)
                                activityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deadline date/time picker
            Text(
                text = "Tanggal Tutup Pendaftaran",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = deadlineString,
                onValueChange = {
                    deadlineString = it
                    onDeadlineChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DDThh:mm:ss") },
                supportingText = { Text("Format: YYYY-MM-DDThh:mm:ss") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-close checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = autoCloseEnabled,
                    onCheckedChange = {
                        autoCloseEnabled = it
                        onAutoCloseChange(it)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Aktifkan Auto-Close berdasarkan tanggal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Kompetisi akan otomatis menjadi non-aktif setelah tanggal tutup pendaftaran",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}