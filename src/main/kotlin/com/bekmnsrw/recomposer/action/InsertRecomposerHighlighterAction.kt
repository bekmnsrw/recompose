package com.bekmnsrw.recomposer.action

import com.bekmnsrw.recomposer.util.Constants.INSERT_RECOMPOSER_HIGHLIGHTER
import com.bekmnsrw.recomposer.util.Constants.MODIFIER
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_HIGHLIGHTER_FQ_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_HIGHLIGHTER_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_NAME
import com.bekmnsrw.recomposer.util.Constants.provideRecomposerHighlighter
import com.bekmnsrw.recomposer.util.addImport
import com.bekmnsrw.recomposer.util.isComposableFun
import com.bekmnsrw.recomposer.util.isContainsString
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtReferenceExpression

class InsertRecomposerHighlighterAction : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = RECOMPOSER_NAME

    override fun getText(): String = INSERT_RECOMPOSER_HIGHLIGHTER

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        psiElement: PsiElement
    ): Boolean {
        val isPsiElementIsKtReferenceExpression = psiElement.context is KtReferenceExpression
        val ktCallExpression = psiElement.context?.parent as KtCallExpression

        return when {
            isPsiElementIsKtReferenceExpression &&
                    isComposableFun(psiElement) &&
                    !isContainsString(
                        ktCallExpression = ktCallExpression,
                        string = RECOMPOSER_HIGHLIGHTER_NAME
                    ) -> true

            else -> false
        }
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        psiElement: PsiElement
    ) {
        val ktPsiFactory = KtPsiFactory(project)
        val ktPsiFile = psiElement.containingFile as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            addImport(ktPsiFactory, ktPsiFile, RECOMPOSER_HIGHLIGHTER_FQ_NAME)
        }

        val ktCallExpression = psiElement.context?.parent as KtCallExpression

        when (
            isContainsString(
                ktCallExpression = ktCallExpression,
                string = MODIFIER
            )
        ) {
            true -> {
                val oldModifier = ktCallExpression.valueArguments.find { it.text.contains(MODIFIER) }
                val newModifier = addRecomposerHighlighter(modifier = oldModifier!!.text)
                val newModifierExpression = ktPsiFactory.createExpression(newModifier)
                WriteCommandAction.runWriteCommandAction(project) {
                    oldModifier.replace(newModifierExpression)
                }
            }

            false -> {
                val recomposerHighlighterExpression = ktPsiFactory.createArgument(provideRecomposerHighlighter())
                WriteCommandAction.runWriteCommandAction(project) {
                    val modifierPsi = ktPsiFile.addBefore(recomposerHighlighterExpression, ktCallExpression.valueArguments.first())
                    val comma = ktPsiFile.addAfter(ktPsiFactory.createComma(), modifierPsi)
                    ktPsiFile.addAfter(ktPsiFactory.createNewLine(1), comma)
                }
            }
        }

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(ktPsiFile)
        }
    }

    private fun addRecomposerHighlighter(modifier: String): String {
        var spaceCounter = 0

        for (i in modifier.trim().length - 1 downTo 0) {
            when {
                modifier[i] != ' ' && spaceCounter == 0 -> continue
                modifier[i] != ' ' && spaceCounter > 0 -> break
                else -> spaceCounter++
            }
        }

        val newModifier = modifier + "\n" + " ".repeat(spaceCounter) + ".$RECOMPOSER_HIGHLIGHTER_NAME()"

        return if (newModifier.contains("modifier")) newModifier else "modifier = $newModifier"
    }
}
