//package com.example.teamup.presentation.screen
//
//import android.app.DatePickerDialog
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import com.example.teamup.R
//import com.example.teamup.data.model.CompetitionModel
//import com.example.teamup.data.viewmodels.CompetitionViewModel
//import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
//import com.example.teamup.di.Injection
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.ktx.storage
//import java.text.SimpleDateFormat
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddCompetitionScreen(
//    navController: NavHostController,
//    viewModel: CompetitionViewModel
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Buat Kompetisi Baru") },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimary
//                ),
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = MaterialTheme.colorScheme.onPrimary
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        AddCompetitionForm(
//            viewModel = viewModel,
//            onSuccess = { navController.popBackStack() },
//            modifier = Modifier.padding(paddingValues)
//        )
//    }
//}