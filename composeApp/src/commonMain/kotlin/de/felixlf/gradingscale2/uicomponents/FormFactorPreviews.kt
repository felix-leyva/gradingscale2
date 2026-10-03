package de.felixlf.gradingscale2.uicomponents

import androidx.compose.ui.tooling.preview.Preview

/**
 * Previews for verifying adaptive UI across the primary form factors:
 * Phone, Foldable, Tablet, and Desktop.
 */
@Preview(name = "Phone", device = "spec:width=411dp,height=891dp", showBackground = true)
@Preview(name = "Foldable", device = "spec:width=673dp,height=841dp", showBackground = true)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp", showBackground = true)
@Preview(name = "Desktop", device = "spec:width=1920dp,height=1080dp", showBackground = true)
annotation class FormFactorPreviews
