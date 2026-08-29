package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun PolicyAndGuideDialog(
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onOpenWebsite: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) } // 0: App Policy, 1: User Policy, 2: Fayde & Nuksan, 3: Legal & About

    val tabs = listOf(
        "ऐप पॉलिसी (Privacy)",
        "यूजर पॉलिसी (Terms)",
        "फायदे व नुकसान (Pros/Cons)",
        "कानूनी नियम (Legal)"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF7090E1A),
            border = BorderStroke(1.5.dp, NeonGoldBright.copy(alpha = 0.8f)),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(NeonGoldBright, NeonCyanBright))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Policy",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "A23MAX OFFICIAL POLICIES",
                                color = NeonGoldBright,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "नियम, शर्तें, फायदे व नुकसान (User Guide)",
                                color = NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0x330F172A),
                    contentColor = NeonCyanBright,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonGoldBright,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) NeonGoldBright else Color.LightGray
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0x220B132B), RoundedCornerShape(16.dp))
                        .border(BorderStroke(0.8.dp, Color(0x3306B6D4)), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            0 -> AppPrivacyPolicySection()
                            1 -> UserPolicySection()
                            2 -> FaydeAndNuksanSection()
                            3 -> LegalNoticeSection()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Actions: Open Website & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenWebsite,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x3306B6D4),
                            contentColor = NeonCyanBright
                        ),
                        border = BorderStroke(1.2.dp, NeonCyanBright)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Website", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Web Portal & Charts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGoldBright,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Understood", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("समझ गए (OK)", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPrivacyPolicySection() {
    PolicyHeaderItem(
        icon = Icons.Default.Security,
        title = "A23MAX ऐप गोपनीयता नीति (App Privacy Policy)",
        subtitle = "Last Updated: 2026 • Official Protocol"
    )

    PolicyCardItem(
        title = "1. 100% ऑन-डिवाइस ऑफलाइन स्टोरेज (Local Database)",
        description = "A23MAX ऐप आपकी सभी सेटिंग्स, बैकअप और फॉर्मूला गणनाओं को आपके ही फोन के Room SQLite डेटाबेस में सुरक्षित रखता है। आपका निजी डेटा किसी अज्ञात बाहरी सर्वर पर स्टोर नहीं किया जाता।"
    )

    PolicyCardItem(
        title = "2. ऐप अनुमतियाँ (Permissions Policy)",
        description = "• स्टोरेज (Storage): कस्टम वॉलपेपर और प्रोफाइल फोटो सेव करने के लिए।\n• इंटरनेट (Internet): लाइव मार्केट ड्रॉ और GitHub डेटा सिंक करने के लिए।\n• कैमरा/गैलरी: प्रोफाइल अवतार फोटो सेट करने के लिए।"
    )

    PolicyCardItem(
        title = "3. थर्ड-पार्टी शेयरिंग निषेध (Zero Data Sharing)",
        description = "हम किसी भी यूजर का डेटा (ईमेल, फोन, नाम या कैलकुलेशन) किसी थर्ड पार्टी या विज्ञापन कंपनी को नहीं बेचते या शेयर करते हैं।"
    )

    PolicyCardItem(
        title = "4. सुरक्षा और एन्क्रिप्शन (Security Standard)",
        description = "ऐप में किसी भी प्रकार का असुरक्षित ट्रांजेक्शन या वित्तीय लेनदेन नहीं होता है। ऐप केवल एक एडवांस्ड सांख्यिकीय और गणितीय रिसर्च टूल है।"
    )
}

@Composable
private fun UserPolicySection() {
    PolicyHeaderItem(
        icon = Icons.Default.PersonOutline,
        title = "यूजर उपयोग नीति एवं नियम (User Policy & Terms)",
        subtitle = "सभी उपयोगकर्ताओं के लिए अनिवार्य नियम"
    )

    PolicyCardItem(
        title = "1. आयु सीमा (Strict 18+ Only)",
        description = "यह एप्लिकेशन केवल 18 वर्ष या उससे अधिक आयु के वयस्क उपयोगकर्ताओं के लिए है। नाबालिगों के लिए इसका उपयोग वर्जित है।"
    )

    PolicyCardItem(
        title = "2. उचित उपयोग नीति (Fair Use & Responsibility)",
        description = "यूजर इस एप्लिकेशन का उपयोग केवल शैक्षिक, सांख्यिकी विश्लेषण और व्यक्तिगत गणितीय अध्ययन के लिए करेंगे। ऐप का उपयोग किसी भी प्रकार के अवैध उद्देश्य के लिए नहीं किया जाएगा।"
    )

    PolicyCardItem(
        title = "3. यूजर अकाउंट व आईडी सुरक्षा",
        description = "यूजर को प्रदान की गई A23 VIP आईडी निजी है। यूजर अपने पासवर्ड और डिवाइस एक्सेस की सुरक्षा के लिए स्वयं जिम्मेदार होंगे।"
    )

    PolicyCardItem(
        title = "4. बौद्धिक संपदा और कॉपीराइट (Copyright)",
        description = "A23MAX के गणितीय फॉर्मूले (A23 Classic, Matrix Delta, Fibonacci), यूआई डिजाइन और सचिन सोलुंके द्वारा विकसित एल्गोरिदम कॉपीराइट संरक्षित हैं। इनका अनधिकृत क्लोन बनाना वर्जित है।"
    )
}

@Composable
private fun FaydeAndNuksanSection() {
    PolicyHeaderItem(
        icon = Icons.Default.Balance,
        title = "A23MAX के फायदे और नुकसान (Pros & Cons Analysis)",
        subtitle = "पूर्ण निष्पक्ष विश्लेषण • स्मार्ट गाइड"
    )

    // FAYDE SECTION (PROS)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x2222C55E),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Fayde", tint = NeonGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("🌟 ऐप के मुख्य फायदे (BENEFITS & PROS):", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }

            FaydePoint("1. 100% सटीक फॉर्मूला इंजन:", "A23 क्लासिक व डेल्टा मैथ से स्वचालित 4 OTC, सुपर जोड़ी व पैनल की तेज गणना बिना किसी मानवीय गलती के।")
            FaydePoint("2. पूर्ण रूप से ऑफलाइन कार्यक्षम:", "इंटरनेट न होने पर भी इनबिल्ट हिस्टोरिकल डेटा और फॉर्मूला लाइब्रेरी पूरी तरह से काम करती है।")
            FaydePoint("3. स्मार्ट AI बैकटैस्टिंग (A23Lab):", "पिछले 30 से 90 दिनों के डेटा का विश्लेषण करके सबसे सफल फॉर्मूले को स्वतः एक्टिवेट करता है।")
            FaydePoint("4. सभी प्रमुख मार्केट्स कवर:", "कल्याण, टाइम बाजार, मेन बाजार, मिलन डे/नाईट, राजधानी, श्रीदेवी आदि का विस्तृत रिकॉर्ड व चार्ट।")
            FaydePoint("5. प्रीमियम स्टाइलिश ग्लास UI:", "सुंदर वॉलपेपर्स, जीरो डिमिंग, रियल टाइम साउंड्स और सचिन सोलुंके का डायरेक्ट एडमिन सपोर्ट डेस्क।")
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // NUKSAN SECTION (CONS & CAUTIONS)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x22EF4444),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Nuksan", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚠️ संभावित नुकसान व सावधानियाँ (RISKS & CAUTIONS):", color = Color(0xFFF87171), fontSize = 13.sp, fontWeight = FontWeight.Black)
            }

            NuksanPoint("1. 100% गारंटी का न होना (No Guaranteed Win):", "यह विशुद्ध गणितीय संभावना (Probability) पर आधारित है। कोई भी गणितीय फॉर्मूला बाजार में 100% जीत की गारंटी नहीं दे सकता।")
            NuksanPoint("2. वित्तीय जोखिम की संभावना (Financial Risk):", "यदि कोई यूजर इन गणनाओं को बिना सोचे-समझे वास्तविक सट्टा/बेटिंग में लगाता है, तो भारी आर्थिक नुकसान हो सकता है।")
            NuksanPoint("3. भावनात्मक लत से बचें (Avoid Addiction):", "लगातार नंबर्स पर निर्भर रहना समय और मानसिक शांति का नुकसान कर सकता है। हमेशा संयम बरतें।")
            NuksanPoint("4. कानूनी प्रतिबंध (Legal Jurisdiction):", "कुछ राज्यों में सट्टा मटका अवैध है। ऐप केवल रिसर्च के लिए है, किसी गैरकानूनी गतिविधि में लिप्त न हों।")
        }
    }
}

