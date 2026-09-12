package com.example.util

import android.content.Context
import android.content.Intent
import com.example.model.MarketMoneyTrackState
import com.example.model.MarketPrediction
import com.example.model.MoneyTrackWonCycle

object SocialShareHelper {

    fun sharePrediction(context: Context, prediction: MarketPrediction) {
        val otcStr = prediction.otcList.joinToString("-")
        val jodiStr = prediction.jodiList.take(6).joinToString(", ")
        val masterJodis = prediction.vipMasterJodis.take(4).joinToString(", ")
        val pannesStr = prediction.panneList.take(6).joinToString(", ")

        val shareText = """
            🎯 *A23 MAX - ${prediction.marketName.uppercase()}* 🎯
            📅 Date: ${prediction.date}
            
            ⭐ *LUCKY 4 OTC:* [ ${otcStr} ]
            🔥 *VIP Master Jodis:* ${if (masterJodis.isNotBlank()) masterJodis else "Check in App"}
            🎲 *Support Jodis:* ${jodiStr}
            📋 *Strong Panne:* ${pannesStr}
            
            ⚡ _Calculated using A23 Mathematical Analytics Engine_
            📲 Generated via A23 MAX Analytics App
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${prediction.marketName} Daily Analysis")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Analysis via"))
    }

    fun shareMoneyTrackWin(context: Context, wonCycle: MoneyTrackWonCycle) {
        val shareText = """
            🏆 *A23 MONEY TRACK - WIN CONGRATULATIONS!* 🏆
            
            📍 *Market:* ${wonCycle.marketName.uppercase()}
            🎉 *Result:* OTC Passed (${wonCycle.winningSession.displayName})
            📈 *Stage Reached:* Day ${wonCycle.finalDay} (Step ${wonCycle.totalSteps})
            
            💰 *Rate / Ank:* ₹${wonCycle.winningRatePerAnk}
            💳 *Total Investment:* ₹${wonCycle.totalInvested}
            🎁 *Total Return:* ₹${wonCycle.returnAmount}
            🟢 *TRUE NET PROFIT:* +₹${wonCycle.netProfit} ✅
            
            ⚡ _Smart Recovery Engine Formula Reset to Stage 1!_
            📲 Generated via A23 MAX Analytics App
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Money Track Win Report")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Win Report via"))
    }
}
