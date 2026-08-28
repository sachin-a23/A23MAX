package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.BacktestSummary
import com.example.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfExportResult(
    val success: Boolean,
    val fileName: String,
    val filePath: String,
    val fileUri: Uri? = null,
    val message: String
)

object PdfReportGenerator {

    fun generateAndSavePdf(
        context: Context,
        summary: BacktestSummary,
        userProfile: UserProfile
    ): PdfExportResult {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val readableDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
            val safeMarketName = summary.marketName.replace(" ", "_")
            val fileName = "A23MAX_Digital_Report_${safeMarketName}_$timeStamp.pdf"

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 32f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rowsPerPage = 17
            val totalRows = summary.results.size
            val totalPages = Math.max(1, Math.ceil(totalRows.toDouble() / rowsPerPage).toInt())

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Digital Soft Canvas Background
                canvas.drawColor(Color.rgb(248, 250, 252))

                var currentY = margin

                // Header on Page 1
                if (pageIndex == 0) {
                    // Header Dark Tech Box
                    paint.color = Color.rgb(15, 23, 42) // Dark Slate #0F172A
                    val headerRect = RectF(margin, currentY, pageWidth - margin, currentY + 112f)
                    canvas.drawRoundRect(headerRect, 10f, 10f, paint)

                    // Gold Neon Left Stripe
                    paint.color = Color.rgb(245, 158, 11) // Gold #F59E0B
                    canvas.drawRoundRect(RectF(margin, currentY, margin + 6f, currentY + 112f), 4f, 4f, paint)

                    // Digital Verified Seal Tag on Top Right
                    paint.color = Color.rgb(30, 41, 59)
                    val sealRect = RectF(pageWidth - margin - 150f, currentY + 10f, pageWidth - margin - 12f, currentY + 30f)
                    canvas.drawRoundRect(sealRect, 5f, 5f, paint)
                    paint.color = Color.rgb(16, 185, 129) // Emerald
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("● DIGITAL VERIFIED AUDIT", pageWidth - margin - 142f, currentY + 23f, paint)

                    // App Title
                    paint.color = Color.rgb(245, 158, 11)
                    paint.textSize = 15f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("A23 MAX • FORMULA BACKTESTING & DIGITAL REPORT", margin + 16f, currentY + 26f, paint)

                    // Subtitle & Metadata Row
                    paint.color = Color.rgb(226, 232, 240)
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Market: ${summary.marketName}   |   Formula: ${summary.formulaName}", margin + 16f, currentY + 46f, paint)
                    canvas.drawText("Generated: $readableDate   |   Analyst: ${userProfile.userName} [ID: ${userProfile.userId}]", margin + 16f, currentY + 64f, paint)

                    // Formula breakdown badge
                    paint.color = Color.rgb(51, 65, 85)
                    val formulaBox = RectF(margin + 16f, currentY + 76f, pageWidth - margin - 16f, currentY + 102f)
                    canvas.drawRoundRect(formulaBox, 5f, 5f, paint)

                    paint.color = Color.rgb(56, 189, 248) // Neon Cyan
                    paint.textSize = 9.5f
                    paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    canvas.drawText("Mathematical Engine: ${summary.formulaExpression}", margin + 24f, currentY + 93f, paint)

                    currentY += 122f

                    // 5 KPI Metric Tiles Grid
                    val gridWidth = pageWidth - (margin * 2)
                    val colWidth = (gridWidth - 16f) / 5f
                    val statCardHeight = 58f

                    drawDigitalStatTile(canvas, paint, margin, currentY, colWidth, statCardHeight, "TOTAL DAYS", "${summary.totalTestedDays}", Color.rgb(15, 23, 42), Color.rgb(241, 245, 249))
                    drawDigitalStatTile(canvas, paint, margin + colWidth + 4f, currentY, colWidth, statCardHeight, "PASSED DAYS", "${summary.passedDays}", Color.rgb(16, 185, 129), Color.rgb(236, 253, 245))
                    drawDigitalStatTile(canvas, paint, margin + (colWidth * 2) + 8f, currentY, colWidth, statCardHeight, "FAILED DAYS", "${summary.failedDays}", Color.rgb(239, 68, 68), Color.rgb(254, 242, 242))

                    val accBg = if (summary.accuracyPercentage >= 75f) Color.rgb(236, 253, 245) else Color.rgb(254, 243, 199)
                    val accColor = if (summary.accuracyPercentage >= 75f) Color.rgb(16, 185, 129) else Color.rgb(217, 119, 6)
                    drawDigitalStatTile(canvas, paint, margin + (colWidth * 3) + 12f, currentY, colWidth, statCardHeight, "ACCURACY %", String.format(Locale.ENGLISH, "%.1f%%", summary.accuracyPercentage), accColor, accBg)

                    drawDigitalStatTile(canvas, paint, margin + (colWidth * 4) + 16f, currentY, colWidth, statCardHeight, "MAX STREAK", "${summary.maxStreak} Days", Color.rgb(14, 165, 233), Color.rgb(240, 249, 255))

                    currentY += 68f
                } else {
                    // Small header on subsequent pages
                    paint.color = Color.rgb(15, 23, 42)
                    paint.textSize = 12f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("A23 MAX • ${summary.marketName} Historical Pass/Fail Record (Contd.)", margin, currentY + 14f, paint)

                    paint.color = Color.rgb(100, 116, 139)
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Formula: ${summary.formulaName}   |   Overall Accuracy: ${String.format(Locale.ENGLISH, "%.1f%%", summary.accuracyPercentage)}", margin, currentY + 28f, paint)
                    currentY += 38f
                }

                // Table Header
                val tableHeaderY = currentY
                paint.color = Color.rgb(15, 23, 42)
                val tableHeaderRect = RectF(margin, tableHeaderY, pageWidth - margin, tableHeaderY + 24f)
                canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

                paint.color = Color.WHITE
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val colDate = margin + 8f
                val colDay = margin + 78f
                val colPrev = margin + 128f
                val colActual = margin + 225f
                val colOtc = margin + 315f
                val colStatus = margin + 418f
                val colWin = margin + 478f

                canvas.drawText("DATE", colDate, tableHeaderY + 16f, paint)
                canvas.drawText("DAY", colDay, tableHeaderY + 16f, paint)
                canvas.drawText("PREV ENTRY", colPrev, tableHeaderY + 16f, paint)
                canvas.drawText("ACTUAL RESULT", colActual, tableHeaderY + 16f, paint)
                canvas.drawText("PREDICTED OTC", colOtc, tableHeaderY + 16f, paint)
                canvas.drawText("STATUS", colStatus, tableHeaderY + 16f, paint)
                canvas.drawText("WIN ANK", colWin, tableHeaderY + 16f, paint)

                currentY += 25f

                // Table Rows
                val startIndex = pageIndex * rowsPerPage
                val endIndex = Math.min(startIndex + rowsPerPage, totalRows)

                for (rowIndex in startIndex until endIndex) {
                    val item = summary.results[rowIndex]
                    val rowY = currentY
                    val rowHeight = 24f

                    // Alternating background
                    paint.color = if (rowIndex % 2 == 0) Color.rgb(255, 255, 255) else Color.rgb(241, 245, 249)
                    canvas.drawRect(RectF(margin, rowY, pageWidth - margin, rowY + rowHeight), paint)

                    // Text values
                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)

                    canvas.drawText(item.date, colDate, rowY + 16f, paint)
                    canvas.drawText(item.dayOfWeek.take(3), colDay, rowY + 16f, paint)
                    canvas.drawText(item.previousResult, colPrev, rowY + 16f, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText(item.actualResult, colActual, rowY + 16f, paint)

                    // Predicted OTC
                    paint.color = Color.rgb(180, 83, 9)
                    paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    canvas.drawText(item.predictedOtc.joinToString(", "), colOtc, rowY + 16f, paint)

                    // Status Badge
                    val badgeRect = RectF(colStatus, rowY + 4f, colStatus + 46f, rowY + 20f)
                    when {
                        item.isHoliday -> {
                            paint.color = Color.rgb(148, 163, 184)
                            canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
                            paint.color = Color.WHITE
                            paint.textSize = 7.5f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText("HOLIDAY", colStatus + 5f, rowY + 15f, paint)
                        }
                        item.isPassed -> {
                            paint.color = Color.rgb(16, 185, 129)
                            canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
                            paint.color = Color.WHITE
                            paint.textSize = 8f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText("PASS", colStatus + 12f, rowY + 15f, paint)
                        }
                        else -> {
                            paint.color = Color.rgb(239, 68, 68)
                            canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
                            paint.color = Color.WHITE
                            paint.textSize = 8f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText("FAIL", colStatus + 14f, rowY + 15f, paint)
                        }
                    }

                    // Win Ank
                    paint.color = if (item.isPassed) Color.rgb(16, 185, 129) else Color.rgb(100, 116, 139)
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val winText = if (item.winningDigits.isNotEmpty()) item.winningDigits.joinToString(", ") else "-"
                    canvas.drawText(winText, colWin + 10f, rowY + 16f, paint)

                    currentY += rowHeight
                }

                // Page Footer
                paint.color = Color.rgb(148, 163, 184)
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("🔒 A23 MAX DIGITAL VERIFIED REPORT • CONFIDENTIAL RESEARCH & NUMEROLOGY", margin, pageHeight - 16f, paint)
                canvas.drawText("Page ${pageIndex + 1} of $totalPages", pageWidth - margin - 65f, pageHeight - 16f, paint)

                pdfDocument.finishPage(page)
            }