@Composable
private fun LegalNoticeSection() {
    PolicyHeaderItem(
        icon = Icons.Default.VerifiedUser,
        title = "कानूनी डिस्क्लेमर एवं पुलिस नोटिस (Legal & Law Compliance)",
        subtitle = "IT Act & Public Gambling Act Compliance"
    )

    PolicyCardItem(
        title = "डिस्क्लेमर (Legal Disclaimer)",
        description = "A23MAX एप्लिकेशन विशुद्ध रूप से गणितीय विश्लेषण, न्यूमरोलॉजी और संख्यात्मक अनुसंधान (Numerology & Mathematical Research) के उद्देश्य से बनाया गया है। यह ऐप किसी भी प्रकार के ऑनलाइन सट्टेबाजी, जुआ या रियल मनी ट्रांजेक्शन का संचालन नहीं करता है।"
    )

    PolicyCardItem(
        title = "पुलिस व कानूनी दिशानिर्देश (Law Enforcement Notice)",
        description = "हम भारत के कानून (Public Gambling Act 1867 एवं Information Technology Act 2000) का पूर्ण सम्मान करते हैं। यदि आपके राज्य में ऐसे खेल प्रतिबंधित हैं, तो कृपया ऐप का उपयोग केवल शैक्षणिक शोध के लिए करें। किसी भी वित्तीय हानि के लिए ऐप या इसके डेवलपर (सचिन सोलुंके) जिम्मेदार नहीं होंगे।"
    )

    PolicyCardItem(
        title = "डेवलपर व स्वामित्व (Developer Ownership)",
        description = "मुख्य डेवलपर: सचिन सोलुंके (Sachin Solunke)\nअधिकारिक ईमेल: woldcom87@gmail.com / a23pro.developer@Gmail.com\nवेबसाइट: https://sachin-a23.github.io/A23Gaming/"
    )
}

@Composable
private fun PolicyHeaderItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeonGoldBright.copy(alpha = 0.15f))
                .border(BorderStroke(1.dp, NeonGoldBright), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, color = NeonCyanBright, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PolicyCardItem(title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x330F172A),
        border = BorderStroke(0.8.dp, Color(0x4D06B6D4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, color = NeonGoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun FaydePoint(heading: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = heading, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFE2E8F0), fontSize = 10.5.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun NuksanPoint(heading: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = heading, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFE2E8F0), fontSize = 10.5.sp, lineHeight = 15.sp)
    }
}
