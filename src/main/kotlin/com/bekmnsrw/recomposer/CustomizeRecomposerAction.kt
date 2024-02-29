package com.bekmnsrw.recomposer;

import com.bekmnsrw.recomposer.util.Constants.ON_CREATE_FUN_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_FQ_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_INIT
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_SHORTEN_NAME
import com.bekmnsrw.recomposer.util.Constants.SUPER_ON_CREATE_CALL
import com.bekmnsrw.recomposer.util.addImport
import com.bekmnsrw.recomposer.util.isApplicationInheritor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateActionBase
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName

class CustomizeRecomposerAction : KotlinGenerateActionBase() {

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        (psiFile as? KtFile)?.let { ktPsiFile ->
            val ktPsiFactory = KtPsiFactory(project)
            val ktClass = ktPsiFile.findDescendantOfType<KtClass>() ?: return

            WriteCommandAction.runWriteCommandAction(project) {
                addImport(
                    ktPsiFactory = ktPsiFactory,
                    ktPsiFile = ktPsiFile,
                    fqName = RECOMPOSER_CONFIG_FQ_NAME
                )
            }

            when (val onCreate = ktClass.findFunctionByName(ON_CREATE_FUN_NAME)) {
                null -> {
                    when (val ktClassBody = ktClass.body) {
                        null -> {
                            WriteCommandAction.runWriteCommandAction(project) {
                                val body = ktPsiFile.addAfter(
                                    ktPsiFactory.createEmptyClassBody(),
                                    ktClass.superTypeListEntries.last()
                                )
                                val recomposerConfigInitWithOnCreateFun = ktPsiFactory.createFunction(
                                    provideRecomposerConfigInitWithOnCreateFun()
                                )
                                val newLine = ktPsiFactory.createNewLine(1)
                                ktPsiFile.addAfter(newLine, (body as KtClassBody).lBrace)
                                ktPsiFile.addBefore(recomposerConfigInitWithOnCreateFun, body.rBrace)
                            }
                        }
                        else -> {
                            WriteCommandAction.runWriteCommandAction(project) {
                                val recomposerConfigInitWithOnCreateFun = ktPsiFactory.createFunction(
                                    provideRecomposerConfigInitWithOnCreateFun()
                                )
                                ktPsiFile.addAfter(recomposerConfigInitWithOnCreateFun, ktClassBody.lBrace)
                            }
                        }
                    }
                }
                else -> {
                    val onCreateFun = onCreate as KtNamedFunction
                    val superOnCreateCall = onCreateFun.bodyExpression?.children?.find { it.text == SUPER_ON_CREATE_CALL }
                    val recomposerConfigInitExpression = ktPsiFactory.createExpression(provideRecomposerConfigInit())
                    WriteCommandAction.runWriteCommandAction(project) {
                        val recomposerConfigInitPsi = ktPsiFile.addAfter(recomposerConfigInitExpression, superOnCreateCall)
                        ktPsiFile.addBefore(ktPsiFactory.createWhiteSpace("\n"), recomposerConfigInitPsi)
                    }
                }
            }

            WriteCommandAction.runWriteCommandAction(project) {
                CodeStyleManager.getInstance(project).reformat(ktPsiFile)
            }
        }
    }

    override fun isValidForClass(targetClass: KtClassOrObject): Boolean {
        return targetClass is KtClass &&
                isApplicationInheritor(targetClass) &&
                !isRecomposerConfigInitAlreadyExists(targetClass)
    }

    private fun isRecomposerConfigInitAlreadyExists(ktClass: KtClassOrObject): Boolean {
        val onCreate = ktClass.findFunctionByName(ON_CREATE_FUN_NAME) as? KtNamedFunction
        val recomposerConfigInit = onCreate?.bodyExpression?.children?.find { psiElement ->
            psiElement.text.contains(RECOMPOSER_CONFIG_INIT)
        }
        return recomposerConfigInit != null
    }

    private fun provideRecomposerConfigInit(): String = """
        $RECOMPOSER_CONFIG_SHORTEN_NAME.init(
            tag = "Provide your custom tag here",
            logger = { tag, message -> /* Provide your custom logger here */ }
        )
    """.trimIndent()

    private fun provideRecomposerConfigInitWithOnCreateFun(): String = """
        override fun onCreate() {
            super.onCreate()
            $RECOMPOSER_CONFIG_SHORTEN_NAME.init(
                tag = "Provide your custom tag here",
                logger = { tag, message -> /* Provide your custom logger here */ }
            )
        }    
    """.trimIndent()
}
