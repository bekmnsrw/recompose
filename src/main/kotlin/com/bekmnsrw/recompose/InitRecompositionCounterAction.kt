package com.bekmnsrw.recompose;

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

class InitRecompositionCounterAction : KotlinGenerateActionBase() {

    private companion object {
        const val COMPOSABLE_ANNOTATION_FQ_NAME = "androidx.compose.runtime.Composable"
        const val REMEMBER_FQ_NAME = "androidx.compose.runtime.remember"
        const val LOG_FQ_NAME = "android.util.Log"
        const val SIDE_EFFECT_FQ_NAME = "androidx.compose.runtime.SideEffect"
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        (psiFile as? KtFile)?.let { ktPsiFile ->
            val ktClass = ktPsiFile.findDescendantOfType<KtClass>() ?: return
            val refClassText = provideRefClassText()
            val recompositionCounterText = provideRecompositionCounterText()

            val ktPsiFactory = KtPsiFactory(project)
            val refClass = ktPsiFactory.createClass(refClassText)
            val recompositionCounterFun = ktPsiFactory.createFunction(recompositionCounterText)

            project.executeWriteCommand("InitRecompositionCounterAction") {
                ktPsiFile.add(refClass)
                ktPsiFile.add(recompositionCounterFun)
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
                    "android.app.Application"
                )
    }

    private fun provideRefClassText(): String = """ class Ref(var value: Int) """

    private fun provideRecompositionCounterText(
        message: String = "\$message",
        refValue: String = "\${ref.value}"
    ): String = """
        @$COMPOSABLE_ANNOTATION_FQ_NAME
        inline fun RecompositionCounter(
            tag: String,
            message: String = "",
            shouldLog: (count: Int) -> Boolean = { true }
        ) {
            val ref = $REMEMBER_FQ_NAME { Ref(0) }
            $SIDE_EFFECT_FQ_NAME { ref.value++ }
            if (shouldLog(ref.value)) { 
                $LOG_FQ_NAME.d(tag, "Compositions: $message $refValue") 
            }
        }
    """
}
