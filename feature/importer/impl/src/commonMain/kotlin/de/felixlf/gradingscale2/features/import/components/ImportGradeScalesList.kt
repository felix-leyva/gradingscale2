package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun ImportGradeScalesList(
    countryGradingScales: List<CountryGradingScales>,
    onGradeScaleClick: (String, String) -> Unit,
) {
    val hazeState = rememberHazeState()

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        countryGradingScales.forEach { countryScale ->
            val country = countryScale.country
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.ultraThin(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
                        ),
                ) {
                    Text(
                        text = country,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // Increased padding for text
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Ensure good contrast
                    )
                }
            }

            items(countryScale.gradesScalesNames) { gradeName ->
                GradeScaleItem(
                    modifier = Modifier.hazeSource(hazeState).padding(horizontal = 16.dp),
                    gradeName = gradeName,
                    onClick = { onGradeScaleClick(country, gradeName) },
                )
            }
        }
    }
}
