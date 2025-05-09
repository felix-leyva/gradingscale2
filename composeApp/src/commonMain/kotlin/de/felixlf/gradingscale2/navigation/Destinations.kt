package de.felixlf.gradingscale2.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.destinations_menu_grade_importerr
import gradingscale2.entities.generated.resources.destinations_menu_grade_scale_detail
import gradingscale2.entities.generated.resources.destinations_menu_grade_scale_list
import gradingscale2.entities.generated.resources.destinations_menu_weighted_grade_calculator
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class Destinations(
    val label: StringResource,
    val icon: ImageVector,
) {
    GradeScaleList(Res.string.destinations_menu_grade_scale_list, Icons.AutoMirrored.Filled.List),

    GradeScaleCalculator(Res.string.destinations_menu_grade_scale_detail, Icons.Default.Edit),

    WeightedGradeCalculator(Res.string.destinations_menu_weighted_grade_calculator, Icons.Default.AddCircle),

    GradeImporter(Res.string.destinations_menu_grade_importerr, Icons.Default.ArrowDropDown),
}
