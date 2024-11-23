package de.felixlf.gradingscale2.di

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier

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
    return get(kClazz, null, null)
}

@OptIn(BetaInteropApi::class)
fun Koin.get(
    objCClass: ObjCClass,
    qualifier: Qualifier?,
    parameter: Any,
): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, qualifier) { parametersOf(parameter) }
}
