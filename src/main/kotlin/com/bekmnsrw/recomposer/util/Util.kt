package com.bekmnsrw.recomposer.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.ImportPath
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

fun addImport(
    ktPsiFactory: KtPsiFactory,
    ktPsiFile: KtFile,
    fqName: String
) {
    val isImportExists = ktPsiFile.importList?.imports?.any { ktImportDirective ->
        ktImportDirective.importedFqName?.asString() == fqName
    }

    requireNotNull(isImportExists)

    if (isImportExists == false) {
        ktPsiFile.importList?.add(
            ktPsiFactory.createImportDirective(
                ImportPath(
                    fqName = FqName(fqName),
                    isAllUnder = false
                )
            )
        )
    }
}

fun isApplicationInheritor(targetClass: KtClassOrObject): Boolean {
    return InheritanceUtil.isInheritor(
        targetClass.toLightClass(),
        Constants.APPLICATION
    )
}

fun isComposableFun(psiElement: PsiElement): Boolean {
    val bindingContext = (psiElement.containingFile as KtFile).analyze(BodyResolveMode.FULL)
    val ktReferenceExpression = psiElement.context as KtReferenceExpression
    val resolvedPsiElement = ktReferenceExpression.references.firstOrNull()?.resolve()
    resolvedPsiElement?.let { psi ->
        bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, psi]?.annotations?.forEach { annotation ->
            if (annotation.fqName == FqName(Constants.COMPOSABLE_FQ_NAME)) return true
        }
        val fqName = psi.kotlinFqName.toString()
        if (fqName.contains(Constants.MATERIAL) || fqName.contains(Constants.MATERIAL3)) return true
    }
    return false
}

fun isContainsString(ktCallExpression: KtCallExpression, string: String): Boolean {
    ktCallExpression.valueArguments.forEach { ktValueArgument ->
        if (ktValueArgument.text.contains(string)) return true
    }
    return false
}