            // Save to Cache for immediate FileProvider Uri sharing
            val cacheFile = File(context.cacheDir, fileName)
            val cacheFos = FileOutputStream(cacheFile)
            pdfDocument.writeTo(cacheFos)
            cacheFos.flush()
            cacheFos.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            // Save to Public Downloads directory
            var savedPath = cacheFile.absolutePath
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/A23_Reports")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    savedPath = "Downloads/A23_Reports/$fileName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                val fos = FileOutputStream(targetFile)
                pdfDocument.writeTo(fos)
                fos.flush()
                fos.close()
                savedPath = targetFile.absolutePath
            }

            pdfDocument.close()

            PdfExportResult(
                success = true,
                fileName = fileName,
                filePath = savedPath,
                fileUri = contentUri,
                message = "Digital PDF Report successfully generated & saved ($fileName)"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PdfExportResult(
                success = false,
                fileName = "",
                filePath = "",
                fileUri = null,
                message = "Failed to generate PDF: ${e.localizedMessage}"
            )
        }
    }

    private fun drawDigitalStatTile(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColor: Int,
        bgColor: Int
    ) {
        // Tile Background
        paint.color = bgColor
        val tileRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(tileRect, 6f, 6f, paint)

        // Tile Border
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(tileRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Label
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 6f, y + 18f, paint)

        // Value
        paint.color = valueColor
        paint.textSize = 13.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 6f, y + 42f, paint)
    }

    fun openPdfFile(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Digital PDF Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
