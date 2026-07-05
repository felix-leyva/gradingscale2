package de.felixlf.gradingscale2.uicomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

private val LIST_PANE_WIDTH = 280.dp

/**
 * List-detail layout for grade scale selection: on wide windows the selection list shows as a permanent side pane,
 * on compact windows only [content] is shown (which should offer a [GradeScaleSelectorDropdown] instead).
 *
 * @param content the detail pane. Receives whether the list pane is currently visible, so callers can skip their
 * compact-only selector UI.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveGradeScaleSelector(
    items: PersistentList<GradeScaleNameAndId>,
    selectedItemId: String?,
    onSelectionChange: (String?) -> Unit,
    content: @Composable (isListPaneVisible: Boolean) -> Unit,
) {
    // The detail pane is always the destination: on compact windows the selection happens via dropdown, never by
    // navigating to the list pane.
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        initialDestinationHistory = listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail)),
    )
    val isListPaneVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        // Plain panes without AnimatedPane: pane animations on every window resize freeze the app on desktop
        // (same issue the old debounced WindowSizeClass provider worked around).
        listPane = {
            Box(modifier = Modifier.preferredWidth(LIST_PANE_WIDTH)) {
                GradeScaleSelectionList(
                    items = items,
                    selectedItemId = selectedItemId,
                    onSelectionChange = onSelectionChange,
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        },
        detailPane = {
            content(isListPaneVisible)
        },
    )
}

@Composable
fun GradeScaleSelectorDropdown(
    items: PersistentList<GradeScaleNameAndId>,
    selectedItemId: String?,
    onSelectionChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
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
