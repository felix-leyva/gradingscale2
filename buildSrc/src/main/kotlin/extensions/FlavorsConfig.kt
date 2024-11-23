package extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension

val flavors: Map<String, Set<String>> =
    mapOf(
        "environment" to setOf("production", "development"),
    )

fun configureFlavors(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        flavorDimensions.addAll(flavors.keys)
        productFlavors {
            flavors.entries.forEach { (flavorDimension, flavors) ->
                flavors.forEach { flavor ->
                    create(flavor) {
                        dimension = flavorDimension
                        if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                            if (applicationIdSuffix != null) {
                                applicationIdSuffix = ".$flavor"
                            }
                        }
                    }
                }
            }
        }
    }
}
