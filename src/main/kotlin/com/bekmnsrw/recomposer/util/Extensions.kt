package com.bekmnsrw.recomposer.util

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

fun addImport(
    ktPsiFactory: KtPsiFactory,
    ktPsiFile: KtFile,
    fqName: String
) {
    val isImportExists = ktPsiFile.importList?.imports?.any {
        it.importedFqName?.asString() == fqName
    }

    requireNotNull(isImportExists)

    if (isImportExists == false) {
        val importDirective = ktPsiFactory.createImportDirective(
            ImportPath(
                fqName = FqName(fqName),
                isAllUnder = false
            )
        )
        ktPsiFile.importList?.add(importDirective)
    }
}
