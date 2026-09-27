package de.felixlf.gradingscale2.export

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class JvmTableClipboardExporter : TableClipboardExporter {

    override suspend fun copyTableToClipboard(html: String, plainText: String): Boolean {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = HtmlAndTextTransferable(html, plainText)
            clipboard.setContents(transferable, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private class HtmlAndTextTransferable(
        private val html: String,
        private val plainText: String,
    ) : Transferable {
        private val htmlFlavor = DataFlavor("text/html; class=java.lang.String")
        private val flavors = if (html.isNotBlank()) {
            arrayOf(htmlFlavor, DataFlavor.stringFlavor)
        } else {
            arrayOf(DataFlavor.stringFlavor)
        }

        override fun getTransferDataFlavors(): Array<DataFlavor> = flavors

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
            return flavors.any { it.equals(flavor) }
        }

        override fun getTransferData(flavor: DataFlavor?): Any {
            return when {
                html.isNotBlank() && htmlFlavor.equals(flavor) -> html
                DataFlavor.stringFlavor.equals(flavor) -> plainText
                else -> throw UnsupportedFlavorException(flavor)
            }
        }
    }
}
