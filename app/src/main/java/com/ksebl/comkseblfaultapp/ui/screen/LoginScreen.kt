package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
// FCM removed
import com.ksebl.comkseblfaultapp.util.Constants
import com.ksebl.comkseblfaultapp.util.dataStore
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// User role definition

sealed class UserRole(val name: String, val displayName: String) {
    object CITIZEN : UserRole("CITIZEN", "Citizen")
    object STAFF : UserRole("STAFF", "Staff")

}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel()
) {
    var koinError by remember { mutableStateOf<String?>(null) }
    
    // Test ViewModel access safely
    LaunchedEffect(Unit) {
        try {
            // Test basic ViewModel operations
            mainViewModel.toString()
            Log.d("LoginScreen", "ViewModel access successful")
        } catch (e: Exception) {
            koinError = e.message
            Log.e("LoginScreen", "ViewModel access failed: ${e.message}", e)
        }
    }
    // If ViewModel access failed, show error state
    if (koinError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBox,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "App initialization failed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "Please restart the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loginTrigger by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val ROLE_KEY = stringPreferencesKey(Constants.PREF_KEY_ROLE)

    fun handleLogin() {
        if (email.isNotBlank() && selectedRole != null) {
            loginTrigger++
        }
    }

    LaunchedEffect(loginTrigger) {
        if (loginTrigger > 0) {
            isLoading = true
            // Temporarily disable Firebase to test for crash cause
            coroutineScope.launch {
                try {
                    // FCM removed - register device with dummy token
                    val role = selectedRole?.name ?: "CITIZEN"
                    mainViewModel.registerDevice("dummy_token", role)
                } catch (e: Exception) {
                    e.printStackTrace() // Log device registration errors but continue
                }

                try {
                    context.dataStore.edit { prefs ->
                        val role = selectedRole?.name ?: "CITIZEN"
                        prefs[ROLE_KEY] = role
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log DataStore errors but continue
                }

                delay(1500)
                isLoading = false
                
                // Start fault monitoring service (temporarily disabled for testing)
                // try {
                //     mainViewModel.startFaultMonitoring(context)
                // } catch (e: Exception) {
                //     e.printStackTrace() // Log error but don't crash
                // }
                
                // Navigate to staff or citizen dashboard based on selected role
                try {
                    val navigateRoute = when (selectedRole) {
                        UserRole.STAFF -> "staff_dashboard/0"
                        else -> "citizen_dashboard"
                    }
                    navController.navigate(navigateRoute) {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Log navigation errors
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        // Enhanced floating elements with glassmorphism effect
        ModernFloatingElements()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Modern Header
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                ModernHeader()
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Glassmorphic Login Card
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(600, delayMillis = 200)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                GlassmorphicLoginCard(
                    email = email,
                    onEmailChange = { email = it },
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it },
                    onLoginClick = { handleLogin() },
                    isLoading = isLoading,
                    isFormValid = email.isNotBlank() && selectedRole != null
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer with pulse animation
            AnimatedFooter()
        }
    }
}

@Composable
fun ModernFloatingElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float1"
    )

    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float2"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient orb 1
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = float1.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Gradient orb 2
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = float2.dp)
                .blur(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Gradient orb 3
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 100.dp)
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun ModernHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated logo container
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome Back",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to access your account",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun GlassmorphicLoginCard(
    email: String,
    onEmailChange: (String) -> Unit,
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    isFormValid: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email Input with modern styling
            ModernTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Selection Header
            Text(
                text = "Select Your Role",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Role Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernRoleCard(
                    role = UserRole.CITIZEN,
                    title = "Citizen",
                    description = "Report & Track Issues",
                    icon = Icons.Rounded.Person,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer
                    ),
                    isSelected = selectedRole == UserRole.CITIZEN,
                    onSelected = { onRoleSelected(UserRole.CITIZEN) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )

                ModernRoleCard(
                    role = UserRole.STAFF,
                    title = "Staff",
                    description = "Manage & Resolve",
                    icon = Icons.Default.AccountBox,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    ),
                    isSelected = selectedRole == UserRole.STAFF,
                    onSelected = { onRoleSelected(UserRole.STAFF) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Modern Login Button
            ModernLoginButton(
                onClick = onLoginClick,
                isLoading = isLoading,
                isEnabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun ModernRoleCard(
    role: UserRole,
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .aspectRatio(0.85f)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.linearGradient(colors = gradientColors)
                        )
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isSelected)
                                Color.White.copy(alpha = 0.25f)
                            else
                                gradientColors[0].copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else gradientColors[0],
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected)
                        Color.White.copy(alpha = 0.9f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                // Check indicator
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(24.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = gradientColors[0],
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLoginButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isEnabled && !isLoading) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .scale(scale)
            .shadow(
                elevation = if (isEnabled && !isLoading) 20.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
        enabled = isEnabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isEnabled && !isLoading) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Signing In...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedFooter() {
    val infiniteTransition = rememberInfiniteTransition(label = "footer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = alpha), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Secure • Reliable • Fast",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}