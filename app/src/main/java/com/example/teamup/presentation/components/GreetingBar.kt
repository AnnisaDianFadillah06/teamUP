package com.example.teamup.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamup.R
import com.example.teamup.common.theme.Dark
import com.example.teamup.common.theme.Transparant
import com.example.teamup.route.Routes

@Composable
fun GreetingBar(navController: NavController) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, 👋 Celvine",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            Text(
                text = "What would you like to learn today ? \nSearh Below",
                style = MaterialTheme.typography.bodySmall.copy(color = Dark)
            )
        }
        FilledIconButton(
            onClick = {
                navController.navigate(Routes.Cart.routes)
            },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Transparant)
        ) {
            Icon(
                modifier = Modifier.width(20.dp),
                painter = painterResource(id = R.drawable.cart_shopping),
                contentDescription = "Cart"
            )
        }
    }
}