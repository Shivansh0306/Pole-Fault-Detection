package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ksebl.comkseblfaultapp.ui.components.FaultPopupNotification
import com.ksebl.comkseblfaultapp.ui.components.NotificationPanel
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext

// Citizen Dashboard - PowerWatch Citizen/User UI (Simplified)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    navController: NavController, 
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Report", "Status")
    val newFaultAlert by mainViewModel.newFaultAlert.collectAsState()
    val faults by mainViewModel.faults.collectAsState()
    
    // Notification panel state
    var showNotificationPanel by remember { mutableStateOf(false) }
    
    // Auto-refresh faults for citizens too
    LaunchedEffect(Unit) {
        mainViewModel.fetchFaults()
        mainViewModel.fetchNodes()
        
        // Refresh every 30 seconds for real-time updates
        while (true) {
            kotlinx.coroutines.delay(30000) // 30 seconds
            mainViewModel.fetchFaults()
        }
    }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "PowerWatch",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            // Notification button
                            val activeFaultCount = faults.count { it.status == "ACTIVE" }
                            IconButton(
                                onClick = { showNotificationPanel = !showNotificationPanel }
                            ) {
                                BadgedBox(badge = {
                                    if (activeFaultCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
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
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = when(index) {
                                            0 -> if (selectedTab == 0) Icons.Filled.Edit else Icons.Outlined.Edit
                                            else -> if (selectedTab == 1) Icons.Filled.List else Icons.Outlined.List
                                        },
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }
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
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "citizen_tab"
            ) { targetTab ->
                when (targetTab) {
                    0 -> CitizenReportScreen(navController, mainViewModel)
                    1 -> CitizenStatusScreen(navController, mainViewModel)
                }
            }
        }
    }

    // Show popup notification for new faults
    FaultPopupNotification(
        fault = newFaultAlert,
        onDismiss = { mainViewModel.dismissFaultAlert() },
        onViewDetails = { faultId ->
            mainViewModel.dismissFaultAlert()
            selectedTab = 1 // Switch to Status tab
        }
    )
    
    // Show notification panel
    NotificationPanel(
        isVisible = showNotificationPanel,
        faults = faults,
        onDismiss = { showNotificationPanel = false },
        onFaultClick = { faultId ->
            showNotificationPanel = false
            selectedTab = 1 // Switch to Status tab
        },
        onMarkAllRead = {
            mainViewModel.markAllFaultsAsRead()
            showNotificationPanel = false
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitizenReportScreen(
    navController: NavController,
    mainViewModel: MainViewModel = koinViewModel()
) {
    var faultType by remember { mutableStateOf("Power Cut") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedNodeId by remember { mutableLongStateOf(1L) } // Default node
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val nodes by mainViewModel.nodes.collectAsState()
    val error by mainViewModel.error.collectAsState()
    
    // Fetch nodes when the screen loads
    LaunchedEffect(Unit) {
        mainViewModel.fetchNodes()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Report Fault",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Fault Type Dropdown
                    Text(
                        "Fault Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = faultType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Power Cut", "Broken Pole", "Transformer Issue", "Sparking Wire", "Other").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        faultType = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Node Selection
                    Text(
                        "Electrical Node/Area",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (nodes.isNotEmpty()) {
                        var nodeExpanded by remember { mutableStateOf(false) }
                        val selectedNode = nodes.find { it.id.toLongOrNull() == selectedNodeId }
                        
                        ExposedDropdownMenuBox(
                            expanded = nodeExpanded,
                            onExpandedChange = { nodeExpanded = !nodeExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedNode?.name ?: "Node $selectedNodeId",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nodeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = nodeExpanded,
                                onDismissRequest = { nodeExpanded = false }
                            ) {
                                nodes.forEach { node ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(node.name)
                                                Text(
                                                    "Status: ${node.status}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedNodeId = node.id.toLongOrNull() ?: 1L
                                            nodeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = "Loading nodes...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        "Description",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe the issue briefly") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Upload Photo placeholder
                    Text(
                        "Upload Photo (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.AddAPhoto,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap to upload photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Location
                    Text(
                        "Location",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var customLocation by remember { mutableStateOf("") }
                    var useCurrentLocation by remember { mutableStateOf(true) }
                    
                    Column {
                        // Toggle for location type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = useCurrentLocation,
                                onCheckedChange = { useCurrentLocation = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (useCurrentLocation) "Use current location" else "Enter custom location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = if (useCurrentLocation) "Auto-detected: Current Location" else customLocation,
                            onValueChange = { if (!useCurrentLocation) customLocation = it },
                            readOnly = useCurrentLocation,
                            placeholder = { Text("Enter location details") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            enabled = !useCurrentLocation,
                            leadingIcon = {
                                Icon(
                                    if (useCurrentLocation) Icons.Outlined.LocationOn else Icons.Outlined.Edit,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit Button
                    Button(
                        onClick = {
                            if (description.isNotBlank() && !isSubmitting) {
                                isSubmitting = true
                                val reportRequest = com.ksebl.comkseblfaultapp.model.request.ReportFaultRequest(
                                    nodeId = selectedNodeId,
                                    description = "$faultType: $description",
                                    confidence = 0.9, // High confidence for manual reports
                                    imageUrl = null // TODO: Implement image upload
                                )
                                mainViewModel.reportFault(reportRequest)
                                
                                // Reset form after submission
                                description = ""
                                faultType = "Power Cut"
                                isSubmitting = false
                                showSuccessMessage = true
                            }
                        },
                        enabled = description.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isSubmitting) "Submitting..." else "Submit Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Success message
        if (showSuccessMessage) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Report Submitted Successfully! ✅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Your fault report has been sent to KSEB staff.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { showSuccessMessage = false }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        // Error message
        error?.let { errorMessage ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Submission Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { mainViewModel.clearError() }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitizenStatusScreen(
    navController: NavController,
    mainViewModel: MainViewModel = koinViewModel()
) {
    val faults by mainViewModel.faults.collectAsState()
    val nodes by mainViewModel.nodes.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Fetch data when screen loads and refresh periodically
    LaunchedEffect(Unit) {
        mainViewModel.fetchFaults()
        mainViewModel.fetchNodes()
        
        // Refresh every 30 seconds for real-time updates
        while (true) {
            kotlinx.coroutines.delay(30000) // 30 seconds
            mainViewModel.fetchFaults()
        }
    }
    
    // Manual refresh function
    fun refreshData() {
        isRefreshing = true
        mainViewModel.fetchFaults()
        mainViewModel.fetchNodes()
        isRefreshing = false
    }
    
    // Convert faults to reports for display (showing all faults, not just user's)
    val reports = faults.map { fault ->
        val node = nodes.find { it.id == fault.nodeId }
        ReportItem(
            id = "#F${fault.id}",
            description = fault.description,
            status = when(fault.status) {
                "ACTIVE" -> "Active"
                "RESOLVED" -> "Resolved"
                "IN_PROGRESS" -> "In Progress"
                else -> fault.status
            },
            meta1 = fault.reportedAt,
            meta2 = "Node: ${node?.name ?: "Unknown"}"
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Fault Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (reports.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    
                    IconButton(
                        onClick = { refreshData() }
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        items(reports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            report.id,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when(report.status) {
                                "Pending" -> MaterialTheme.colorScheme.tertiaryContainer
                                "In Progress" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                report.status,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when(report.status) {
                                    "Pending" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    "In Progress" -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        report.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            report.meta1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            report.meta2,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Empty state if no reports
        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No fault reports yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Report any electrical issues to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private data class ReportItem(
    val id: String,
    val description: String,
    val status: String,
    val meta1: String,
    val meta2: String
)
