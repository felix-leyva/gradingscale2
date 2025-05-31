@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.WeightedGradeDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStore
import de.felixlf.gradingscale2.store.WeightedGradesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreProvider
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual fun getDbPlatformModule(): Module =
    module {
        single<GradeScaleStoreProvider> {
            val gradeScalesStore = createInMemoryStore(
                default = GradeScalesStoreData(persistentListOf()),
                serializer = GradeScalesStoreData.serializer()
            )
            GradeScaleStoreProvider(gradeScalesStore = gradeScalesStore, dispatcherProvider = get())
        }
        single<WeightedGradesStore> {
            val weightedGradesStore = createInMemoryStore(
                default = WeightedGradesStoreData(persistentListOf()),
                serializer = WeightedGradesStoreData.serializer()
            )
            WeightedGradesStoreProvider(weightedGradesStore = weightedGradesStore, dispatcherProvider = get())
        }
        singleOf(::GradeScaleDaoStoreImpl).bind<GradeScaleDao>()
        singleOf(::GradesDaoStoreImpl).bind<GradesDao>()
        singleOf(::WeightedGradeDaoStoreImpl).bind<WeightedGradeDao>()
    }

// Helper function to create in-memory stores for WasmJS
private inline fun <reified T : Any> createInMemoryStore(
    default: T,
    serializer: kotlinx.serialization.KSerializer<T>
): io.github.xxfast.kstore.KStore<T> {
    val jsonFormat = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // In-memory storage for WasmJS
    val memoryStorage = MutableStateFlow<String?>(null)
    
    return storeOf(
        default = default,
        codec = object : Codec<T> {
            override suspend fun encode(value: T?) {
                val jsonString = if (value != null) {
                    jsonFormat.encodeToString(serializer, value)
                } else {
                    null
                }
                memoryStorage.value = jsonString
            }

            override suspend fun decode(): T? {
                val jsonString = memoryStorage.value
                return if (jsonString?.isNotEmpty() == true) {
                    jsonFormat.decodeFromString(serializer, jsonString)
                } else {
                    null
                }
            }
        }
    )
}