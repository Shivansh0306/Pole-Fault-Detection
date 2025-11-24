package com.ksebl.comkseblfaultapp.ui.screen

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel
import com.ksebl.comkseblfaultapp.model.Fault
import com.ksebl.comkseblfaultapp.model.Node
import com.ksebl.comkseblfaultapp.ui.components.FaultPopupNotification
import com.ksebl.comkseblfaultapp.ui.components.MapComponent
import com.ksebl.comkseblfaultapp.ui.components.NotificationPanel
import java.util.Calendar


// Added minimal supporting types used by the UI
private enum class FaultStatus(val displayName: String) { ACTIVE("Active"), IN_PROGRESS("In Progress"), RESOLVED("Resolved") }
private data class StatItem(val value: String, val label: String, val icon: ImageVector, val color: Color)
private data class ActionItem(val label: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)
private fun getCurrentHourCompat(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)


// Enhanced Dashboard Screen with modern UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf("Overview", "Faults", "Map")
    val faults by mainViewModel.faults.collectAsState()
    val stats by mainViewModel.stats.collectAsState()
    val error by mainViewModel.error.collectAsState()
    val newFaultAlert by mainViewModel.newFaultAlert.collectAsState()
    
    // Notification panel state
    var showNotificationPanel by remember { mutableStateOf(false) }

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        try {
            mainViewModel.fetchFaults()
            mainViewModel.fetchStats()
            mainViewModel.fetchNodes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with gradient border
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "HS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Welcome back 👋",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Harkirat",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Enhanced notification button
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shadowElevation = 0.dp
                        ) {
                            IconButton(
                                onClick = { showNotificationPanel = !showNotificationPanel }
                            ) {
                                BadgedBox(badge = {
                                    val activeFaultCount = faults.count { it.status == "ACTIVE" }
                                    if (activeFaultCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                        ) {
                                            Text(
                                                text = if (activeFaultCount > 99) "99+" else activeFaultCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Enhanced Tab Row
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { },
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .height(4.dp)
                                        .padding(horizontal = 24.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTabIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            "Home",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTabIndex == 1) Icons.Filled.Warning else Icons.Outlined.Warning,
                            contentDescription = "Faults",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            "Faults",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedTabIndex == 2) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "Map",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            "Map",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == 2) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    alwaysShowLabel = true
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "tab_content"
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewTab(navController = navController, faults = faults)
                    1 -> FaultsTab(navController = navController, faults = faults)
                    2 -> MapTab(nodes = mainViewModel.nodes.collectAsState().value)
                }
            }

            if (error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        TextButton(
                            onClick = {
                                mainViewModel.fetchFaults()
                                mainViewModel.fetchStats()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                ) { Text(error ?: "") }
            }
        }
    }

    // Show popup notification for new faults
    FaultPopupNotification(
        fault = newFaultAlert,
        onDismiss = { mainViewModel.dismissFaultAlert() },
        onViewDetails = { faultId ->
            mainViewModel.dismissFaultAlert()
            selectedTabIndex = 1 // Switch to Faults tab
        }
    )
    
    // Show notification panel
    NotificationPanel(
        isVisible = showNotificationPanel,
        faults = faults,
        onDismiss = { showNotificationPanel = false },
        onFaultClick = { faultId ->
            showNotificationPanel = false
            selectedTabIndex = 1 // Switch to Faults tab
            // TODO: Scroll to specific fault in the list
        },
        onMarkAllRead = {
            mainViewModel.markAllFaultsAsRead()
            showNotificationPanel = false
        }
    )
}

@Composable
private fun OverviewTab(navController: NavController, faults: List<Fault>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { GreetingCard(faults) }
        item { QuickActions(navController) }
        item { StatsOverview(faults) }
        item {
            SectionHeader(
                title = "Recent Faults",
                actionText = "See All",
                onActionClick = { navController.navigate("staff_dashboard/1") }
            )
        }
        items(faults.take(3)) { fault ->
            FaultItem(
                fault = fault,
                onClick = { navController.navigate("fault/${fault.id}") }
            )
        }
    }
}

