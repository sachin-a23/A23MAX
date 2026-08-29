package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.UserProfile
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminOwnerHeroCard(
    userProfile: UserProfile,
    onUpdateAdminPhoto: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateAdminPhoto(uri)
            Toast.makeText(context, "Admin Photo Updated Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_owner_hero_card"),
        backgroundColor = Color(0x350A1428),
        borderColor = NeonGoldBright.copy(alpha = 0.7f),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ---------------------------------------------------------
            // 1. OFFICIAL APP LOGO & ADMIN BANNER HEADER
            // ---------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x330F172A),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonGold, NeonCyanBright, Color(0xFFA855F7)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Banner Vector Graphic
                    Image(
                        painter = painterResource(id = R.drawable.ic_admin_luxury_banner),
                        contentDescription = "Admin Luxury Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                    )

                    // Banner Content Overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 3D Emblem Logo
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NeonGoldBright, Color(0xFFD97706), NeonCyanBright)
                                        )
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_a23_logo),
                                    contentDescription = "A23 Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "A23 MAX PRO",
                                        color = NeonGoldBright,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xDD22C55E)
                                    ) {
                                        Text(
                                            text = "v0.2.1 VIP",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "OFFICIAL ADMIN & OWNER DECK",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Neural Matrix • Prediction & Formula Lab",
                                    color = NeonCyanBright,
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        // SuperAdmin Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x44F59E0B),
                            border = BorderStroke(1.dp, NeonGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👑 ROOT",
                                    color = NeonGoldBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "ACCESS",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------------------------------------------------------
            // 2. ADMIN OWNER IDENTITY PROFILE (SACHIN SOLUNKE)
            // ---------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x350F172A),
                border = BorderStroke(1.dp, Color(0x4438BDF8)),
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
                        // Admin Avatar Box with Photo & Fallback Portrait
                        Box(
                            modifier = Modifier
                                .size(76.dp)
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
                                .padding(2.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val hasLocalPhoto = !userProfile.profilePhotoUri.isNullOrBlank() && File(userProfile.profilePhotoUri!!).exists()

                            if (hasLocalPhoto) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(userProfile.profilePhotoUri!!))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Admin Sachin Solunke Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                // High-definition Stylized Vector Portrait of the Admin Owner
                                Image(
                                    painter = painterResource(id = R.drawable.ic_admin_owner_portrait),
                                    contentDescription = "Admin Sachin Solunke Portrait",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }

                            // Camera / Upload Photo Quick Button
                            Surface(
                                shape = CircleShape,
                                color = NeonCyanBright,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Change Admin Photo",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sachin Solunke",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
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
                                text = "👑 App Owner & Lead Developer",
                                color = NeonGoldBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "A23 System Founder • Master Control",
                                color = Color.LightGray,
                                fontSize = 10.5.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x3322C55E)
                                ) {
                                    Text(
                                        text = "ID: A23-8411",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Mumbai, MH",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice clarifying Owner Rights
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x22F59E0B),
                        border = BorderStroke(1.dp, Color(0x33F59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ Admin / Malik Notice: ",
                                color = NeonGoldBright,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sachin Solunke is the permanent verified Creator of A23 MAX PRO.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // 3. ADMIN CONTACT & SOCIAL CHANNELS
            // ---------------------------------------------------------
            Text(
                text = "DIRECT ADMIN CONTACT & SUPPORT",
                color = NeonCyanBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // WhatsApp
                AdminContactButton(
                    title = "WhatsApp Direct",
                    icon = Icons.Default.Chat,
                    accentColor = NeonGreen,
                    onClick = {
                        val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919876543210?text=Hello%20Sachin%20Sir%2C%20I%20am%20using%20A23%20MAX%20PRO"))
                        context.startActivity(whatsappIntent)
                    }
                )

                // Phone Call
                AdminContactButton(
                    title = "Call Admin",
                    icon = Icons.Default.Call,
                    accentColor = NeonGoldBright,
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                        context.startActivity(callIntent)
                    }
                )

                // Telegram
                AdminContactButton(
                    title = "Telegram",
                    icon = Icons.Default.OpenInNew,
                    accentColor = NeonCyanBright,
                    onClick = {
                        val tgIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Open_network_Sachin"))
                        context.startActivity(tgIntent)
                    }
                )

                // Instagram
                AdminContactButton(
                    title = "Instagram",
                    icon = Icons.Default.Language,
                    accentColor = Color(0xFFEC4899),
                    onClick = {
                        val instaIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/black_b.o.y__"))
                        context.startActivity(instaIntent)
                    }
                )

                // Website
                AdminContactButton(
                    title = "Official Site",
                    icon = Icons.Default.Language,
                    accentColor = Color(0xFFA855F7),
                    onClick = {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sachin-a23.github.io/A23Gaming/"))
                        context.startActivity(webIntent)
                    }
                )

                // Email
                AdminContactButton(
                    title = "Email Support",
                    icon = Icons.Default.Email,
                    accentColor = Color(0xFF38BDF8),
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:woldcom87@gmail.com"))
                        context.startActivity(emailIntent)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminContactButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
