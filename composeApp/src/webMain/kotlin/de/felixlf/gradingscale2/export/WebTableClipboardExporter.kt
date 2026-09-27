package de.felixlf.gradingscale2.export

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import kotlin.js.ExperimentalWasmJsInterop

class WebTableClipboardExporter : TableClipboardExporter {
    override suspend fun copyTableToClipboard(html: String, plainText: String): Boolean {
        return copyToClipboardJs(html, plainText)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun copyToClipboardJs(html: String, text: String): Boolean = js(
    """
    (() => {
        try {
            // Modern Async Clipboard API with text/html and text/plain MIME support
            if (html && html.length > 0 && navigator.clipboard && typeof window.ClipboardItem !== 'undefined') {
                const htmlBlob = new Blob([html], { type: 'text/html' });
                const textBlob = new Blob([text], { type: 'text/plain' });
                const item = new ClipboardItem({
                    'text/html': htmlBlob,
                    'text/plain': textBlob
                });
                navigator.clipboard.write([item]).catch(function(err) {
                    console.warn('ClipboardItem write failed, falling back to writeText:', err);
                    navigator.clipboard.writeText(text);
                });
                return true;
            } else if (navigator.clipboard && navigator.clipboard.writeText) {
                // Clipboard writeText fallback
                navigator.clipboard.writeText(text);
                return true;
            } else {
                // Legacy document.execCommand('copy') textarea fallback
                const textarea = document.createElement('textarea');
                textarea.value = text;
                textarea.style.position = 'fixed';
                textarea.style.opacity = '0';
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand('copy');
                document.body.removeChild(textarea);
                return true;
            }
        } catch (e) {
            console.error('Copy to clipboard failed with exception:', e);
            try {
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(text);
                    return true;
                }
            } catch (fallbackError) {
                console.error('Secondary clipboard fallback also failed:', fallbackError);
            }
            return false;
        }
    })()
    """
)