@Composable
private fun StatsOverview(faults: List<Fault>) {
    val stats = listOf(
        StatItem(
            value = faults.count { it.status.equals("ACTIVE", ignoreCase = true) }.toString(),
            label = FaultStatus.ACTIVE.displayName,
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.error
        ),
        StatItem(
            value = faults.count { it.status.equals("IN_PROGRESS", ignoreCase = true) }.toString(),
            label = FaultStatus.IN_PROGRESS.displayName,
            icon = Icons.Default.Build,
            color = MaterialTheme.colorScheme.primary
        ),
        StatItem(
            value = faults.count { it.status.equals("RESOLVED", ignoreCase = true) }.toString(),
            label = FaultStatus.RESOLVED.displayName,
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF10B981)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Fault Overview",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.forEach { stat ->
                    StatItemContent(
                        value = stat.value,
                        label = stat.label,
                        icon = stat.icon,
                        color = stat.color
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItemContent(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stat_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stat_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(if (label == "Active") scale else 1f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GreetingCard(faults: List<Fault>) {
    val currentHour = getCurrentHourCompat()
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val activeFaults = faults.count { it.status.equals("ACTIVE", ignoreCase = true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$greeting!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (activeFaults > 0)
                            "You have $activeFaults active fault${if (activeFaults != 1) "s" else ""} to check"
                        else
                            "All systems running smoothly",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activeFaults > 0) Icons.Outlined.Warning else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActions(navController: NavController) {
    val actions = listOf(
        ActionItem("Report", Icons.Outlined.AddAlert, MaterialTheme.colorScheme.error) {
            navController.navigate("report_fault")
        },
        ActionItem("Scan QR", Icons.Outlined.QrCodeScanner, MaterialTheme.colorScheme.tertiary) {
            // Handle scan QR
        },
        ActionItem("History", Icons.Outlined.History, MaterialTheme.colorScheme.primary) {
            navController.navigate("history")
        },
        ActionItem("Settings", Icons.Outlined.Settings, MaterialTheme.colorScheme.secondary) {
            // Handle settings
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            QuickActionButton(
                text = action.label,
                icon = action.icon,
                color = action.color,
                onClick = action.onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .background(
                color = color.copy(alpha = 0.08f)
            )
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FaultItem(
    fault: Fault,
    onClick: () -> Unit
) {
    val statusColor = when (fault.status.uppercase()) {
        "ACTIVE" -> MaterialTheme.colorScheme.error
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
        "RESOLVED" -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusContainerColor = when (fault.status.uppercase()) {
        "ACTIVE" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        "RESOLVED" -> Color(0xFF10B981).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val statusTextColor = when (fault.status.uppercase()) {
        "ACTIVE" -> MaterialTheme.colorScheme.error
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
        "RESOLVED" -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val icon = when (fault.status.uppercase()) {
        "ACTIVE" -> Icons.Filled.Warning
        "IN_PROGRESS" -> Icons.Filled.Build
        "RESOLVED" -> Icons.Filled.CheckCircle
        else -> Icons.Filled.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = fault.faultType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Node ${fault.nodeId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusContainerColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = fault.status.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = fault.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = fault.reportedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaultsTab(navController: NavController, faults: List<Fault>) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Faults",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${faults.size}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Sort button
                        var showSortMenu by remember { mutableStateOf(false) }
                        var selectedSortOption by remember { mutableStateOf("Recent") }

                        Box {
                            Surface(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        selectedSortOption,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (showSortMenu) Icons.Outlined.ArrowDropUp
                                        else Icons.Outlined.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                listOf("Recent", "Oldest", "Severity", "Status").forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selectedSortOption == option)
                                                    FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSortOption == option)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            selectedSortOption = option
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (selectedSortOption == option) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier.animateContentSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(faults) { fault ->
                FaultItem(fault = fault) {
                    navController.navigate("fault/${fault.id}")
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Enhanced FAB with animation
        var fabExpanded by remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { navController.navigate("report_fault") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomEnd)
                .size(64.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Report Fault",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun MapTab(nodes: List<Node> = emptyList()) {
    val (selectedLocation, setSelectedLocation) = remember { mutableStateOf<Pair<Double, Double>?>(null) }
    MapComponent(
        selectedLocation = selectedLocation,
        onLocationSelected = { lat, lng -> setSelectedLocation(lat to lng) },
        modifier = Modifier.fillMaxSize(),
        nodes = nodes
    )
}
