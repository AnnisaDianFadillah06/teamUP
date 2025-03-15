package com.example.teamup.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.UiState
import com.example.teamup.common.theme.Dark
import com.example.teamup.common.theme.White
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.CourseModel
import com.example.teamup.data.viewmodels.DetailViewModel
import com.example.teamup.data.viewmodels.MyCoursesViewModel
import com.example.teamup.data.viewmodels.ViewModelDetailFactory
import com.example.teamup.data.viewmodels.ViewModelMyCoursesFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.CardCourse
import com.example.teamup.route.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    detailViewModel: DetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelDetailFactory(
            Injection.provideDetailRepository()
        )
    ),
    myCourseViewModel: MyCoursesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelMyCoursesFactory(
            Injection.provideMyCoursesRepository(),
            Injection.provideCartRepository()
        )
    ),
) {
    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = White),
            title = {
                Text(
                    text = stringResource(id = R.string.my_course),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Dark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            actions = {
                Row {
                    IconButton(onClick = {
                        navController.navigate(Routes.Search.routes) {
                            popUpTo(Routes.Home.routes) {
                                inclusive = true
                            }
                        }
                    }) {
                        Icon(
                            modifier = Modifier.width(18.dp),
                            painter = painterResource(id = R.drawable.search_icon),
                            contentDescription = stringResource(id = R.string.search_icon),
                            tint = Dark
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Routes.Cart.routes) {
                            popUpTo(Routes.Home.routes) {
                                inclusive = true
                            }
                        }
                    }) {
                        Icon(
                            modifier = Modifier.width(18.dp),
                            painter = painterResource(id = R.drawable.cart_shopping),
                            contentDescription = stringResource(id = R.string.cart_icon),
                            tint = Dark
                        )
                    }
                }
            })
    }) { p ->
        val x = p
        myCourseViewModel.uiState.collectAsState(initial = UiState.Loading).value.let { uiState ->
            when (uiState) {
                is UiState.Loading -> {
                    myCourseViewModel.getMyCourses()
                }
                is UiState.Success -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = White2),
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = paddingValues.calculateBottomPadding(),
                            top = p.calculateTopPadding() + 10.dp
                        ),
                        content = {
                            items(uiState.data.myCoursesList.size, key = { it }) {
                                val context = LocalContext.current
                                val data: CourseModel =
                                    detailViewModel.getDataById(uiState.data.myCoursesList[it].Id)
                                CardCourse(
                                    modifier = Modifier.fillMaxWidth(),
                                    item = data,
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            R.string.in_dev,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            }
                        },
                    )
                }
                is UiState.Error -> {
                    Text(text = stringResource(id = R.string.error))
                }
            }
        }
    }

}