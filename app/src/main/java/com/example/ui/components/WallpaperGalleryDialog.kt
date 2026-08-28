package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppCustomSettings
import com.example.model.WallpaperStyle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import java.io.File

@Composable
fun WallpaperGalleryDialog(
    settings: AppCustomSettings,
    onSaveSettings: (AppCustomSettings) -> Unit,
    onAddGalleryImage: (Uri) -> Unit,
    onDeleteCustomWallpaper: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempSettings by remember(settings) { mutableStateOf(settings) }
    var selectedCategory by remember { mutableStateOf("All") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onAddGalleryImage(uri)
            Toast.makeText(context, "Adding wallpaper from Gallery...", Toast.LENGTH_SHORT).show()
        }
    }

    val categories = listOf("All", "My Photos", "Cyber", "Luxury", "Cosmos", "Minimal")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Color(0x66F59E0B), RoundedCornerShape(24.dp))
                .testTag("wallpaper_gallery_dialog"),
            color = Color(0xF8080E1A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 1. TOP HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33F59E0B))
                                .border(1.dp, NeonGoldBright, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Gallery",
                                tint = NeonGoldBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wallpaper Gallery",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Select, add from device & save app theme",
                                color = NeonGoldBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. ADD FROM PHONE GALLERY BUTTON BANNER
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x3306B6D4),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            photoPickerLauncher.launch("image/*")
                        }
                        .testTag("add_wallpaper_from_gallery_btn")
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
                                    .background(NeonCyan.copy(alpha = 0.25f))
                                    .border(1.dp, NeonCyanBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = NeonCyanBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "+ Add from Phone Gallery",
                                    color = NeonCyanBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "फ़ोन गैलरी से नया फोटो/वॉलपेपर जोड़ें और सेव करें",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3322C55E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "Pick Photo",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. CATEGORY CHIPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0x4DF59E0B),
                                selectedLabelColor = NeonGoldBright,
                                containerColor = Color(0x22111D32),
                                labelColor = Color.LightGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonGoldBright else Color(0x33FFFFFF),
                                selectedBorderColor = NeonGoldBright
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. WALLPAPERS GRID (Built-in + Custom Gallery Uploads)
                val presetStyles = WallpaperStyle.values().filter { it != WallpaperStyle.CUSTOM_GALLERY }
                val filteredPresets = if (selectedCategory == "All") {
                    presetStyles
                } else {
                    presetStyles.filter { it.category.equals(selectedCategory, ignoreCase = true) }
                }

                val showCustomPhotos = selectedCategory == "All" || selectedCategory == "My Photos"
                val customWallpapers = if (showCustomPhotos) tempSettings.customWallpaperList else emptyList()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 4A. Custom Uploaded Wallpapers
                    items(customWallpapers) { customPath ->
                        val isSelected = tempSettings.wallpaperStyle == WallpaperStyle.CUSTOM_GALLERY &&
                                tempSettings.customWallpaperUri == customPath

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x330F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyanBright else Color(0x3306B6D4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    tempSettings = tempSettings.copy(
                                        wallpaperStyle = WallpaperStyle.CUSTOM_GALLERY,
                                        customWallpaperUri = customPath,
                                        isWallpaperEnabled = true
                                    )
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Image
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(customPath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "My Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Dark bottom gradient overlay for readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color(0xCC000000))
                                            )
                                        )
                                )

                                // Active Selection Badge
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NeonCyanBright,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopStart)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(3.dp)
                                        )
                                    }
                                }

                                // Delete Action Button
                                IconButton(
                                    onClick = {
                                        onDeleteCustomWallpaper(customPath)
                                        if (tempSettings.customWallpaperUri == customPath) {
                                            tempSettings = tempSettings.copy(
                                                wallpaperStyle = WallpaperStyle.CYBER_GRID,
                                                customWallpaperUri = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color(0x99000000), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = NeonRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Label
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "My Gallery Photo",
                                        color = if (isSelected) NeonCyanBright else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Saved Locally",
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4B. Preset Wallpapers
                    items(filteredPresets) { style ->
                        val isSelected = tempSettings.wallpaperStyle == style &&
                                (tempSettings.customWallpaperUri == null || tempSettings.wallpaperStyle != WallpaperStyle.CUSTOM_GALLERY)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x330F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonGoldBright else Color(0x33F59E0B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    tempSettings = tempSettings.copy(
                                        wallpaperStyle = style,
                                        isWallpaperEnabled = true
                                    )
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Miniature Canvas Thumbnail
                                WallpaperBackground(
                                    isWallpaperEnabled = true,
                                    wallpaperStyle = style,
                                    dimLevel = 0.1f,
                                    themePreset = tempSettings.themePreset,
                                    modifier = Modifier.fillMaxSize()
                                ) {}

                                // Dark bottom gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color(0xCC050912))
                                            )
                                        )
                                )

                                // Active Selection Badge
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NeonGoldBright,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopStart)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(3.dp)
                                        )
                                    }
                                }

                                // Category Tag
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x99000000),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = style.category,
                                        color = Color.LightGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                // Label
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSelected) NeonGoldBright else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = style.description,
                                        color = Color.LightGray,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. QUICK ADJUST CONTROLS (Dimming & Enable Toggle)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x330D1526),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33F59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Adjust",
                                    tint = NeonGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Wallpaper Dim / Darkness",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${(tempSettings.wallpaperDim * 100).toInt()}% Dim",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Slider(
                            value = tempSettings.wallpaperDim,
                            onValueChange = { tempSettings = tempSettings.copy(wallpaperDim = it) },
                            valueRange = 0f..0.95f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGoldBright,
                                activeTrackColor = NeonGold,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 6. BOTTOM ACTION BUTTONS: Apply & Save / Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset Button
                    Button(
                        onClick = {
                            tempSettings = tempSettings.copy(
                                isWallpaperEnabled = true,
                                wallpaperStyle = WallpaperStyle.CYBER_GRID,
                                wallpaperDim = 0.35f,
                                customWallpaperUri = null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x22334155),
                            contentColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Apply & Save Button
                    Button(
                        onClick = {
                            onSaveSettings(tempSettings)
                            Toast.makeText(context, "Wallpaper applied & saved successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("apply_and_save_wallpaper_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply Wallpaper", fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
