package com.bekmnsrw.recompose

import com.bekmnsrw.recompose.util.Constants.APPLICATION
import com.bekmnsrw.recompose.util.Constants.BLOCK_ELEMENT_TYPE
import com.bekmnsrw.recompose.util.Constants.COMPOSABLE_ANNOTATION_SHORT_NAME
import com.bekmnsrw.recompose.util.Constants.FUNCTION_LITERAL_ELEMENT_TYPE
import com.bekmnsrw.recompose.util.Constants.FUN_ELEMENT_TYPE
import com.bekmnsrw.recompose.util.Constants.RECOMPOSITION_LOGGER_FUN_NAME
import com.bekmnsrw.recompose.util.Constants.RECOMPOSITION_LOGGER_REFERENCE
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.core.getFqNameWithImplicitPrefixOrRoot
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

class ImportRecompositionLoggerAction : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "RecompositionLogger @Composable function"

    override fun getText(): String = "Import RecompositionLogger @Composable function"

    override fun isAvailable(project: Project, editor: Editor?, psiElement: PsiElement): Boolean {
        val psiElementContext = psiElement.context

        if (psiElementContext.toString() == BLOCK_ELEMENT_TYPE && psiElementContext?.parent?.elementType.toString() == FUN_ELEMENT_TYPE) {
            val function = psiElementContext?.parent as KtFunction

            if (function.name == RECOMPOSITION_LOGGER_FUN_NAME) return false

            function.annotationEntries.forEach {
                if (it.shortName.toString() == COMPOSABLE_ANNOTATION_SHORT_NAME) {
//                    println("Composable")
                    return true
                }
            }
        }

//        println("Not Composable")

        return psiElementContext.toString() == FUNCTION_LITERAL_ELEMENT_TYPE
    }

    override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
        val app = JavaPsiFacade.getInstance(project).findClass(APPLICATION, GlobalSearchScope.allScope(project))
        val appInheritor = ClassInheritorsSearch.search(app!!).first()
        val appPackageName = PsiUtil.getPackageName(appInheritor)
        val filePackageName = psiElement.containingFile.containingDirectory.getFqNameWithImplicitPrefixOrRoot().asString()
        val ktPsiFactory = KtPsiFactory(project)
        val recompositionLoggerFun: String = provideRecompositionLoggerReference()

        // Make import
        if (filePackageName != appPackageName) {
            val psiFile = psiElement.containingFile as? KtFile ?: return
            val recompositionLoggerImport = provideRecompositionLoggerImport(appPackageName)

            if (!psiFile.importList?.imports?.any { it.importedFqName?.asString() == recompositionLoggerImport }!!) {
                val importDirective = ktPsiFactory.createImportDirective(ImportPath(FqName(recompositionLoggerImport), false))
                psiFile.importList?.add(importDirective)
            }
        }

        val document = editor?.document ?: return
        val psiFile = psiElement.containingFile

        val lineNumber = document.getLineNumber(psiElement.textOffset)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val newLine = "\n${ktPsiFactory.createExpression(recompositionLoggerFun).text}"

        document.insertString(lineEndOffset, newLine)

        CodeStyleManager.getInstance(project).reformatText(psiFile, lineEndOffset, lineEndOffset + newLine.length)
    }

    private fun provideRecompositionLoggerReference(): String = "$RECOMPOSITION_LOGGER_REFERENCE(message = \"\")"

    private fun provideRecompositionLoggerImport(
        appPackageName: String?
    ): String = "$appPackageName.$RECOMPOSITION_LOGGER_REFERENCE"
}
