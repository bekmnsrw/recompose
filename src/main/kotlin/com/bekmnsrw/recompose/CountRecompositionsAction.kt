package com.bekmnsrw.recompose;

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.actions.generate.KotlinGenerateActionBase
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject

class CountRecompositionsAction : KotlinGenerateActionBase() {

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        println("INVOKE")
    }

    override fun isValidForClass(targetClass: KtClassOrObject): Boolean {
        return targetClass is KtClass
    }
}
