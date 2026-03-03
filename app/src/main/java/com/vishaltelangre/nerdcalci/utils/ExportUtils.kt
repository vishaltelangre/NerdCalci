package com.vishaltelangre.nerdcalci.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.ui.theme.SyntaxColors
import com.vishaltelangre.nerdcalci.ui.theme.ResultSuccess
import com.vishaltelangre.nerdcalci.ui.theme.ResultError
import androidx.compose.ui.graphics.toArgb
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ExportUtils {

    suspend fun exportAsImage(context: Context, fileName: String, lines: List<LineEntity>) {
        val file = withContext(Dispatchers.IO) {
            val bitmap = createBitmapFromLines(lines)
            val fileNameWithDate = "${fileName}_${getTimestamp()}"
            val file = File(context.cacheDir, "$fileNameWithDate.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        }
        withContext(Dispatchers.Main) {
            shareFile(context, file, "image/png")
        }
    }

    suspend fun exportAsPdf(context: Context, fileName: String, lines: List<LineEntity>) {
        val file = withContext(Dispatchers.IO) {
            val document = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                var page = document.startPage(pageInfo)
                var canvas = page.canvas

                val paint = TextPaint().apply {
                    color = Color.BLACK
                    textSize = 12f
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                val resultPaint = TextPaint().apply {
                    color = Color.DKGRAY
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                val separatorPaint = Paint().apply {
                    color = Color.LTGRAY
                    strokeWidth = 1f
                }

                val padding = 40f
                val startX = padding
                var startY = padding

                val usableWidth = pageInfo.pageWidth - padding * 2
                val exprWidth = (usableWidth * 0.6f).toInt()
                val resWidth = usableWidth.toInt() - exprWidth - 20
                val separatorX = startX + exprWidth + 10f

                // Draw top border
                canvas.drawLine(startX, startY, pageInfo.pageWidth - padding, startY, separatorPaint)

                lines.forEach { line ->
                    val exprLayout = getStaticLayout(highlightSyntax(line.expression), paint, exprWidth, Layout.Alignment.ALIGN_NORMAL)
                    val resLayout = getStaticLayout(formatResult(line.result), resultPaint, resWidth, Layout.Alignment.ALIGN_OPPOSITE)

                    val rowHeight = max(exprLayout.height, resLayout.height) + 20f

                    if (startY + rowHeight > pageInfo.pageHeight - padding) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        startY = padding
                        // Top border for new page
                        canvas.drawLine(startX, startY, pageInfo.pageWidth - padding, startY, separatorPaint)
                    }

                    // Draw Expression
                    canvas.save()
                    canvas.translate(startX, startY + 10f)
                    exprLayout.draw(canvas)
                    canvas.restore()

                    // Draw Result
                    canvas.save()
                    canvas.translate(separatorX + 10f, startY + 10f)
                    resLayout.draw(canvas)
                    canvas.restore()

                    // Draw Vertical Separator
                    canvas.drawLine(separatorX, startY, separatorX, startY + rowHeight, separatorPaint)

                    startY += rowHeight

                    // Draw Horizontal Separator
                    canvas.drawLine(startX, startY, pageInfo.pageWidth - padding, startY, separatorPaint)
                }

                document.finishPage(page)

                val fileNameWithDate = "${fileName}_${getTimestamp()}"
                val file = File(context.cacheDir, "$fileNameWithDate.pdf")
                FileOutputStream(file).use { out ->
                    document.writeTo(out)
                }
                file
            } finally {
                document.close()
            }
        }
        withContext(Dispatchers.Main) {
            shareFile(context, file, "application/pdf")
        }
    }

    private fun createBitmapFromLines(lines: List<LineEntity>): Bitmap {
        val paint = TextPaint().apply {
            color = Color.BLACK
            textSize = 36f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        val resultPaint = TextPaint().apply {
            color = Color.DKGRAY
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val separatorPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

        val padding = 60f
        val width = 1200
        val usableWidth = width - padding * 2
        val exprWidth = (usableWidth * 0.65f).toInt()
        val resWidth = usableWidth.toInt() - exprWidth - 40 // 40px gap
        val separatorX = padding + exprWidth + 20f
        val startX = padding

        var totalHeight = padding + 2f // Top border space mapping
        val layouts = mutableListOf<Pair<StaticLayout, StaticLayout>>()

        for (line in lines) {
            val exprLayout = getStaticLayout(highlightSyntax(line.expression), paint, exprWidth, Layout.Alignment.ALIGN_NORMAL)
            val resLayout = getStaticLayout(formatResult(line.result), resultPaint, resWidth, Layout.Alignment.ALIGN_OPPOSITE)

            val rowHeight = max(exprLayout.height, resLayout.height) + 40f
            totalHeight += rowHeight

            layouts.add(Pair(exprLayout, resLayout))
            if (totalHeight > 10000f) {
                // Hard cap at max height to prevent OOM bounds
                totalHeight = 10000f
                break
            }
        }

        totalHeight += padding

        val bitmapHeight = Math.min(totalHeight.toInt(), 10000)
        val bitmap = Bitmap.createBitmap(width, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var startY = padding
        canvas.drawLine(startX, startY, width - padding, startY, separatorPaint) // Top border

        for (i in layouts.indices) {
            if (startY > bitmapHeight - padding - 40f) break // Safe space check

            val (exprLayout, resLayout) = layouts[i]
            val rowHeight = max(exprLayout.height, resLayout.height) + 40f

            // Draw Expression
            canvas.save()
            canvas.translate(startX, startY + 20f)
            exprLayout.draw(canvas)
            canvas.restore()

            // Draw Result
            canvas.save()
            canvas.translate(separatorX + 20f, startY + 20f)
            resLayout.draw(canvas)
            canvas.restore()

            // Draw Vertical Separator
            canvas.drawLine(separatorX, startY, separatorX, startY + rowHeight, separatorPaint)

            startY += rowHeight

            // Draw Horizontal Separator
            canvas.drawLine(startX, startY, width - padding, startY, separatorPaint)
        }

        return bitmap
    }

    private fun getStaticLayout(text: CharSequence, paint: TextPaint, width: Int, alignment: Layout.Alignment): StaticLayout {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, alignment, 1.2f, 0f, true)
        }
    }

    private fun highlightSyntax(text: String): SpannableString {
        val spannable = SpannableString(text)

        val tokens = SyntaxUtils.parseSyntaxTokens(text)

        val numberColor = SyntaxColors.NumberColorLight.toArgb()
        val variableColor = SyntaxColors.VariableColorLight.toArgb()
        val operatorColor = SyntaxColors.OperatorColorLight.toArgb()
        val percentColor = SyntaxColors.PercentColorLight.toArgb()
        val commentColor = SyntaxColors.CommentColorLight.toArgb()

        for (token in tokens) {
            when (token.type) {
                TokenType.Number -> spannable.setSpan(ForegroundColorSpan(numberColor), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                TokenType.Variable -> {
                    spannable.setSpan(ForegroundColorSpan(variableColor), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                TokenType.Operator -> spannable.setSpan(ForegroundColorSpan(operatorColor), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                TokenType.Percent -> spannable.setSpan(ForegroundColorSpan(percentColor), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                TokenType.Comment -> {
                    spannable.setSpan(ForegroundColorSpan(commentColor), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(android.graphics.Typeface.ITALIC), token.start, token.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                TokenType.Default -> {} // Do nothing
            }
        }

        return spannable
    }

    private fun formatResult(text: String): CharSequence {
        if (text.isBlank()) return ""
        val spannable = SpannableString(text)
        val color = if (text == "Err") ResultError.toArgb() else ResultSuccess.toArgb()
        spannable.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
    }
}
