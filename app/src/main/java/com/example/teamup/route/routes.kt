package com.example.teamup.route

sealed class Routes(val routes: String) {
    object SplashScreen : Routes("splash_screen")
    object TeamManagement : Routes("team_management")
    object FormAddTeam : Routes("form_add_team")
    object JoinTeam : Routes("join_team")
    object CategoryTeams : Routes("category_team")
    object TeamList : Routes("team_list") //contoh firebase
    object AddTeam : Routes("add_team") //contoh firebase
    object TeamListCategory : Routes("team_list")
    object Notifications : Routes("notification")

    data object Login : Routes("login")
    data object Register : Routes("register")
    data object FingerprintLogin : Routes("fingerprint_login")
    data object LoginV5 : Routes("login_v5")
    data object Dashboard : Routes("dashboard_graph") // Diubah menjadi graph route

    // Dashboard routes
    data object Home : Routes("home")
    data object HomeV5 : Routes("homev5")
    data object Search : Routes("search")
    data object Profile : Routes("profile")
    data object CompleteProfile : Routes("complete_profile")
    data object CompetitionDetail : Routes("competition_detail/{competitionId}") {
        fun createRoute(competitionId: String) = "competition_detail/$competitionId"
    }

    data object ProfileSettings : Routes("profile_settings") // Pastikan nama route sesuai
    data object Competition : Routes("competition")
    data object Wishlist : Routes("wishlist")
    data object Cart : Routes("cart")
    data object MyCourse : Routes("my_course")
    data object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
    object Verification : Routes("verification")
    object CekEmail : Routes("cek_email/{email}") {
        fun createRoute(email: String) = "cek_email/$email"
    }
    object RegisterSuccess : Routes("register_success")
    object ForgotPassword : Routes("forgot_password")
    object ResetPassword : Routes("reset_password")
    object TeamDetailGrup : Routes("team_detail/{teamId}/{isJoined}/{isFull}") {
        fun createRoute(teamId: String, isJoined: Boolean, isFull: Boolean) =
            "team_detail/$teamId/$isJoined/$isFull"
    }
    object Invite : Routes("invite_member")
    object ChatGroup : Routes("chat_group/{teamId}/{teamName}") {
        fun createRoute(teamId: String, teamName: String) =
            "chat_group/$teamId/$teamName"
    }
    object InviteSelect : Routes("invite_select")
    object DraftSelectMember {
        const val routes = "draft_select_member/{teamId}/{teamName}"
        fun createRoute(teamId: String, teamName: String): String {
            return "draft_select_member/$teamId/$teamName"
        }
    }
}
