package com.bekmnsrw.recomposer.util

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Constants {

    const val APPLICATION = "android.app.Application"

    const val BLOCK_ELEMENT_TYPE = "BLOCK"
    const val FUN_ELEMENT_TYPE = "FUN"
    const val FUNCTION_LITERAL_ELEMENT_TYPE = "FUNCTION_LITERAL"

    const val ON_CREATE_FUN_NAME = "onCreate"
    const val SUPER_ON_CREATE_CALL = "super.onCreate()"

    const val RECOMPOSER_NAME = "Recomposer"
    const val INSERT_RECOMPOSER = "Insert Recomposer"
    const val RECOMPOSER_FQ_NAME = "com.bekmnsrw.recomposer.core.Recomposer"

    const val RECOMPOSER_CONFIG_FQ_NAME = "com.bekmnsrw.recomposer.core.RecomposerConfig"
    const val RECOMPOSER_CONFIG_SHORTEN_NAME = "RecomposerConfig"
    const val RECOMPOSER_CONFIG_INIT = "RecomposerConfig.init"

    const val INSERT_RECOMPOSER_HIGHLIGHTER = "Insert RecomposerHighlighter"
    const val RECOMPOSER_HIGHLIGHTER_FQ_NAME = "com.bekmnsrw.recomposer.highlighter.recomposerHighlighter"
    const val RECOMPOSER_HIGHLIGHTER_NAME = "recomposerHighlighter"

    const val COMPOSABLE_SHORT_NAME = "Composable"
    const val COMPOSABLE_FQ_NAME = "androidx.compose.runtime.Composable"
    const val MODIFIER = "Modifier"

    const val MATERIAL = "androidx.compose.material"
    const val MATERIAL3 = "androidx.compose.material3"

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

    fun provideRecomposerFunReference(): String = """
        Recomposer(
            trackingComposableArguments = mapOf(),
            composableName = ""
        )
    """.trimIndent()

    fun provideRecomposerHighlighter(): String = """
        modifier = Modifier.recomposerHighlighter()
    """.trimIndent()
}
