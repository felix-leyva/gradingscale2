package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImportGradeScalesList(
    countryGradingScales: List<CountryGradingScales>,
    onGradeScaleClick: (String, String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Added a bit more padding
    ) {
        countryGradingScales.forEach { countryScale ->
            val country = countryScale.country
            stickyHeader {
                Card(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(vertical = 4.dp), // Added some vertical padding around the card
                    shape = RoundedCornerShape(12.dp), // New shape
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // New container color
                    )
                ) {
                    Text(
                        text = country,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // Increased padding for text
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Ensure good contrast
                    )
                }
            }

            items(countryScale.gradesScalesNames) { gradeName ->
                GradeScaleItem(
                    gradeName = gradeName,
                    onClick = { onGradeScaleClick(country, gradeName) },
                )
            }
        }
    }
}
