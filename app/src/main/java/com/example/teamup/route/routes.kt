package com.example.teamup.route

sealed class Routes(val routes: String) {
    object TeamList : Routes("team_list")
    object AddTeam : Routes("add_team")
    object Login : Routes("login")
    object Register : Routes("register")
    object Verification : Routes("verification")
    object RegisterSuccess : Routes("register_success")
    object ForgotPassword : Routes("forgot_password")
    object ResetPassword : Routes("reset_password")
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