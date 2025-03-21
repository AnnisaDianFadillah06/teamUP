package com.example.teamup.route

sealed class Routes(val routes: String) {
    object TeamManagement : Routes("team_management")
    object TeamList : Routes("team_list") //contoh firebase
    object AddTeam : Routes("add_team") //contoh firebase
    object Login : Routes("login")
    object Register : Routes("register")
    object Dashboard : Routes("dashboard")
    object Home : Routes("home")
    object Search : Routes("search")
    object Profile : Routes("profile")
    object Wishlist : Routes("wishlist")
    object Cart : Routes("cart")
    object MyCourse : Routes("my_courses")
    object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}