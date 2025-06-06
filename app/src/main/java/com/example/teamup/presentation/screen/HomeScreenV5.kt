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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
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
    val uiState by competitionViewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Pull-to-refresh state dengan refresh yang lebih cepat
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            isRefreshing = true
            competitionViewModel.refreshData()
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = White2),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp // Tambah padding bottom
            )
        ) {
            item {
                Column {
                    // Custom Header dengan Notification Badge (Red Dot Only)
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

                        // Featured Banner Carousel with Auto-Slide + Loop
                        FeaturedBannerSection()

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Access Menu dengan Create Competition dan Fix Text
                        QuickAccessSection(navController = navController)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Update Active Competitions Section dengan refresh yang benar
                ActiveCompetitionsSection(
                    navController = navController,
                    competitionViewModel = competitionViewModel,
                    isRefreshing = isRefreshing,
                    onRefreshComplete = { isRefreshing = false }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Comment My Teams Section sesuai request
                // MyTeamsSection(navController = navController)

                // Ganti Recent Activities jadi Statistics Section
                StatisticsSection(navController = navController)

                // Tambah spacer untuk scroll yang proper
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppStyleHeader(navController: NavController) {
    // Simple red dot notification (ganti badge number jadi red dot)
    val hasNotifications = true // Mock - nanti bisa dari ViewModel

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

            // Notification Bell dengan Red Dot (bukan badge number)
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

                // Simple red dot
                if (hasNotifications) {
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

    // Auto-slide carousel dengan infinite loop
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000) // 3 detik delay
            val nextPage = (pagerState.currentPage + 1) % bannerItems.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

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
    // Tambah Create Competition + Fix text dengan maxLines dan overflow
    val menuItems = listOf(
        QuickAccessItem(
            title = "Competitions",
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
        ),
        QuickAccessItem(
            title = "Create",
            icon = Icons.Default.Add,
            color = Color(0xFF9C27B0),
            route = Routes.Competition.routes // Navigate ke create competition
        )
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
            .width(85.dp) // Sedikit lebih lebar untuk teks yang lebih panjang
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
                .padding(10.dp), // Kurangi padding sedikit
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                fontSize = 9.sp, // Kurangi font size sedikit
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2, // Allow 2 lines
                overflow = TextOverflow.Ellipsis,
                lineHeight = 10.sp
            )
        }
    }
}

// Update ActiveCompetitionsSection dengan refresh yang benar
@Composable
fun ActiveCompetitionsSection(
    navController: NavController,
    competitionViewModel: CompetitionViewModel,
    isRefreshing: Boolean,
    onRefreshComplete: () -> Unit
) {
    val uiState by competitionViewModel.uiState.collectAsState()

    // Effect untuk handle refresh completion yang lebih responsive
    LaunchedEffect(uiState.isLoading, isRefreshing) {
        if (!uiState.isLoading && isRefreshing) {
            delay(500) // Small delay untuk UX yang smooth
            onRefreshComplete()
        }
    }

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

        // Handle state dengan loading yang lebih responsive
        when {
            uiState.isLoading && !isRefreshing -> {
                // Trigger refresh data saat pertama kali load
                LaunchedEffect(Unit) {
                    competitionViewModel.refreshData()
                }

                // Shimmer loading cards
                repeat(2) {
                    CompetitionCardShimmer()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            uiState.errorMessage != null -> {
                ErrorCompetitionCard(
                    onRetry = {
                        competitionViewModel.refreshData()
                    },
                    onClick = { navController.navigate(Routes.Competition.routes) }
                )
            }

            else -> {
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

// Shimmer Loading Card untuk Competition
@Composable
fun CompetitionCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shimmer circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Shimmer title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .placeholder(
                            visible = true,
                            highlight = PlaceholderHighlight.shimmer()
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Shimmer subtitle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .placeholder(
                            visible = true,
                            highlight = PlaceholderHighlight.shimmer()
                        )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Shimmer participants
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(10.dp)
                        .placeholder(
                            visible = true,
                            highlight = PlaceholderHighlight.shimmer()
                        )
                )
            }

            // Shimmer chevron
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    )
            )
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "No competitions",
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No active competitions yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to create or join competitions",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
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

// Statistics Section dengan padding bottom yang cukup
@Composable
fun StatisticsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Platform Statistics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Universities
            StatisticCard(
                title = "Universities",
                value = "50+",
                subtitle = "Registered",
                icon = Icons.Default.School,
                iconColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.Competition.routes) }
            )

            // Total Members
            StatisticCard(
                title = "Members",
                value = "1.2K+",
                subtitle = "Active users",
                icon = Icons.Default.People,
                iconColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.TeamManagement.routes) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Competitions
            StatisticCard(
                title = "Competitions",
                value = "25",
                subtitle = "This month",
                icon = Icons.Default.EmojiEvents,
                iconColor = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.Competition.routes) }
            )

            // Success Rate
            StatisticCard(
                title = "Success Rate",
                value = "85%",
                subtitle = "Team formation",
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.TeamManagement.routes) }
            )
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
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