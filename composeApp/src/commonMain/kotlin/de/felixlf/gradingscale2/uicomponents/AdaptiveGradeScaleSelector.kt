package de.felixlf.gradingscale2.uicomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.utils.isLargeScreenWidthLocal
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdaptiveGradeScaleSelector(
    items: PersistentList<GradeScaleNameAndId>,
    selectedItemId: String?,
    onSelectionChange: (String?) -> Unit,
    content: @Composable () -> Unit,
) {
    val isLargeScreen by isLargeScreenWidthLocal()

    if (isLargeScreen) {
        Row(modifier = Modifier.fillMaxWidth()) {
            GradeScaleSelectionList(
                items = items,
                selectedItemId = selectedItemId,
                onSelectionChange = onSelectionChange,
                modifier = Modifier.fillMaxHeight(),
            )
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        content()
    }
}

@Composable
fun GradeScaleSelectorDropdown(
    items: PersistentList<GradeScaleNameAndId>,
    selectedItemId: String?,
    onSelectionChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLargeScreen by isLargeScreenWidthLocal()

    if (!isLargeScreen) {
        val selectedName = items.find { it.id == selectedItemId }?.name
        DropboxSelector(
            elements = items.map { it.name }.toImmutableList(),
            selectedElement = selectedName,
            onSelectElement = { name ->
                val selectedItem = items.find { it.name == name }
                onSelectionChange(selectedItem?.id)
            },
            modifier = modifier,
            label = stringResource(Res.string.gradescale_list_select_grade_scale),
        )
    }
}
