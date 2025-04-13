package com.example.teamup.route

import com.example.teamup.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {

//    object Home : NavigationItem(Routes.Home.routes, R.drawable.house_icon, "Home")
//    object Sail : NavigationItem(Routes.MyCourse.routes, R.drawable.sailboat_icon, "Sail")
//    object Wishlist : NavigationItem(Routes.Wishlist.routes, R.drawable.seedling_icon, "Wishlist")
    object Profile : NavigationItem(Routes.Profile.routes, R.drawable.captain_icon, "Profile")
    object TeamManagement : NavigationItem(Routes.TeamManagement.routes, R.drawable.peopleteam, "Team Management")
    data object Competition : NavigationItem(Routes.Competition.routes, R.drawable.trophy, "Competition")
}
