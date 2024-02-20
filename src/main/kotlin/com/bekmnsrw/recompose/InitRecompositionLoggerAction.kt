package com.bekmnsrw.recompose;

import com.bekmnsrw.recompose.util.Constants.APPLICATION
import com.bekmnsrw.recompose.util.Constants.COMPOSABLE_ANNOTATION_FQ_NAME
import com.bekmnsrw.recompose.util.Constants.CURRENT_RECOMPOSE_SCOPE_FQ_NAME
import com.bekmnsrw.recompose.util.Constants.LOG_FQ_NAME
import com.bekmnsrw.recompose.util.Constants.REMEMBER_FQ_NAME
import com.bekmnsrw.recompose.util.Constants.SIDE_EFFECT_FQ_NAME
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateActionBase
import org.jetbrains.kotlin.idea.codeinsight.utils.commitAndUnblockDocument
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

class InitRecompositionLoggerAction : KotlinGenerateActionBase() {

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        (psiFile as? KtFile)?.let { ktPsiFile ->
            val ktClass = ktPsiFile.findDescendantOfType<KtClass>() ?: return
            val recompositionCounterClassText = provideRecompositionCounterClassText()
            val recompositionLoggerText = provideRecompositionLoggerText()

            val ktPsiFactory = KtPsiFactory(project)
            val recompositionCounterClass = ktPsiFactory.createClass(recompositionCounterClassText)
            val recompositionLoggerFun = ktPsiFactory.createFunction(recompositionLoggerText)

            project.executeWriteCommand("InitRecompositionCounterAction") {
                ktPsiFile.add(recompositionCounterClass)
                ktPsiFile.add(recompositionLoggerFun)
                ktClass.containingKtFile.commitAndUnblockDocument()
                ShortenReferences.DEFAULT.process(ktPsiFile)
                CodeStyleManager.getInstance(project).reformat(ktClass)
            }
        }
    }

    override fun isValidForClass(targetClass: KtClassOrObject): Boolean {
        return targetClass is KtClass &&
                InheritanceUtil.isInheritor(
                    targetClass.toLightClass(),
                    APPLICATION
                )
    }

    private fun provideRecompositionCounterClassText(): String = """ class RecompositionCounter(var value: Int) """

    private fun provideRecompositionLoggerText(): String = """
        @$COMPOSABLE_ANNOTATION_FQ_NAME
        inline fun RecompositionLogger(
            tag: String = "RecompositionLogger",
            message: String,
            shouldLog: (count: Int) -> Boolean = { true }
        ) {
            val recompositionCounter = $REMEMBER_FQ_NAME { RecompositionCounter(0) }
            $SIDE_EFFECT_FQ_NAME { recompositionCounter.value++ }
            if (shouldLog(recompositionCounter.value)) { 
                $LOG_FQ_NAME.d(tag, "${'$'}message; ${'$'}{recompositionCounter.value}; ${'$'}${'{'}$CURRENT_RECOMPOSE_SCOPE_FQ_NAME${'}'}") 
            }
        }
    """
}
