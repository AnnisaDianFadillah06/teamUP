// CustomBottomNavigationBar.kt
package com.example.teamup.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
fun CustomBottomNavigationBar(
    navController: NavController,
    onCompetitionClick: () -> Unit
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Sail,
        NavigationItem.Wishlist,
        NavigationItem.Competition,
        NavigationItem.Profile,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = White, contentColor = DodgerBlue) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (item == NavigationItem.Competition) {
                        // When in form and competition is clicked, just go back to competition screen
                        onCompetitionClick()
                    } else {
                        // For other tabs, navigate normally
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier
                            .width(18.dp)
                            .height(20.dp),
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
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