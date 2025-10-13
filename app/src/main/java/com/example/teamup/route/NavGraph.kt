
//package com.example.teamup.route
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import com.example.teamup.presentation.screen.*
//import com.example.teamup.presentation.screen.register.CekEmailScreen
//import com.example.teamup.presentation.screen.register.RegisterScreen
//import com.example.teamup.presentation.screen.register.VerificationScreen
//import com.example.teamup.presentation.screen.register.RegisterSuccessScreen
//@Composable
//fun NavGraph(startDestination: String = Routes.Register.routes) {
//    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = startDestination) {
//        composable(Routes.Register.routes) { RegisterScreen(navController = navController) }
//        composable(Routes.Login.routes) { LoginScreen(navController = navController) }
//        composable(Routes.ForgotPassword.routes) { ForgotPasswordScreen(navController = navController) }
//        composable(Routes.ResetPassword.routes) { ResetPasswordScreen(navController = navController) }
//
//        composable(Routes.Verification.routes) { VerificationScreen(navController = navController) }
//        composable(Routes.RegisterSuccess.routes) { RegisterSuccessScreen(navController = navController) }
//        composable(Routes.Profile.routes) { ProfileScreen(navController = navController) }
//
//        composable(
//            route = Routes.CekEmail.routes,
//            arguments = listOf(
//                navArgument("email") {
//                    type = NavType.StringType
//                }
//            )
//        ) { backStackEntry ->
//            val email = backStackEntry.arguments?.getString("email") ?: ""
//            CekEmailScreen(navController, email = email)
//        }
//        // Tambahkan route lain jika diperlukan
//    }
//}
//
