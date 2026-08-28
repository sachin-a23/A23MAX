package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketPrediction
import com.example.ui.components.CalculationCard
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    predictions: List<MarketPrediction>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRunCalculation: (MarketPrediction) -> Unit,
    onRecalculateCustom: (marketId: String, openPana: Int, jodi: Int, divisor: Int) -> Unit,
    onNavigateToLab: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGoldBright
) {
    val todayLiveDate = remember { DateUtils.getTodayLiveDate() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Fast Live Date & Market Count Status Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0x660F1A2E),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fast Live: $todayLiveDate",
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "${predictions.size} Markets Active",
                    color = NeonCyanBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Clean Markets Prediction Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 2.dp, bottom = 90.dp)
        ) {
            items(predictions, key = { it.id }) { item ->
                CalculationCard(
                    prediction = item,
                    accentColor = accentColor
                )
            }
        }
    }
}
