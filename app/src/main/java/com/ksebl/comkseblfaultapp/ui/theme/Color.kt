package com.ksebl.comkseblfaultapp.ui.theme

import androidx.compose.ui.graphics.Color

// Ultra Modern Vibrant Theme - Purple to Orange Gradient (Instagram/Spotify inspired)
// Primary colors - Deep Violet Purple
val Primary = Color(0xFF8B5CF6) // Vibrant Purple - Bold & Creative
val PrimaryVariant = Color(0xFF7C3AED) // Deep Violet
val PrimaryLight = Color(0xFFA78BFA) // Light Purple

// Secondary colors - Sunset Orange
val Secondary = Color(0xFFFF6B35) // Vibrant Orange - Warm & Energetic
val SecondaryVariant = Color(0xFFF97316) // Deep Orange
val SecondaryLight = Color(0xFFFFAB91) // Light Coral

// Tertiary colors - Hot Pink Magenta
val Tertiary = Color(0xFFF72585) // Vibrant Magenta - Electric accent
val TertiaryVariant = Color(0xFFE91E63) // Deep Pink
val TertiaryLight = Color(0xFFFF6EC7) // Light Pink

// Background colors - Vibrant & Modern
val Background = Color(0xFFFFF5F7) // Soft pink-white tint
val Surface = Color(0xFFFFFFFF) // Pure white
val SurfaceVariant = Color(0xFFFFF0F5) // Light rose
val Error = Color(0xFFFF4757) // Bright red for errors

// Text colors - High Contrast
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = Color(0xFF2D1B69) // Rich deep purple
val OnSurface = Color(0xFF3D2C5C) // Deep violet
val OnError = Color.White

// Status colors - Ultra Vibrant & Colorful
val Success = Color(0xFF00E0A1) // Neon Mint Green
val Warning = Color(0xFFFFD700) // Bright Gold
val Info = Color(0xFF00D4FF) // Electric Cyan
val Active = Color(0xFF39FF14) // Neon Lime Green

// Additional colors
val Disabled = Color(0xFFFFE4E6)
val Divider = Color(0xFFFFD6E0) // Light pink tint
val Overlay = Color(0xFF2D1B69).copy(alpha = 0.6f)

// Gradient colors for modern UI elements (Instagram/Sunset inspired)
val GradientStart = Primary // Vibrant Purple
val GradientMiddle = Tertiary // Hot Magenta
val GradientEnd = Secondary // Sunset Orange

// Light theme colors
val LightPrimary = Primary
val LightBackground = Background
val LightSurface = Surface
val LightOnPrimary = OnPrimary
val LightOnBackground = OnBackground
val LightOnSurface = OnSurface
val LightSurfaceVariant = SurfaceVariant

// Dark theme colors - Vibrant Dark Mode
val DarkPrimary = PrimaryLight
val DarkBackground = Color(0xFF0A0A1A) // Nearly black with purple tint
val DarkSurface = Color(0xFF1A1A2E) // Deep purple-slate
val DarkSurfaceVariant = Color(0xFF2D2D44) // Medium purple-slate
val DarkOnPrimary = Color(0xFF0A0A1A)
val DarkOnBackground = Color(0xFFFAFAFF)
val DarkOnSurface = Color(0xFFF5F3FF)