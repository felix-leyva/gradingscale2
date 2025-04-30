package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        // Flatten all country grading scales into pairs of country and grade scale name
        countryGradingScales.forEach { countryScale ->
            val country = countryScale.country
            stickyHeader {
                Card(
                    modifier = Modifier.fillParentMaxWidth(),
                ) {
                    Text(
                        text = country,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
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
