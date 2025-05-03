package com.example.teamup.route

sealed class Routes(val routes: String) {
    object SplashScreen : Routes("splash_screen")
    object TeamManagement : Routes("team_management")
    object FormAddTeam : Routes("form_add_team")
    object JoinTeam : Routes("join_team")
    object TeamDetail : Routes("team_detail")
    object CategoryTeams : Routes("category_team")
    object TeamListCategory : Routes("team_list")
    object Notifications : Routes("notification")

    data object Login : Routes("login")
    data object Register : Routes("register")
    data object FingerprintLogin : Routes("fingerprint_login")
    data object LoginV5 : Routes("login_v5")
    data object Dashboard : Routes("dashboard_graph") // Diubah menjadi graph route

    // Dashboard routes
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Profile : Routes("profile")
    object CompleteProfile : Routes("complete_profile")
    object ProfileSettings : Routes ("profile_settings" )
    data object Competition : Routes("competition")
    data object Wishlist : Routes("wishlist")
    data object Cart : Routes("cart")
    data object MyCourse : Routes("my_course")
    data object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}