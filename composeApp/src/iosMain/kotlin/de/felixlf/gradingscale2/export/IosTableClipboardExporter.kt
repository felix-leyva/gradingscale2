package de.felixlf.gradingscale2.export

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIPasteboard

class IosTableClipboardExporter : TableClipboardExporter {

    override suspend fun copyTableToClipboard(html: String, plainText: String): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                val pasteboard = UIPasteboard.generalPasteboard
                val item = mutableMapOf<Any?, Any>()

                if (html.isNotBlank()) {
                    val htmlData = (html as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (htmlData != null) {
                        item["public.html"] = htmlData
                    }
                }
                if (plainText.isNotBlank()) {
                    val textData = (plainText as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (textData != null) {
                        item["public.utf8-plain-text"] = textData
                    }
                }

                pasteboard.setItems(listOf(item))
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
