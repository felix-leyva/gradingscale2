package de.felixlf.gradingscale2.store

import de.felixlf.gradingscale2.entities.models.GradeScale
import io.github.xxfast.kstore.KStore
import kotlinx.collections.immutable.ImmutableList

typealias GradeScalesStore = KStore<ImmutableList<GradeScale>>
