package com.ksebl.comkseblfaultapp.ui.components

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.ksebl.comkseblfaultapp.model.Fault
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationPanel(
    isVisible: Boolean,
    faults: List<Fault>,
    onDismiss: () -> Unit,
    onFaultClick: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Popup(
            alignment = Alignment.TopEnd,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismiss
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(400, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(300)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(200)
                ) + fadeOut(animationSpec = tween(200)) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(200)
                )
            ) {
                NotificationPanelCard(
                    faults = faults,
                    onDismiss = onDismiss,
                    onFaultClick = onFaultClick,
                    onMarkAllRead = onMarkAllRead,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun NotificationPanelCard(
    faults: List<Fault>,
    onDismiss: () -> Unit,
    onFaultClick: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFaults = faults.filter { it.status == "ACTIVE" }.take(10) // Show latest 10 active faults
    
    Card(
        modifier = modifier
            .width(380.dp)
            .heightIn(max = 500.dp)
            .padding(16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .zIndex(1000f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Fault Notifications",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeFaults.size} active faults",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Row {
                        if (activeFaults.isNotEmpty()) {
                            TextButton(
                                onClick = onMarkAllRead,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    "Mark all read",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Notifications list
            if (activeFaults.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All clear! 🎉",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active fault notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeFaults) { fault ->
                        NotificationItem(
                            fault = fault,
                            onClick = { onFaultClick(fault.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    fault: Fault,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val formattedTime = remember(fault.reportedAt) {
        try {
            timeFormatter.format(Date(fault.reportedAt.toLongOrNull() ?: System.currentTimeMillis()))
        } catch (e: Exception) {
            fault.reportedAt
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Severity indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when (fault.priority) {
                            1 -> MaterialTheme.colorScheme.error
                            2 -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Fault #${fault.id}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = fault.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (fault.faultType.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = fault.faultType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}