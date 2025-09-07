package com.example.teamup.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.teamup.common.theme.DodgerBlue
import com.example.teamup.common.theme.DodgerBlueShade
import com.example.teamup.common.theme.IceBlue
import com.example.teamup.common.theme.White
import com.example.teamup.route.NavigationItem

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
//        NavigationItem.Home,
        NavigationItem.HomeV5,
//        NavigationItem.Sail,
//        NavigationItem.Wishlist,
        NavigationItem.Competition,
        NavigationItem.TeamManagement,
        NavigationItem.Profile,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = White,
        contentColor = DodgerBlue,
        modifier = Modifier.height(57.dp) // Kurangi tinggi dari default (80dp) ke 56dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // Cek jika item adalah Profile, gunakan Icon Material, selain itu pakai painterResource
                    if (item == NavigationItem.Profile) {
                        Icon(
                            imageVector = Icons.Default.Person, // Icon user material
                            contentDescription = item.title,
                            modifier = Modifier
                                .width(18.dp)
                                .height(20.dp)
                        )
                    } else {
                        Icon(
                            modifier = Modifier
                                .width(18.dp)
                                .height(20.dp),
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title
                        )
                    }
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = DodgerBlueShade,
                    selectedIconColor = White,
                    unselectedIconColor = IceBlue
                )
            )
        }
    }
}