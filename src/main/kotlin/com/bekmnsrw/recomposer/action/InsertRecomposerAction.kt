package com.bekmnsrw.recomposer.action

import com.bekmnsrw.recomposer.util.Constants.BLOCK_ELEMENT_TYPE
import com.bekmnsrw.recomposer.util.Constants.COMPOSABLE_SHORT_NAME
import com.bekmnsrw.recomposer.util.Constants.FUNCTION_LITERAL_ELEMENT_TYPE
import com.bekmnsrw.recomposer.util.Constants.FUN_ELEMENT_TYPE
import com.bekmnsrw.recomposer.util.Constants.INSERT_RECOMPOSER
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_NAME
import com.bekmnsrw.recomposer.util.Constants.RECOMPOSER_FQ_NAME
import com.bekmnsrw.recomposer.util.Constants.provideRecomposerFunReference
import com.bekmnsrw.recomposer.util.addImport
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.psi.*

class InsertRecomposerAction : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = RECOMPOSER_NAME

    override fun getText(): String = INSERT_RECOMPOSER

    override fun isAvailable(project: Project, editor: Editor?, psiElement: PsiElement): Boolean {
        val psiElementContext = psiElement.context.toString()
        val psiElementContextParent = psiElement.context?.parent

        if (psiElementContext == BLOCK_ELEMENT_TYPE && psiElementContextParent.elementType.toString() == FUN_ELEMENT_TYPE) {
            (psiElementContextParent as KtFunction).annotationEntries.forEach { annotationEntry ->
                if (annotationEntry.shortName.toString() == COMPOSABLE_SHORT_NAME) return true
            }
        }

        return psiElementContext == FUNCTION_LITERAL_ELEMENT_TYPE
    }

    override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
        val ktPsiFactory = KtPsiFactory(project)
        val ktPsiFile = psiElement.containingFile as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            addImport(ktPsiFactory, ktPsiFile, RECOMPOSER_FQ_NAME)
        }

        val recomposerExpression = ktPsiFactory.createExpression(provideRecomposerFunReference())
        val cursorOffset = editor?.caretModel?.offset ?: return
        val psiUnderCursor = ktPsiFile.findElementAt(cursorOffset) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            val recomposerPsi = ktPsiFile.addBefore(recomposerExpression, psiUnderCursor) ?: return@runWriteCommandAction
            val newLine = ktPsiFactory.createNewLine(1)
            ktPsiFile.addBefore(newLine, recomposerPsi) ?: return@runWriteCommandAction
        }

        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(ktPsiFile)
        }
    }
}
