package de.felixlf.gradingscale2.di

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass

class DependenciesProviderHelper {
    fun setupKoin() {
        val instance = koinSetup()

        koin = instance.koin
    }

    companion object {
        lateinit var koin: Koin
    }
}

@OptIn(BetaInteropApi::class)
fun Koin.get(objCClass: ObjCClass): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return dynamicGet(this, kClazz, null, null)
}

@OptIn(BetaInteropApi::class)
fun Koin.get(
    objCClass: ObjCClass,
    qualifier: Qualifier?,
    parameter: Any,
): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return dynamicGet(this, kClazz, qualifier) { parametersOf(parameter) }
}

/**
 * Runtime lookup by a [KClass] that is only known at runtime (Swift hands us an ObjC class).
 *
 * This deliberately takes [koin] as a plain parameter instead of being a `Koin` extension: for an
 * implicit-receiver `get(kClazz, ...)` inside a `Koin` extension, the Koin compiler plugin's
 * call-site check resolves the call as `get<Any>()` and fails the build with
 * `[KOIN-D002] Missing definition: kotlin.Any`. With the receiver passed in, the plugin leaves
 * the dynamic lookup alone.
 */
private fun <T> dynamicGet(
    koin: Koin,
    clazz: KClass<*>,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null,
): T = koin.get(clazz, qualifier, parameters)
