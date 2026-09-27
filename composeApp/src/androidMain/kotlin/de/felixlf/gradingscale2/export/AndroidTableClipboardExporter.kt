package de.felixlf.gradingscale2.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter

class AndroidTableClipboardExporter(
    private val context: Context,
) : TableClipboardExporter {
    override suspend fun copyTableToClipboard(html: String, plainText: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return false
            val clip = ClipData.newHtmlText("Exam Grading Scale", plainText, html)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }
}
