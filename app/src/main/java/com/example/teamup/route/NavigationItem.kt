package com.example.teamup.route

import com.example.teamup.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Home : NavigationItem(Routes.Home.routes, R.drawable.house_icon, "Home")
    data object Sail : NavigationItem(Routes.MyCourse.routes, R.drawable.sailboat_icon, "Sail")
    data object Competition : NavigationItem(Routes.Competition.routes, R.drawable.trophy, "Competition")
    data object Wishlist : NavigationItem(Routes.Wishlist.routes, R.drawable.seedling_icon, "Wishlist")
    data object Profile : NavigationItem(Routes.Profile.routes, R.drawable.captain_icon, "Profile")
}