package com.bekmnsrw.recomposer.util

import com.bekmnsrw.recomposer.util.Constants.APPLICATION
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

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

fun isApplicationInheritor(
    targetClass: KtClassOrObject
): Boolean = InheritanceUtil.isInheritor(
    targetClass.toLightClass(),
    APPLICATION
)
