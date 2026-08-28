package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.R
import com.example.model.AppCustomSettings
import com.example.model.TextColorAccent
import com.example.model.UserProfile
import com.example.model.WallpaperStyle
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed

import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Collections
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.WallpaperBackground
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.example.model.FormulaConfig
import com.example.model.FormulaEngineMode
import com.example.data.FormulaCalculator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppCustomSettings,
    userProfile: UserProfile,
    onUpdateSettings: (AppCustomSettings) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onSyncGithub: () -> Unit,
    onNavigateToLab: () -> Unit,
    onOpenSyncReport: () -> Unit = {},
    onImportRawData: (String, String) -> Unit = { _, _ -> },
    onOpenPanelChart: (String) -> Unit = {},
    onOpenOfflineStorage: () -> Unit = {},
    onOpenWallpaperGallery: () -> Unit = {},
    onApplyFormula: (FormulaConfig) -> Unit = {},
    onSaveCustomFormula: (FormulaConfig, Boolean) -> Unit = { _, _ -> },
    onDeleteCustomFormula: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showProfileDialog by remember { mutableStateOf(false) }
    var showFormulaDialog by remember { mutableStateOf(false) }
    var showGithubDialog by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf(false) }
    var showRawImportDialog by remember { mutableStateOf(false) }
    var selectedChartMarket by remember { mutableStateOf("SHRIDEVI") }

    val activeAccentColor = Color(settings.textColorAccent.hexValue)
    val activeSecondaryColor = Color(settings.textColorAccent.secondaryHex)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.util.WallpaperManager.saveWallpaperFromUri(context, uri)
            if (savedPath != null) {
                onUpdateProfile(userProfile.copy(profilePhotoUri = savedPath))
                Toast.makeText(context, "Admin photo updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. HD WALLPAPERS & VISUAL GLASS CUSTOMIZATION SUITE
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0A101D),
                borderColor = Color(0x4DF59E0B),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Wallpaper Master Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Wallpaper Toggle",
                                tint = NeonGoldBright
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "HD Background Wallpapers",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (settings.isWallpaperEnabled) "HD Wallpapers Active (Glass mode)" else "Disabled (Pure dark mode)",
                                    color = if (settings.isWallpaperEnabled) NeonGreen else Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = settings.isWallpaperEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings(settings.copy(isWallpaperEnabled = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonGoldBright,
                                checkedTrackColor = Color(0x66F59E0B),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("wallpaper_toggle_switch")
                        )
                    }

                    // Wallpaper Style Selection & Gallery Hub
                    AnimatedVisibility(
                        visible = settings.isWallpaperEnabled,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            // Big Gallery Action Banner
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x3306B6D4),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenWallpaperGallery() }
                                    .testTag("open_wallpaper_gallery_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Collections,
                                                contentDescription = "Gallery Hub",
                                                tint = NeonCyanBright,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "HD Wallpaper Gallery & Photos",
                                                color = NeonCyanBright,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "Select preset HD or add custom photos",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x33F59E0B),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGoldBright)
                                    ) {
                                        Text(
                                            text = "Open Gallery",
                                            color = NeonGoldBright,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HD Wallpaper Presets & Photos:",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x33F59E0B)
                                ) {
                                    Text(
                                        text = if (settings.wallpaperStyle == WallpaperStyle.CUSTOM_GALLERY) "Active: Custom Photo" else "Active: ${settings.wallpaperStyle.displayName}",
                                        color = NeonGoldBright,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal Quick Preview Cards
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 1. Add from gallery button
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x2206B6D4),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .size(width = 95.dp, height = 90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenWallpaperGallery() }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Add",
                                                tint = NeonCyanBright,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "+ Add Photo",
                                                color = NeonCyanBright,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                // 2. Custom Photos if available
                                items(settings.customWallpaperList) { customPath ->
                                    val isSelected = settings.wallpaperStyle == WallpaperStyle.CUSTOM_GALLERY &&
                                            settings.customWallpaperUri == customPath
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x220F172A),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) NeonCyanBright else Color(0x3306B6D4)
                                        ),
                                        modifier = Modifier
                                            .size(width = 95.dp, height = 90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onUpdateSettings(
                                                    settings.copy(
                                                        wallpaperStyle = WallpaperStyle.CUSTOM_GALLERY,
                                                        customWallpaperUri = customPath,
                                                        isWallpaperEnabled = true
                                                    )
                                                )
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(File(customPath))
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Custom Wallpaper",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (isSelected) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = NeonCyanBright,
                                                    modifier = Modifier.padding(4.dp).align(Alignment.TopStart)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp).padding(2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. Preset Styles
                                items(WallpaperStyle.values().filter { it != WallpaperStyle.CUSTOM_GALLERY }) { style ->
                                    val isSelected = settings.wallpaperStyle == style &&
                                            (settings.customWallpaperUri == null || settings.wallpaperStyle != WallpaperStyle.CUSTOM_GALLERY)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x220F172A),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) NeonGoldBright else Color(0x33F59E0B)
                                        ),
                                        modifier = Modifier
                                            .size(width = 95.dp, height = 90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onUpdateSettings(
                                                    settings.copy(
                                                        wallpaperStyle = style,
                                                        isWallpaperEnabled = true
                                                    )
                                                )
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            WallpaperBackground(
                                                isWallpaperEnabled = true,
                                                wallpaperStyle = style,
                                                dimLevel = 0.1f,
                                                themePreset = settings.themePreset,
                                                modifier = Modifier.fillMaxSize()
                                            ) {}
                                            if (isSelected) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = NeonGoldBright,
                                                    modifier = Modifier.padding(4.dp).align(Alignment.TopStart)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp).padding(2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = style.displayName,
                                                color = if (isSelected) NeonGoldBright else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .background(Color(0xCC000000))
                                                    .fillMaxWidth()
                                                    .padding(2.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 4. DIMMING LEVEL CONTROLLER (0 - 100)
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0A101D),
                borderColor = Color(0x4DF59E0B),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Dimming Level",
                                tint = NeonGoldBright
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Adjust Dimming Level (0 - 100)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33F59E0B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                        ) {
                            Text(
                                text = "${(settings.wallpaperDim * 100).toInt()}%",
                                color = NeonGoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dim level adjust kare taki background wallpaper or glass cards crystal clear ya solid dark dikhe.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = settings.wallpaperDim,
                        onValueChange = { newDim ->
                            onUpdateSettings(settings.copy(wallpaperDim = newDim))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGoldBright,
                            activeTrackColor = NeonGold,
                            inactiveTrackColor = Color(0x33F59E0B)
                        ),
                        modifier = Modifier.testTag("wallpaper_dim_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0% (Crystal Clear)", color = Color.Gray, fontSize = 11.sp)
                        Text("50% (Balanced)", color = Color.Gray, fontSize = 11.sp)
                        Text("100% (Solid Dark)", color = Color.Gray, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Dim Presets Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.0f to "0%",
                            0.25f to "25%",
                            0.50f to "50%",
                            0.75f to "75%",
                            1.0f to "100%"
                        ).forEach { (presetDim, label) ->
                            val isCurrentPreset = ((settings.wallpaperDim * 100).toInt() == (presetDim * 100).toInt())
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrentPreset) Color(0x44F59E0B) else Color(0x220F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrentPreset) NeonGoldBright else Color(0x33F59E0B)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateSettings(settings.copy(wallpaperDim = presetDim))
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isCurrentPreset) NeonGoldBright else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 5. CHANGE TEXT COLORS & PERSONALIZE GLASS UI THEME
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0A101D),
                borderColor = activeAccentColor.copy(alpha = 0.5f),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Text Color Customizer",
                                tint = activeAccentColor
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Customize Text Colors",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = activeAccentColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, activeAccentColor)
                        ) {
                            Text(
                                text = settings.textColorAccent.displayName,
                                color = activeAccentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose accent text color for headers, OTC badges, and glass UI highlights:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Palette Grid
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextColorAccent.values().forEach { palette ->
                            val isSelected = settings.textColorAccent == palette
                            val pColor = Color(palette.hexValue)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) pColor.copy(alpha = 0.25f) else Color(0x220F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) pColor else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onUpdateSettings(settings.copy(textColorAccent = palette))
                                        Toast.makeText(context, "Accent color: ${palette.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(pColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = palette.displayName,
                                        color = if (isSelected) pColor else Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 6. A23 LAB BANNER
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLab() },
                backgroundColor = Color(0xDD190E2E),
                borderColor = Color(0x80A855F7),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33A855F7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "A23 Lab",
                                tint = Color(0xFFC084FC)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "A23 Lab",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF9333EA)
                                ) {
                                    Text(
                                        text = "NEW OPTION",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Advanced formula simulator & custom market lab",
                                color = Color(0xFFA78BFA),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open Lab",
                        tint = Color(0xFFC084FC)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 7. PROFILE & FORMULA QUICK SETTINGS
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0A101D),
                borderColor = Color(0x3306B6D4),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dedicated User ID & Account Details Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x3306B6D4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(NeonGoldBright, NeonCyanBright, Color(0xFFA855F7))
                                            )
                                        )
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF090F1C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (userProfile.userName.isNotBlank()) userProfile.userName.take(2).uppercase() else "SS",
                                            color = NeonGoldBright,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userProfile.userName,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = NeonCyanBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "USER ID: ",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = userProfile.userId,
                                            color = NeonGoldBright,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${userProfile.role}",
                                            color = NeonCyanBright,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { showProfileDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33F59E0B),
                                    contentColor = NeonGoldBright
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change User ID",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Change User ID & Details (यूजर आईडी व नाम बदलें)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    SettingTile(
                        icon = Icons.Default.Functions,
                        iconTint = NeonGold,
                        title = "Formula Setting",
                        subtitle = "Step 1: (Open+Jodi)*Open • Step 2: /9 • Step 3: OTC ank",
                        onClick = { showFormulaDialog = true }
                    )

                    SettingTile(
                        icon = Icons.Default.PictureAsPdf,
                        iconTint = Color(0xFFF43F5E),
                        title = "PDF Setting",
                        subtitle = "Export market prediction chart & OTC sheet to PDF",
                        onClick = { showPdfDialog = true }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 7B. MARKET PANEL CHART (CHAT / HISTORY GRID)
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0A1324),
                borderColor = Color(0x66F59E0B),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ListAlt,
                                contentDescription = "Panel Chart",
                                tint = NeonGoldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Market Panel Chart (Chat)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33F59E0B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                        ) {
                            Text(
                                text = "PATTI & JODI",
                                color = NeonGoldBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sabhi markets ka full weekly date-to-date panel chart table dekhein. Kis din kya pana aur jodi aaya sabhi details yahan available he:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val chartMarkets = listOf("SHRIDEVI", "KALYAN", "TIME BAZAR", "MILAN", "RAJDHANI DAY", "MAIN BAZAR")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(chartMarkets) { market ->
                            val isSelected = selectedChartMarket.equals(market, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedChartMarket = market
                                    onOpenPanelChart(market)
                                },
                                label = {
                                    Text(
                                        text = market,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0x221E293B),
                                    labelColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onOpenPanelChart(selectedChartMarket) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_panel_chart_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open $selectedChartMarket Panel Chart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 7C. PHONE STORAGE & OFFLINE CACHE
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0C1A1E),
                borderColor = Color(0x6610B981),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Phone Storage",
                                tint = Color(0xFF34D399)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Phone Storage & Offline Mode",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3310B981)
                        ) {
                            Text(
                                text = "OFFLINE OK",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Data phone storage me offline save rehta he, bina internet ke bhi calculations aur charts smoothly load honge. Google Drive link bhi add kar sakte he:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenOfflineStorage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_offline_storage_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Manage Phone Storage & Google Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 8. GITHUB SETTINGS
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD091224),
                borderColor = Color(0x6606B6D4),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "GitHub",
                                tint = NeonCyanBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Data Sync & Remote Files",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3322C55E)
                        ) {
                            Text(
                                text = "AUTO PARSER",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Data link se app me accurate market files load hoti he. Refresh karne par total markets, total days aur holidays (***) report milti hai:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x4406B6D4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D06B6D4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Active Sync URL / File Source:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = settings.customGithubUrl,
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncGithub,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now (रिफ्रेश)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenSyncReport,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33F59E0B), contentColor = NeonGoldBright)
                        ) {
                            Text("📊 View Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2 Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showRawImportDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33A855F7), contentColor = Color(0xFFE9D5FF))
                        ) {
                            Text("📥 Paste Raw Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showGithubDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2238BDF8), contentColor = NeonCyanBright)
                        ) {
                            Text("Configure URL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 9. STYLISH ADMIN PANEL & VIP DEVELOPER PROFILE (SACHIN SOLUNKE)
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stylish_admin_panel_card"),
                backgroundColor = Color(0xF20B101D),
                borderColor = NeonGoldBright.copy(alpha = 0.6f),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top Header with verified badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = NeonGoldBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STYLISH ADMIN PANEL",
                                color = NeonGoldBright,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0x3322C55E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "VERIFIED DEVELOPER",
                                    color = NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Admin Profile Box (Fixed Official Admin Details with Stylish Photo Support)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x400F172A),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x4DF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar with Cyber Gradient Ring & Image Support
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    NeonGoldBright,
                                                    NeonCyanBright,
                                                    Color(0xFFA855F7),
                                                    NeonGoldBright
                                                )
                                            )
                                        )
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF080C14)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!userProfile.profilePhotoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = userProfile.profilePhotoUri,
                                                contentDescription = "Admin Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "SS",
                                                    color = NeonGoldBright,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                    // Floating Camera Upload / VIP Tag
                                    Surface(
                                        shape = CircleShape,
                                        color = if (!userProfile.profilePhotoUri.isNullOrBlank()) NeonCyanBright else NeonGoldBright,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Upload Photo",
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = userProfile.userName.ifBlank { "Sachin Solunke" },
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Admin",
                                            tint = NeonCyanBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = userProfile.role.ifBlank { "VIP Lead Admin & Analyst" },
                                        color = NeonGoldBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x3306B6D4),
                                            border = androidx.compose.foundation.BorderStroke(0.8.dp, NeonCyan.copy(alpha = 0.5f)),
                                            modifier = Modifier.clickable {
                                                copyToClipboard(context, userProfile.userId.ifBlank { "A23-8411" }, "Admin ID")
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "ID: ${userProfile.userId.ifBlank { "A23-8411" }}",
                                                    color = NeonCyanBright,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy ID",
                                                    tint = NeonCyanBright,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x3322C55E),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = "● ${userProfile.status.ifBlank { "Active 24/7" }}",
                                                color = NeonGreen,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Photo Management Row (Upload / Change / Remove)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x2206B6D4),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Upload Photo",
                                            tint = NeonCyanBright,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (userProfile.profilePhotoUri.isNullOrBlank()) "📷 Add My Photo" else "📷 Change Photo",
                                            color = NeonCyanBright,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (!userProfile.profilePhotoUri.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0x22EF4444),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                                        modifier = Modifier.clickable {
                                            onUpdateProfile(userProfile.copy(profilePhotoUri = null))
                                            Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Remove Photo",
                                                tint = NeonRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Remove",
                                                color = NeonRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email pill row with Copy Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x2B06B6D4),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x4D06B6D4)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        copyToClipboard(context, "woldcom87@gmail.com", "Admin Email")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = NeonCyanBright,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "woldcom87@gmail.com",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x3306B6D4)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = NeonCyanBright,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Copy",
                                                color = NeonCyanBright,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fixed Admin Action Buttons (Share Card & Copy Info)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val adminProfile = userProfile.copy(
                                            userId = userProfile.userId.ifBlank { "A23-8411" },
                                            userName = userProfile.userName.ifBlank { "Sachin Solunke" },
                                            role = userProfile.role.ifBlank { "VIP Lead Admin & Analyst" },
                                            email = "woldcom87@gmail.com",
                                            status = "Active / Online 24/7"
                                        )
                                        shareAdminCard(context, adminProfile)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x3306B6D4),
                                        contentColor = NeonCyanBright
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Admin Card", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                Button(
                                    onClick = {
                                        copyToClipboard(
                                            context,
                                            "👑 A23 VIP ADMIN\n👤 Name: ${userProfile.userName.ifBlank { "Sachin Solunke" }}\n🆔 Admin ID: ${userProfile.userId.ifBlank { "A23-8411" }}\n⭐ Role: ${userProfile.role.ifBlank { "VIP Lead Admin & Analyst" }}\n📧 Email: woldcom87@gmail.com\n🌐 Web: https://sachin-a23.github.io/A23Gaming/",
                                            "Admin Full Info"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x33F59E0B),
                                        contentColor = NeonGoldBright
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Contact",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Admin Info", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Column Quick Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("MARKETS", "6 Live", NeonGreen),
                            Triple("ENGINE", "v0.2.1", NeonCyanBright),
                            Triple("ACCESS", "Root VIP", NeonGoldBright),
                            Triple("SYNC", "Cloud OK", Color(0xFFA855F7))
                        ).forEach { (label, value, tint) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x260F172A),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, tint.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = value, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Social & Contact Grid Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL CONNECT & SOCIAL LINKS:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tap to open",
                            color = NeonGoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grid of 6 interactive connection cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Row 1: WhatsApp & Telegram
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminSocialGridItem(
                                modifier = Modifier.weight(1f),
                                title = "WhatsApp Group",
                                subtitle = "VIP Group & Chat",
                                brandColor = Color(0xFF25D366),
                                icon = Icons.Default.Chat,
                                onClick = {
                                    openUrl(context, "https://wa.me/?text=Hello%20A23MAX%20Admin")
                                }
                            )

                            AdminSocialGridItem(
                                modifier = Modifier.weight(1f),
                                title = "Telegram Channel",
                                subtitle = "@Open_network_Sachin",
                                brandColor = Color(0xFF229ED9),
                                icon = Icons.AutoMirrored.Filled.Send,
                                onClick = {
                                    openUrl(context, "https://t.me/Open_network_Sachin")
                                }
                            )
                        }

                        // Row 2: Instagram & Facebook
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminSocialGridItem(
                                modifier = Modifier.weight(1f),
                                title = "Instagram VIP",
                                subtitle = "@black_b.o.y__",
                                brandColor = Color(0xFFE1306C),
                                icon = Icons.Default.Star,
                                onClick = {
                                    openUrl(context, "https://www.instagram.com/black_b.o.y__?igsi=MWp5aWNqdWFqbjc3dg==")
                                }
                            )

                            AdminSocialGridItem(
                                modifier = Modifier.weight(1f),
                                title = "Facebook Profile",
                                subtitle = "Sachin Solunke",
                                brandColor = Color(0xFF1877F2),
                                icon = Icons.Default.Language,
                                onClick = {
                                    openUrl(context, "https://www.facebook.com/share/1KS9zaNsbU/")
                                }
                            )
                        }

                        // Row 3: Website & GitHub Raw Repository
                        AdminSocialBannerItem(
                            title = "GitHub Raw Data Repository & Web Portal",
                            subtitle = "https://sachin-a23.github.io/A23Gaming/",
                            brandColor = NeonCyanBright,
                            icon = Icons.Default.Language,
                            badgeText = "A23site",
                            onClick = {
                                openUrl(context, "https://sachin-a23.github.io/A23Gaming/")
                            }
                        )

                        // Row 4: Official Developer Mail
                        AdminSocialBannerItem(
                            title = "Official Admin & Support Email",
                            subtitle = "a23pro.developer@Gmail.com",
                            brandColor = NeonGoldBright,
                            icon = Icons.Default.Email,
                            badgeText = "DIRECT DESK",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:a23pro.developer@Gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "A23MAX Admin Query & Support")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send Email to Admin"))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Copy All Admin Links & Info Full-width Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x22F59E0B),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonGoldBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val allInfo = """
                                    A23MAX ADMIN CONTACT & SOCIAL PROFILES
                                    --------------------------------------
                                    👤 Admin: ${userProfile.userName} (${userProfile.role})
                                    🆔 User ID: ${userProfile.userId}
                                    ✉️ Primary Email: ${userProfile.email}
                                    ✉️ Support Email: a23pro.developer@Gmail.com
                                    
                                    🔗 Social Links:
                                    • Instagram: https://www.instagram.com/black_b.o.y__
                                    • Telegram: https://t.me/Open_network_Sachin
                                    • Website: https://sachin-a23.github.io/A23Gaming/
                                    • Facebook: https://www.facebook.com/share/1KS9zaNsbU/
                                    • Raw Data: https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json
                                """.trimIndent()
                                copyToClipboard(context, allInfo, "All Admin Links & Info")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy All",
                                tint = NeonGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copy All Admin Links & Info",
                                color = NeonGoldBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 10. ABOUT A23MAX & PLAY STORE READINESS
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD0B132B),
                borderColor = NeonGoldBright.copy(alpha = 0.5f),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_a23_logo),
                                contentDescription = "A23 Logo",
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "A23MAX Analytics Pro",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Version 1.0.0 (Play Store Ready)",
                                    color = NeonGoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3322C55E),
                            border = BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "VERIFIED",
                                color = NeonGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Advanced mathematical analysis, OTC derivation engine, live chart audit record sheets, custom formula laboratory, and multi-market numerology platform.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x221E293B),
                            border = BorderStroke(0.8.dp, Color(0x33475569))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("CREATOR / ADMIN", color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(userProfile.userName, color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x221E293B),
                            border = BorderStroke(0.8.dp, Color(0x33475569))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ADMIN ID", color = Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(userProfile.userId, color = NeonGoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 11. POLICY & WARNING
        // -------------------------------------------------------------
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPolicyDialog = true },
                backgroundColor = Color(0xDD1C1313),
                borderColor = Color(0x66EF4444),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = NeonRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "App Info & Policy • Warning ⚠️",
                            color = Color(0xFFFCA5A5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Educational numerology calculation software. Tap to read policy.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // Profile Edit Dialog (Advanced Admin Profile Editor)
    if (showProfileDialog) {
        var editUserId by remember { mutableStateOf(userProfile.userId) }
        var editName by remember { mutableStateOf(userProfile.userName) }
        var editRole by remember { mutableStateOf(userProfile.role) }
        var editEmail by remember { mutableStateOf(userProfile.email) }
        var editStatus by remember { mutableStateOf(userProfile.status) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Profile",
                        tint = NeonGoldBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change User ID & Profile Details", color = NeonGoldBright, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live Preview Card inside dialog
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x330F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyanBright.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NeonGoldBright, NeonCyanBright, Color(0xFFA855F7))
                                        )
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userProfile.profilePhotoUri.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF090F1C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (editName.isNotBlank()) editName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase() else "SS",
                                            color = NeonGoldBright,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userProfile.profilePhotoUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Admin Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (editName.isNotBlank()) editName else "User Name",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (editRole.isNotBlank()) editRole else "VIP Member"} • ID: ${if (editUserId.isNotBlank()) editUserId else "A23"}",
                                    color = NeonCyanBright,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Admin photo picker button
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBright),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Admin Profile Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("User Name (यूजर नाम)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUserId,
                        onValueChange = { editUserId = it },
                        label = { Text("User ID (e.g. A23-8411 / USER-01)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Member Role / Badge (e.g. VIP Member)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email / Contact Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it },
                        label = { Text("Status (e.g. Active / Online 24/7)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(
                            userProfile.copy(
                                userId = if (editUserId.isNotBlank()) editUserId.trim() else userProfile.userId,
                                userName = if (editName.isNotBlank()) editName.trim() else userProfile.userName,
                                role = if (editRole.isNotBlank()) editRole.trim() else userProfile.role,
                                email = editEmail.trim(),
                                status = if (editStatus.isNotBlank()) editStatus.trim() else userProfile.status
                            )
                        )
                        showProfileDialog = false
                        Toast.makeText(context, "User ID & Details Updated Successfully! ✅", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Formula Settings Dialog
    if (showFormulaDialog) {
        AlertDialog(
            onDismissRequest = { showFormulaDialog = false },
            title = { Text("Formula Settings", color = NeonGoldBright, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Formula last entry ki jodi se banaya jata hai:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33F59E0B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Step 1: (159 + 56) × 159 = 34185", color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Step 2: 34185 ÷ 9 = 3798", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Calculated OTC: 3, 7, 9, 8", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Formula is calculated directly using the Last Entry's Open Pana and Jodi.", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFormulaDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // GitHub URL Settings Dialog
    if (showGithubDialog) {
        var editUrl by remember { mutableStateOf(settings.customGithubUrl) }

        AlertDialog(
            onDismissRequest = { showGithubDialog = false },
            title = { Text("Configure GitHub Data Link", color = NeonCyanBright, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter GitHub Raw JSON URL to load and sync data:", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("Raw JSON URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings(settings.copy(customGithubUrl = editUrl))
                        showGithubDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("Save & Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGithubDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Direct Raw Text Import Dialog
    if (showRawImportDialog) {
        var selectedImportMarket by remember { mutableStateOf("KALYAN") }
        var rawInputText by remember {
            mutableStateOf(
                """
                11-08-2026 / 459 - 81 - 227
                12-08-2026 / 158 - 42 - 129
                13-08-2026 / 560 - 18 - 378
                14-08-2026 / 139 - 31 - 146
                15-08-2026 / *** - ** - ***
                17-08-2026 / 147 - 20 - 460
                18-08-2026 / 239 - 41 - 128
                19-08-2026 / 679 - 28 - 260
                20-08-2026 / 158 - 43 - 157
                21-08-2026 / 136 - 02 - 147
                22-08-2026 / *** - ** - ***
                24-08-2026 / 230 - 58 - 189
                25-08-2026 / 470 - 17 - 179
                """.trimIndent()
            )
        }

        AlertDialog(
            onDismissRequest = { showRawImportDialog = false },
            title = {
                Text(
                    text = "📥 Direct Raw Data Paste / Import",
                    color = Color(0xFFC084FC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste multi-market data [MARKET] or single market history lines (Holidays marked with ***):",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("KALYAN", "SHRIDEVI", "MAIN BAZAR", "MILAN DAY").forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedImportMarket == m) Color(0x66A855F7) else Color(0x221E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedImportMarket == m) Color(0xFFC084FC) else Color.Gray.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedImportMarket = m }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = m,
                                        color = if (selectedImportMarket == m) Color.White else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        label = { Text("Paste Data lines here") },
                        maxLines = 8,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportRawData(selectedImportMarket, rawInputText)
                        showRawImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White)
                ) {
                    Text("Import & Calculate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRawImportDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // PDF Export Dialog
    if (showPdfDialog) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = { Text("Export to PDF Report", color = NeonGoldBright, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Generated PDF chart includes:", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• All Market OTC Calculations & Super Jodi", color = Color.LightGray, fontSize = 12.sp)
                    Text("• Step 1 & Step 2 mathematical breakdown", color = Color.LightGray, fontSize = 12.sp)
                    Text("• 30-Day Historical Pass/Fail audit table", color = Color.LightGray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPdfDialog = false
                        Toast.makeText(context, "Exporting A23MAX Report PDF to Downloads...", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black)
                ) {
                    Text("Download PDF", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) { Text("Close", color = Color.Gray) }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Policy & Disclaimer Dialog
    if (showPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPolicyDialog = false },
            title = { Text("Warning & App Policy ⚠️", color = NeonRed, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "A23MAX is an educational mathematical formula analyzer and numerology calculation tool. All predictions and calculated numbers are generated purely via fixed algebraic algorithms (Step 1 addition/multiplication and Step 2 division). This software does not promote gambling and is strictly intended for algorithmic study and analytical research.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                ) {
                    Text("I Understand", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun SettingTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color.LightGray,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open", tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ContactLinkRow(
    platform: String,
    url: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x330F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22F59E0B)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconTint.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = platform,
                        color = iconTint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = url,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open Link",
                tint = NeonGoldBright,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AdminSocialGridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    brandColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x330F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = brandColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = brandColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = brandColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AdminSocialBannerItem(
    title: String,
    subtitle: String,
    brandColor: Color,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x330F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = brandColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badgeText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = brandColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = badgeText,
                                color = brandColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = brandColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String = "Admin Info") {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard! ✅", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }
}

private fun shareAdminCard(context: Context, profile: UserProfile) {
    try {
        val shareText = """
            👑 A23MAX ADMIN & VIP PROFILE
            --------------------------------
            👤 Name: ${profile.userName}
            ⭐ Title: ${profile.role}
            🆔 User ID: ${profile.userId}
            🟢 Status: ${profile.status}
            ✉️ Email: ${profile.email}
            
            🌐 Official Links:
            • Website: ${profile.website}
            • Telegram: ${profile.telegram} (https://t.me/Open_network_Sachin)
            • Instagram: ${profile.instagram}
            • Facebook: ${profile.facebook}
            
            ⚡ A23MAX Multi-Market Analytics Suite v0.2.1
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "A23MAX Admin Profile - ${profile.userName}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Admin Profile"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing profile: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
