package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImportGradeScalesList(
    countryGradingScales: List<CountryGradingScales>,
    onGradeScaleClick: (String, String) -> Unit,
) {
    LazyColumn {
        // Flatten all country grading scales into pairs of country and grade scale name
        countryGradingScales.forEach { countryScale ->
            val country = countryScale.country
            stickyHeader {
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
                ) {
                    Text(
                        text = country,
                        style = MaterialTheme.typography.titleMedium,
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
