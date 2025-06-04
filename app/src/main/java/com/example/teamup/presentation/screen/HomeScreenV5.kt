package com.example.teamup.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamup.common.theme.White2
import com.example.teamup.data.viewmodels.CompetitionViewModel
import com.example.teamup.data.viewmodels.CompetitionViewModelFactory
import com.example.teamup.di.Injection
import com.example.teamup.presentation.components.SearchField
import com.example.teamup.route.Routes
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreenV5(
    paddingValues: PaddingValues,
    navController: NavController,
    // Tambah CompetitionViewModel
    competitionViewModel: CompetitionViewModel = viewModel(
        factory = CompetitionViewModelFactory(
            Injection.provideCompetitionRepository(),
            Injection.provideCabangLombaRepository()
        )
    )
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White2),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
    ) {
        item {
            Column {
                // Custom Header (seperti WhatsApp style)
                WhatsAppStyleHeader(navController = navController)

                Column(modifier = Modifier.padding(16.dp)) {
                    // Search Field
                    SearchField(
                        placeholder = "Search competitions, teams...",
                        enable = false,
                        onClick = {
                            navController.navigate(Routes.Search.routes)
                        },
                        value = ""
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Featured Banner Carousel
                    FeaturedBannerSection()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick Access Menu (tanpa Profile)
                    QuickAccessSection(navController = navController)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            // Update Active Competitions Section jadi dinamis
            ActiveCompetitionsSection(
                navController = navController,
                competitionViewModel = competitionViewModel
            )

            Spacer(modifier = Modifier.height(20.dp))

            // My Teams tetap static dulu (sesuai request)
            MyTeamsSection(navController = navController)

            Spacer(modifier = Modifier.height(20.dp))

            RecentActivitiesSection(navController = navController)
        }
    }
}

@Composable
fun WhatsAppStyleHeader(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TeamUp Title (seperti WhatsApp)
            Text(
                text = "TeamUp",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Notification Bell dengan Badge
            Box {
                IconButton(
                    onClick = { navController.navigate(Routes.Notifications.routes) }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Badge notification
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedBannerSection() {
    val bannerItems = listOf(
        TeamUpBanner(
            emoji = "🏆",
            title = "Kompetisi Terbaru!",
            subtitle = "Bergabung dalam lomba programming",
            color = Color(0xFF1976D2)
        ),
        TeamUpBanner(
            emoji = "👥",
            title = "Team Management",
            subtitle = "Kelola tim mu dengan mudah",
            color = Color(0xFF388E3C)
        ),
        TeamUpBanner(
            emoji = "🎯",
            title = "Achievement System",
            subtitle = "Raih badge & tingkatkan level",
            color = Color(0xFFF57C00)
        )
    )

    val pagerState = rememberPagerState()

    Column {
        Text(
            text = "Featured",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalPager(
            count = bannerItems.size,
            state = pagerState,
            modifier = Modifier.height(140.dp)
        ) { page ->
            BannerCard(
                banner = bannerItems[page],
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun BannerCard(
    banner: TeamUpBanner,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle banner click */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            banner.color,
                            banner.color.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = banner.emoji,
                    fontSize = 36.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Column {
                    Text(
                        text = banner.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessSection(navController: NavController) {
    // Hapus Profile dari menu items
    val menuItems = listOf(
        QuickAccessItem(
            title = "Competition",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFFE53935),
            route = Routes.Competition.routes
        ),
        QuickAccessItem(
            title = "Teams",
            icon = Icons.Default.Group,
            color = Color(0xFF1E88E5),
            route = Routes.TeamManagement.routes
        ),
        QuickAccessItem(
            title = "Join Team",
            icon = Icons.Default.PersonAdd,
            color = Color(0xFF43A047),
            route = Routes.JoinTeam.routes
        )
        // Profile dihapus sesuai request
    )

    Column {
        Text(
            text = "Quick Access",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                QuickAccessCard(
                    item = item,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    item: QuickAccessItem,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(100.dp)
            .clickable { navController.navigate(item.route) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        item.color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// Update ActiveCompetitionsSection jadi dinamis
@Composable
fun ActiveCompetitionsSection(
    navController: NavController,
    competitionViewModel: CompetitionViewModel
) {
    // Perbaiki collectAsState dan getValue
    val uiState by competitionViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Competitions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = {
                navController.navigate(Routes.Competition.routes)
            }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Handle state dari CompetitionViewModel.CompetitionUiState
        when {
            uiState.isLoading -> {
                // Trigger refresh data saat pertama kali load
                LaunchedEffect(Unit) {
                    competitionViewModel.refreshData()
                }

                // Loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            uiState.errorMessage != null -> {
                // Error state
                ErrorCompetitionCard(
                    onRetry = { competitionViewModel.refreshData() },
                    onClick = { navController.navigate(Routes.Competition.routes) }
                )
            }

            else -> {
                // Success state
                val competitions = uiState.competitions

                if (competitions.isNotEmpty()) {
                    // Ambil max 2 kompetisi terbaru untuk home screen
                    competitions.take(2).forEach { competition ->
                        CompetitionCard(
                            title = competition.namaLomba ?: "Competition",
                            subtitle = "Deadline: ${competition.tanggalPelaksanaan ?: "TBA"}",
                            participants = "Competition available",
                            onClick = {
                                navController.navigate(Routes.Competition.routes)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // Empty state
                    EmptyCompetitionCard(
                        onClick = { navController.navigate(Routes.Competition.routes) }
                    )
                }
            }
        }
    }
}

// Empty state card
@Composable
fun EmptyCompetitionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "No competitions",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No active competitions yet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Text(
                text = "Tap to create or join competitions",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Error state card
@Composable
fun ErrorCompetitionCard(
    onRetry: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Failed to load competitions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tap to retry",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onRetry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MyTeamsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Teams",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = {
                navController.navigate(Routes.TeamManagement.routes)
            }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mock team cards
        TeamCard(
            teamName = "Code Warriors",
            memberCount = "4/5 members",
            status = "Active",
            onClick = { navController.navigate(Routes.TeamManagement.routes) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TeamCard(
            teamName = "Design Masters",
            memberCount = "3/4 members",
            status = "Recruiting",
            onClick = { navController.navigate(Routes.TeamManagement.routes) }
        )
    }
}

@Composable
fun RecentActivitiesSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recent Activities",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ActivityCard(
            title = "New team invitation",
            subtitle = "From Code Warriors team",
            icon = Icons.Default.Group,
            iconColor = Color(0xFF4CAF50),
            onClick = { navController.navigate(Routes.Notifications.routes) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActivityCard(
            title = "Competition reminder",
            subtitle = "Programming contest deadline approaching",
            icon = Icons.Default.NotificationsActive,
            iconColor = Color(0xFFFF9800),
            onClick = { navController.navigate(Routes.Competition.routes) }
        )
    }
}

@Composable
fun CompetitionCard(
    title: String,
    subtitle: String,
    participants: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFE53935).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Competition",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = participants,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TeamCard(
    teamName: String,
    memberCount: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF1E88E5).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Team",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teamName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = memberCount,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = if (status == "Active") Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ActivityCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Data classes
data class TeamUpBanner(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val color: Color
)

data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)