package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.ksebl.comkseblfaultapp.util.Constants
import com.ksebl.comkseblfaultapp.util.dataStore
import com.ksebl.comkseblfaultapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(2000)
    )

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        // Decide next screen based on persisted role
        val roleKey = stringPreferencesKey(Constants.PREF_KEY_ROLE)
        val savedRole = context.dataStore.data
            .map { prefs -> prefs[roleKey] }
            .first()
        val nextRoute = if (!savedRole.isNullOrBlank()) {
            if (savedRole == "STAFF") "staff_dashboard/0" else "citizen_dashboard"
        } else "login"
        navController.navigate(nextRoute) {
            popUpTo("splash") { inclusive = true }
            launchSingleTop = true
        }
    }

    Splash(alpha = alphaAnim.value)
}

@Composable
fun Splash(alpha: Float) {
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Pole Fault Monitor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading...",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}

