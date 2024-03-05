package com.bekmnsrw.recomposer.util

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
