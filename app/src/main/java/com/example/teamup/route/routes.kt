package com.example.teamup.route

sealed class Routes(val routes: String) {

    object TeamManagement : Routes("team_management")
    object FormAddTeam : Routes("form_add_team")
    object JoinTeam : Routes("join_team")
    object TeamDetail : Routes("team_detail")
    object CategoryTeams : Routes("category_team")
    object TeamList : Routes("team_list") //contoh firebase
    object AddTeam : Routes("add_team") //contoh firebase

    data object Login : Routes("login")
    data object Register : Routes("register")
    data object FingerprintLogin : Routes("fingerprint_login")
    data object LoginV5 : Routes("login_v5")
    data object Dashboard : Routes("dashboard_graph") // Diubah menjadi graph route

    // Dashboard routes
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Profile : Routes("profile")
    data object Competition : Routes("competition")
    data object Wishlist : Routes("wishlist")
    data object Cart : Routes("cart")
    data object MyCourse : Routes("my_course")
    data object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
    object Verification : Routes("verification")
    object RegisterSuccess : Routes("register_success")
    object ForgotPassword : Routes("forgot_password")
    object ResetPassword : Routes("reset_password")
}
