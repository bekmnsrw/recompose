package com.bekmnsrw.recomposer;

import com.bekmnsrw.recomposer.util.Constants.APPLICATION
import com.bekmnsrw.recomposer.util.Constants.CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME
import com.bekmnsrw.recomposer.util.Constants.ON_CREATE_FUN_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_FQ_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_INIT
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_CONFIG_SHORTEN_NAME
import com.bekmnsrw.recomposer.util.Constants.SUPER_ON_CREATE_CALL
import com.bekmnsrw.recomposer.util.addImport
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateActionBase
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName

class CustomizeRecomposerAction : KotlinGenerateActionBase() {

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        (psiFile as? KtFile)?.let { ktPsiFile ->
            val ktPsiFactory = KtPsiFactory(project)
            val ktClass = ktPsiFile.findDescendantOfType<KtClass>() ?: return

            project.executeWriteCommand(CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME) {
                addImport(ktPsiFactory, ktPsiFile, RECOMPOSER_CONFIG_FQ_NAME)
            }

            when (val onCreate = ktClass.findFunctionByName(ON_CREATE_FUN_NAME)) {
                null -> {
                    when (val ktClassBody = ktClass.body) {
                        null -> {
                            project.executeWriteCommand(CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME) {
                                val body = ktPsiFile.addAfter(
                                    ktPsiFactory.createEmptyClassBody(),
                                    ktClass.superTypeListEntries.last()
                                )
                                val recomposerConfigInitWithOnCreateFun = provideRecomposerConfigInitWithOnCreateFun()
                                val recomposerConfigInitWithOnCreateFunPsi = ktPsiFactory.createFunction(recomposerConfigInitWithOnCreateFun)
                                ktPsiFile.addAfter(ktPsiFactory.createNewLine(1), (body as KtClassBody).lBrace)
                                ktPsiFile.addBefore(recomposerConfigInitWithOnCreateFunPsi, body.rBrace)
                            }
                        }
                        else -> {
                            project.executeWriteCommand(CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME) {
                                val recomposerConfigInitWithOnCreateFun = provideRecomposerConfigInitWithOnCreateFun()
                                val recomposerConfigInitWithOnCreateFunPsi = ktPsiFactory.createFunction(recomposerConfigInitWithOnCreateFun)
                                ktPsiFile.addAfter(recomposerConfigInitWithOnCreateFunPsi, ktClassBody.lBrace)
                            }
                        }
                    }
                }
                else -> {
                    val onCreateFun = onCreate as KtNamedFunction
                    val superOnCreateCall = onCreateFun.bodyExpression?.children?.find { it.text == SUPER_ON_CREATE_CALL }
                    val recomposerConfigInit = provideRecomposerConfigInit()
                    val recomposerConfigInitExpression = ktPsiFactory.createExpression(recomposerConfigInit)
                    project.executeWriteCommand(CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME) {
                        val recomposerConfigInitPsi = ktPsiFile.addAfter(recomposerConfigInitExpression, superOnCreateCall)
                        ktPsiFile.addBefore(ktPsiFactory.createWhiteSpace("\n"), recomposerConfigInitPsi)
                    }
                }
            }

            project.executeWriteCommand(CUSTOMIZE_RECOMPOSER_EXECUTE_WRITE_COMMAND_NAME) {
                CodeStyleManager.getInstance(project).reformat(ktPsiFile)
            }
        }
    }

    override fun isValidForClass(targetClass: KtClassOrObject): Boolean {
        return targetClass is KtClass &&
                isApplicationInheritor(targetClass) &&
                !isRecomposerConfigInitAlreadyExists(targetClass)
    }

    private fun isApplicationInheritor(targetClass: KtClassOrObject): Boolean {
        return InheritanceUtil.isInheritor(
            targetClass.toLightClass(),
            APPLICATION
        )
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
