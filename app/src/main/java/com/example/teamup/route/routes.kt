package com.example.teamup.route

sealed class Routes(val routes: String) {
    data object TeamList : Routes("team_list")
    data object AddTeam : Routes("add_team")
    data object Login : Routes("login")
    object LoginV5 : Routes("login_v5")
    data object Register : Routes("register")
    data object FingerprintLogin : Routes("fingerprint_login")
    data object Dashboard : Routes("dashboard")
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Profile : Routes("profile")
    data object Wishlist : Routes("wishlist")
    data object Cart : Routes("cart")
    data object MyCourse : Routes("my_courses")
    data object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}