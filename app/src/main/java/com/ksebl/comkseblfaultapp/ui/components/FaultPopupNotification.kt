package com.ksebl.comkseblfaultapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.ksebl.comkseblfaultapp.model.Fault
import kotlinx.coroutines.delay

@Composable
fun FaultPopupNotification(
    fault: Fault?,
    onDismiss: () -> Unit,
    onViewDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember(fault) { mutableStateOf(fault != null) }
    
    // Auto-dismiss after 10 seconds
    LaunchedEffect(fault) {
        if (fault != null) {
            isVisible = true
            delay(10000) // Show for 10 seconds
            isVisible = false
            delay(300) // Wait for animation
            onDismiss()
        }
    }

    if (fault != null) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = {
                isVisible = false
                onDismiss()
            }
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(500, easing = EaseOutBounce)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                FaultNotificationCard(
                    fault = fault,
                    onDismiss = {
                        isVisible = false
                        onDismiss()
                    },
                    onViewDetails = onViewDetails,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun FaultNotificationCard(
    fault: Fault,
    onDismiss: () -> Unit,
    onViewDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    )
    
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.error,
                spotColor = MaterialTheme.colorScheme.error
            )
            .zIndex(1000f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header with icon and dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Fault Alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ FAULT DETECTED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Fault details
                Column {
                    Text(
                        text = "Fault ID: ${fault.id}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = fault.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                    )
                    
                    if (fault.faultType.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type: ${fault.faultType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Dismiss")
                    }
                    
                    Button(
                        onClick = { onViewDetails(fault.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Details")
                    }
                }
            }
        }
    }
}