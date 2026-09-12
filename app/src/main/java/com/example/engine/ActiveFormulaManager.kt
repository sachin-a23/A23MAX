package com.example.engine

import android.content.Context
import com.example.model.FormulaConfig
import com.example.model.ResearchedFormulaCandidate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class LockedActiveFormulaRecord(
    val marketName: String,
    val candidate: ResearchedFormulaCandidate,
    val lockedConfig: FormulaConfig,
    val activatedTimestampMillis: Long,
    val activatedTimestampFormatted: String,
    val userConfirmation1Confirmed: Boolean,
    val userConfirmation2Confirmed: Boolean
)

object ActiveFormulaManager {

    private const val PREFS_NAME = "a23_active_formulas_prefs"

    // Market -> LockedActiveFormulaRecord
    private val activeLockedFormulas = ConcurrentHashMap<String, LockedActiveFormulaRecord>()

    // Market -> List of Saved Formula Candidates in Formula List
    private val savedCandidates = ConcurrentHashMap<String, MutableList<ResearchedFormulaCandidate>>()

    fun initialize(context: Context) {
        // Can reload persisted active formula mappings if needed
    }

    /**
     * Gets the current locked active formula for a market.
     */
    fun getActiveFormulaForMarket(marketName: String): FormulaConfig {
        val key = marketName.trim().uppercase()
        val record = activeLockedFormulas[key]
        return record?.lockedConfig ?: FormulaConfig(
            id = "default_$key",
            name = "A23 MAX Default Formula",
            isLocked = true
        )
    }

    /**
     * Gets full active locked record for verification report display.
     */
    fun getActiveRecordForMarket(marketName: String): LockedActiveFormulaRecord? {
        val key = marketName.trim().uppercase()
        return activeLockedFormulas[key]
    }

    /**
     * Saves a researched candidate to the User's Formula List (Initially Unlocked).
     */
    fun addCandidateToFormulaList(candidate: ResearchedFormulaCandidate) {
        val key = candidate.marketName.trim().uppercase()
        val list = savedCandidates.getOrPut(key) { mutableListOf() }
        if (!list.any { it.expressionHash == candidate.expressionHash }) {
            list.add(candidate.copy(isLocked = false, isActivated = false))
        }
    }

    fun getSavedCandidates(marketName: String): List<ResearchedFormulaCandidate> {
        val key = marketName.trim().uppercase()
        return savedCandidates[key]?.toList() ?: emptyList()
    }

    fun removeSavedCandidate(marketName: String, expressionHash: String) {
        val key = marketName.trim().uppercase()
        savedCandidates[key]?.removeAll { it.expressionHash == expressionHash }
    }

    /**
     * 2-Step Explicit User Confirmation to Activate and Lock a Formula.
     */
    fun activateAndLockFormula(
        marketName: String,
        candidate: ResearchedFormulaCandidate,
        confirm1: Boolean,
        confirm2: Boolean
    ): Boolean {
        if (!confirm1 || !confirm2) return false

        val key = marketName.trim().uppercase()
        val now = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(now))

        val lockedCandidate = candidate.copy(
            isLocked = true,
            isActivated = true,
            version = candidate.version + 1
        )

        val lockedConfig = candidate.config.copy(
            id = "locked_${candidate.expressionHash}",
            name = "${candidate.formulaName} [ACTIVE 🔒]",
            isLocked = true,
            isCustom = true,
            customNotes = "Locked Active Formula (Pass Rate: ${String.format(Locale.ENGLISH, "%.1f%%", candidate.overallPassRate)}, Verified: $formattedDate)"
        )

        val record = LockedActiveFormulaRecord(
            marketName = key,
            candidate = lockedCandidate,
            lockedConfig = lockedConfig,
            activatedTimestampMillis = now,
            activatedTimestampFormatted = formattedDate,
            userConfirmation1Confirmed = true,
            userConfirmation2Confirmed = true
        )

        activeLockedFormulas[key] = record

        // Also mark as locked in saved candidates list
        savedCandidates[key]?.let { list ->
            val idx = list.indexOfFirst { it.expressionHash == candidate.expressionHash }
            if (idx >= 0) {
                list[idx] = lockedCandidate
            } else {
                list.add(lockedCandidate)
            }
        }

        return true
    }
}
