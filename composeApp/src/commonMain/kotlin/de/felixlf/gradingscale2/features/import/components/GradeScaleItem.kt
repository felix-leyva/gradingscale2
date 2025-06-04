package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun GradeScaleItem(
    gradeName: String,
    onClick: () -> Unit,
) {
    Text(
        text = gradeName,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 24.dp, horizontal = 16.dp).fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
    )
}
