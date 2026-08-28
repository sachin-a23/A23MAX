package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ThemePreset
import com.example.model.WallpaperStyle
import java.io.File

@Composable
fun WallpaperBackground(
    isWallpaperEnabled: Boolean = true,
    wallpaperStyle: WallpaperStyle = WallpaperStyle.CYBER_GRID,
    customWallpaperUri: String? = null,
    dimLevel: Float = 0.35f, // 0.0f (no dim, bright clear wallpaper) to 1.0f (fully dimmed solid)
    themePreset: ThemePreset = ThemePreset.NEON_GOLD,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(themePreset.primaryColorHex)
    val secondaryColor = Color(themePreset.secondaryColorHex)

    Box(modifier = modifier.fillMaxSize()) {
        if (!isWallpaperEnabled) {
            // Pure minimalist dark solid background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B12))
            )
        } else {
            val isCustomPhoto = (wallpaperStyle == WallpaperStyle.CUSTOM_GALLERY || !customWallpaperUri.isNullOrBlank()) &&
                    !customWallpaperUri.isNullOrBlank() &&
                    (File(customWallpaperUri).exists() || customWallpaperUri.startsWith("content://") || customWallpaperUri.startsWith("http"))

            if (isCustomPhoto) {
                // Render User Selected Custom Gallery Photo
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(customWallpaperUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Custom Gallery Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Dynamic Wallpaper Canvas based on selected style
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    when (wallpaperStyle) {
                        WallpaperStyle.CYBER_GRID, WallpaperStyle.CUSTOM_GALLERY -> {
                            // Base gradient
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF070B14),
                                        Color(0xFF0B1120),
                                        Color(0xFF04070D)
                                    )
                                )
                            )
                            // Ambient Neon Spotlights
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.28f), Color.Transparent),
                                    center = Offset(width * 0.2f, height * 0.15f),
                                    radius = width * 0.7f
                                ),
                                radius = width * 0.7f,
                                center = Offset(width * 0.2f, height * 0.15f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(secondaryColor.copy(alpha = 0.24f), Color.Transparent),
                                    center = Offset(width * 0.85f, height * 0.75f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.85f, height * 0.75f)
                            )
                            // Futuristic Grid lines
                            val gridSize = 44f
                            var x = 0f
                            while (x <= width) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.07f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 1f
                                )
                                x += gridSize
                            }
                            var y = 0f
                            while (y <= height) {
                                drawLine(
                                    color = secondaryColor.copy(alpha = 0.07f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                                y += gridSize
                            }
                        }

                        WallpaperStyle.AURORA_NEBULA -> {
                            // Deep Cosmic Aurora
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF09081E),
                                        Color(0xFF041824),
                                        Color(0xFF050E18)
                                    )
                                )
                            )
                            // Multi-color nebula glows
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.25f, height * 0.25f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.25f, height * 0.25f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.30f), Color.Transparent),
                                    center = Offset(width * 0.75f, height * 0.5f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.75f, height * 0.5f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.4f, height * 0.85f),
                                    radius = width * 0.7f
                                ),
                                radius = width * 0.7f,
                                center = Offset(width * 0.4f, height * 0.85f)
                            )
                        }

                        WallpaperStyle.MATRIX_STREAM -> {
                            // Deep Black Matrix
                            drawRect(color = Color(0xFF020904))
                            // Green Cyber Flow Spotlights
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF22C55E).copy(alpha = 0.30f), Color.Transparent),
                                    center = Offset(width * 0.5f, height * 0.2f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.5f, height * 0.2f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF15803D).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.2f, height * 0.8f),
                                    radius = width * 0.6f
                                ),
                                radius = width * 0.6f,
                                center = Offset(width * 0.2f, height * 0.8f)
                            )
                            // Matrix vertical flow lines
                            val step = 32f
                            var lineX = 16f
                            while (lineX < width) {
                                drawLine(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x0022C55E),
                                            Color(0x2222C55E),
                                            Color(0x444ADE80),
                                            Color(0x0022C55E)
                                        )
                                    ),
                                    start = Offset(lineX, 0f),
                                    end = Offset(lineX, height),
                                    strokeWidth = 1.5f
                                )
                                lineX += step
                            }
                        }

                        WallpaperStyle.GOLDEN_VIP -> {
                            // Royal Luxury Dark Gold
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF140F06),
                                        Color(0xFF1C1304),
                                        Color(0xFF0A0702)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.3f, height * 0.2f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.3f, height * 0.2f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.75f, height * 0.7f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.75f, height * 0.7f)
                            )
                        }

                        WallpaperStyle.MIDNIGHT_SAPPHIRE -> {
                            // Deep Sapphire Blue
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF050D1A),
                                        Color(0xFF0B192E),
                                        Color(0xFF02060D)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.7f, height * 0.25f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.7f, height * 0.25f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.25f, height * 0.75f),
                                    radius = width * 0.7f
                                ),
                                radius = width * 0.7f,
                                center = Offset(width * 0.25f, height * 0.75f)
                            )
                        }

                        WallpaperStyle.SUNSET_CYBERPUNK -> {
                            // Sunset Cyberpunk
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E0A1D),
                                        Color(0xFF130820),
                                        Color(0xFF09040F)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.2f, height * 0.3f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.2f, height * 0.3f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.30f), Color.Transparent),
                                    center = Offset(width * 0.8f, height * 0.65f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.8f, height * 0.65f)
                            )
                        }

                        WallpaperStyle.EMERALD_DRAGON -> {
                            // Royal Emerald Jade
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF03140C),
                                        Color(0xFF062215),
                                        Color(0xFF020B07)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.3f, height * 0.25f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.3f, height * 0.25f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF34D399).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.75f, height * 0.75f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.75f, height * 0.75f)
                            )
                        }

                        WallpaperStyle.RUBY_CRIMSON -> {
                            // Royal Ruby Crimson
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1A050A),
                                        Color(0xFF260810),
                                        Color(0xFF0E0205)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFF43F5E).copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(width * 0.7f, height * 0.2f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.7f, height * 0.2f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFB7185).copy(alpha = 0.22f), Color.Transparent),
                                    center = Offset(width * 0.2f, height * 0.8f),
                                    radius = width * 0.7f
                                ),
                                radius = width * 0.7f,
                                center = Offset(width * 0.2f, height * 0.8f)
                            )
                        }

                        WallpaperStyle.ROYAL_GOLD_HD -> {
                            // Ultra-luminous 24K Gold & Diamond Rays
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F0B03),
                                        Color(0xFF1F1706),
                                        Color(0xFF0A0702)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.38f), Color(0xFFD97706).copy(alpha = 0.15f), Color.Transparent),
                                    center = Offset(width * 0.3f, height * 0.2f),
                                    radius = width * 0.85f
                                ),
                                radius = width * 0.85f,
                                center = Offset(width * 0.3f, height * 0.2f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(width * 0.8f, height * 0.7f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.8f, height * 0.7f)
                            )
                        }

                        WallpaperStyle.COSMIC_DEEP_SPACE -> {
                            // Deep Space Ultraviolet & Starburst
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF06030F),
                                        Color(0xFF0E0720),
                                        Color(0xFF04020A)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFA855F7).copy(alpha = 0.32f), Color(0xFF6366F1).copy(alpha = 0.18f), Color.Transparent),
                                    center = Offset(width * 0.75f, height * 0.25f),
                                    radius = width * 0.85f
                                ),
                                radius = width * 0.85f,
                                center = Offset(width * 0.75f, height * 0.25f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.22f), Color.Transparent),
                                    center = Offset(width * 0.2f, height * 0.8f),
                                    radius = width * 0.7f
                                ),
                                radius = width * 0.7f,
                                center = Offset(width * 0.2f, height * 0.8f)
                            )
                        }

                        WallpaperStyle.FIRE_MAGMA -> {
                            // Volcanic Crimson & Magma Glow
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF140404),
                                        Color(0xFF220808),
                                        Color(0xFF0A0202)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFEF4444).copy(alpha = 0.38f), Color(0xFFF97316).copy(alpha = 0.20f), Color.Transparent),
                                    center = Offset(width * 0.4f, height * 0.25f),
                                    radius = width * 0.85f
                                ),
                                radius = width * 0.85f,
                                center = Offset(width * 0.4f, height * 0.25f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.24f), Color.Transparent),
                                    center = Offset(width * 0.75f, height * 0.75f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.75f, height * 0.75f)
                            )
                        }

                        WallpaperStyle.OBSIDIAN_CARBON -> {
                            // Dark Carbon Fiber & Sleek Titanium
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF090B10),
                                        Color(0xFF111622),
                                        Color(0xFF06070B)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF475569).copy(alpha = 0.30f), Color.Transparent),
                                    center = Offset(width * 0.5f, height * 0.4f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.5f, height * 0.4f)
                            )
                        }

                        WallpaperStyle.DIAMOND_PRISM -> {
                            // Platinum Frost & Diamond Aura
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0C101A),
                                        Color(0xFF141B2B),
                                        Color(0xFF080B12)
                                    )
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFE2E8F0).copy(alpha = 0.28f), Color.Transparent),
                                    center = Offset(width * 0.5f, height * 0.3f),
                                    radius = width * 0.8f
                                ),
                                radius = width * 0.8f,
                                center = Offset(width * 0.5f, height * 0.3f)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF94A3B8).copy(alpha = 0.20f), Color.Transparent),
                                    center = Offset(width * 0.8f, height * 0.8f),
                                    radius = width * 0.75f
                                ),
                                radius = width * 0.75f,
                                center = Offset(width * 0.8f, height * 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Adjustable Dim layer: 0.0 means 0% dim (ultra clear), 1.0 means 100% dim (solid dark)
        if (dimLevel > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimLevel.coerceIn(0f, 0.98f)))
            )
        }

        // Foreground application content
        content()
    }
}

