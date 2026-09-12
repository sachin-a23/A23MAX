package com.example.ui.screens.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormulaCalculator
import com.example.model.FormulaConfig
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurpleBright

data class LockedMarketFormulaItem(
    val marketName: String,
    val formulaExpression: String,
    val unseenPassRate: String,
    val config: FormulaConfig,
    val isAlternate: Boolean = false
)

@Composable
fun LockedFormulasLabTab(
    onApplyFormula: (FormulaConfig) -> Unit,
    activeFormulaId: String = "d7_m2_universal"
) {
    val lockedFormulas = remember {
        listOf(
            LockedMarketFormulaItem(
                marketName = "TIME BAZAR",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×3)÷11)+0)",
                unseenPassRate = "75.2%",
                config = FormulaCalculator.TIME_BAZAR_LOCKED
            ),
            LockedMarketFormulaItem(
                marketName = "MILAN",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×4)÷9)+1)",
                unseenPassRate = "69.1%",
                config = FormulaCalculator.MILAN_LOCKED
            ),
            LockedMarketFormulaItem(
                marketName = "KALYAN",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×3)÷3)+7)",
                unseenPassRate = "78.5%",
                config = FormulaCalculator.KALYAN_LOCKED
            ),
            LockedMarketFormulaItem(
                marketName = "SHRIDEVI",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×7)÷8)+5)",
                unseenPassRate = "73.6%",
                config = FormulaCalculator.SHRIDEVI_LOCKED
            ),
            LockedMarketFormulaItem(
                marketName = "SHRIDEVI (Alternate)",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×4)÷7)+3)",
                unseenPassRate = "71.4%",
                config = FormulaCalculator.SHRIDEVI_ALT_LOCKED,
                isAlternate = true
            ),
            LockedMarketFormulaItem(
                marketName = "UNIVERSAL (D7 M2)",
                formulaExpression = "((((OpenPana(T-1)+Jodi(T-1))×2)÷7)+0)",
                unseenPassRate = "74.8%",
                config = FormulaCalculator.D7_M2_CONFIG
            )
        )
    }

    var testOpenPana by remember { mutableStateOf("159") }
    var testJodi by remember { mutableStateOf("56") }
    var selectedFormulaForTest by remember { mutableStateOf(FormulaCalculator.D7_M2_CONFIG) }

    val testResult = remember(testOpenPana, testJodi, selectedFormulaForTest) {
        val open = testOpenPana.toIntOrNull() ?: 159
        val jodi = testJodi.toIntOrNull() ?: 56
        FormulaCalculator.calculateWithConfig(open, jodi, selectedFormulaForTest)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x3306B6D4),
                border = BorderStroke(1.dp, NeonCyanBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCKED FORMULA ARCHITECTURE (D7 M2)",
                            color = NeonGoldBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Har market ke fix locked formulas. Home screen and History follow D7 M2 Master standard.",
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Quick Interactive Tester Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "INTERACTIVE FORMULA VERIFIER",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Open Pana (T-1)", color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
                            OutlinedTextField(
                                value = testOpenPana,
                                onValueChange = { if (it.length <= 3) testOpenPana = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jodi (T-1)", color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
                            OutlinedTextField(
                                value = testJodi,
                                onValueChange = { if (it.length <= 2) testJodi = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x44000000),
                        border = BorderStroke(1.dp, Color(0x3306B6D4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Formula: ${selectedFormulaForTest.name}",
                                color = NeonGoldBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Step 1: ${testResult.step1Formula}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "OTC Output: ${testResult.otcDigits.joinToString(" - ")}",
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Table Header
        item {
            Text(
                text = "OFFICIAL LOCKED FORMULA TABLE",
                color = NeonGoldBright,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        // Formulas List Cards
        items(lockedFormulas.size) { index ->
            val item = lockedFormulas[index]
            val isCurrentActive = activeFormulaId == item.config.id
            val isSelectedForTest = selectedFormulaForTest.id == item.config.id

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelectedForTest) Color(0xFF1E2D4A) else Color(0xFF111827)),
                border = BorderStroke(1.dp, if (isSelectedForTest) NeonCyanBright else Color(0x22FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locked_formula_card_${item.marketName.replace(" ", "_")}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (item.isAlternate) Color(0x33A855F7) else Color(0x3322C55E)
                            ) {
                                Text(
                                    text = if (item.isAlternate) "ALT" else "LOCKED",
                                    color = if (item.isAlternate) NeonPurpleBright else NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.marketName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3322C55E),
                            border = BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "Pass: ${item.unseenPassRate}",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x66000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.formulaExpression,
                            color = NeonCyanBright,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live OTC Prediction for this Formula
                    val itemCalc = remember(testOpenPana, testJodi, item.config) {
                        val open = testOpenPana.toIntOrNull() ?: 159
                        val jodi = testJodi.toIntOrNull() ?: 56
                        FormulaCalculator.calculateWithConfig(open, jodi, item.config)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33000000),
                        border = BorderStroke(1.dp, Color(0x33F59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "PREDICTED OTC:",
                                    color = NeonGoldBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                itemCalc.otcDigits.forEach { digit ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x44F59E0B),
                                        border = BorderStroke(1.dp, NeonGoldBright),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = digit.toString(),
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }

                            if (itemCalc.superJodis.isNotEmpty()) {
                                Text(
                                    text = "Pairs: ${itemCalc.superJodis.take(3).joinToString(" ")}",
                                    color = NeonCyanBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedFormulaForTest = item.config },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3306B6D4)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Run", color = NeonCyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onApplyFormula(item.config) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentActive) Color(0x3322C55E) else NeonGoldBright
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isCurrentActive) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Active", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Set Active", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
