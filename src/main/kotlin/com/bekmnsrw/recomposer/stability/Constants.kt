package com.bekmnsrw.recomposer.stability

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Constants {

    private const val COMPOSE_PACKAGE = "androidx.compose"
    private const val ROOT = "$COMPOSE_PACKAGE.runtime"

    val NON_RESTARTABLE_COMPOSABLE_FQ_NAME = FqName("$ROOT.NonRestartableComposable")
    val NON_SKIPPABLE_COMPOSABLE_FQ_NAME = FqName("$ROOT.NonSkippableComposable")
    val EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME = FqName("$ROOT.ExplicitGroupsComposable")
    val COMPOSE_PACKAGE_NAME = Name.identifier("androidx.compose")
    val STABLE_FQ_NAME = FqName("$ROOT.Stable")
    val IMMUTABLE_FQ_NAME = FqName("$ROOT.Immutable")

    const val SUPPRESS = "Suppress"
    const val NON_SKIPPABLE_COMPOSABLE = "NON_SKIPPABLE_COMPOSABLE"
    const val NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE = "Non skippable function. That will lead to unnecessary recompositions. Consider all function arguments to be stable"
    const val NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE = "Remove unused @Suppress"
    const val NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE = "NonSkippableComposable"
    const val COMPOSABLE_FUNCTION = "androidx.compose.runtime.internal.ComposableFunction"
    const val FUN = "fun"

    val STABLE_COLLECTIONS = listOf(
        "com.google.common.collect.ImmutableList",
        "com.google.common.collect.ImmutableEnumMap",
        "com.google.common.collect.ImmutableMap",
        "com.google.common.collect.ImmutableEnumSet",
        "com.google.common.collect.ImmutableSet",
        "kotlinx.collections.immutable.immutableListOf",
        "kotlinx.collections.immutable.immutableSetOf",
        "kotlinx.collections.immutable.immutableMapOf",
        "kotlinx.collections.immutable.persistentListOf",
        "kotlinx.collections.immutable.persistentSetOf",
        "kotlinx.collections.immutable.persistentMapOf",
        "kotlinx.collections.immutable.ImmutableCollection",
        "kotlinx.collections.immutable.ImmutableList",
        "kotlinx.collections.immutable.ImmutableSet",
        "kotlinx.collections.immutable.ImmutableMap",
        "kotlinx.collections.immutable.PersistentCollection",
        "kotlinx.collections.immutable.PersistentList",
        "kotlinx.collections.immutable.PersistentSet",
        "kotlinx.collections.immutable.PersistentMap",
    )
}
