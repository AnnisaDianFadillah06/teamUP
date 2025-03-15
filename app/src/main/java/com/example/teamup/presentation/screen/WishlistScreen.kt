package com.example.teamup.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.UiState
import com.example.teamup.common.theme.White2
import com.example.teamup.data.model.CourseModel
import com.example.teamup.data.model.WishlistModel
import com.example.teamup.data.viewmodels.DetailViewModel
import com.example.teamup.data.viewmodels.ViewModelDetailFactory
import com.example.teamup.data.viewmodels.ViewModelWishListFactory
import com.example.teamup.data.viewmodels.WishlistViewModel
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.SimpleCardCourse
import com.example.teamup.presentation.components.TopBar
import com.example.teamup.route.Routes

@Composable
fun WishlistScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    viewModel: WishlistViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelWishListFactory(
            Injection.provideWishlistRepository()
        )
    ),
    detailViewModel: DetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelDetailFactory(
            Injection.provideDetailRepository()
        )
    ),
) {
    Column {
        TopBar(title = "Wishlist", icon = R.drawable.cart_shopping, onClick = {
            navController.navigate(Routes.Cart.routes)
        })
        viewModel.uiState.collectAsState(initial = UiState.Loading).value.let { uiState ->
            when (uiState) {
                is UiState.Loading -> {
                    viewModel.getAddedWishlist()
                }
                is UiState.Success -> {
                    if (uiState.data.wishlist.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .background(color = White2)
                                .fillMaxHeight()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = paddingValues.calculateBottomPadding()
                                ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.data.wishlist.size, key = { it }) {
                                WishListItem(
                                    detailViewModel = detailViewModel,
                                    navController = navController,
                                    wishlist = uiState.data.wishlist[it]
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(text = stringResource(id = R.string.error))
                }
            }
        }

    }
}

@Composable
fun WishListItem(
    detailViewModel: DetailViewModel,
    navController: NavController,
    wishlist: WishlistModel
) {
    val item: CourseModel = detailViewModel.getDataById(wishlist.Id)
    SimpleCardCourse(item = item, navController = navController)
}