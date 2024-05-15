package com.bekmnsrw.recomposer.util

import com.bekmnsrw.recomposer.util.Constants.COMPOSABLE_FUNCTION
import com.bekmnsrw.recomposer.util.Constants.EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME
import com.bekmnsrw.recomposer.util.Constants.NON_RESTARTABLE_COMPOSABLE_FQ_NAME
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnit

fun KtNamedFunction.hasAnnotation(fqString: String): Boolean =
    annotationEntries.any { annotation -> annotation.fqNameMatches(fqString) }

fun KtNamedFunction.hasAnnotation(fqName: FqName): Boolean = hasAnnotation(fqName.asString())

fun KtAnnotationEntry.fqNameMatches(fqName: String): Boolean {
    val shortName = shortName?.asString() ?: return false
    return fqName.endsWith(shortName) && fqName == getQualifiedName()
}

fun KtAnnotationEntry.getQualifiedName(): String? {
    return analyze(BodyResolveMode.PARTIAL).get(BindingContext.ANNOTATION, this)?.fqName?.asString()
}

fun KtNamedFunction.isRestartable(): Boolean = when {
    isLocal -> false
    hasModifier(KtTokens.INLINE_KEYWORD) -> false
    hasAnnotation(NON_RESTARTABLE_COMPOSABLE_FQ_NAME) -> false
    hasAnnotation(EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME) -> false
    resolveToDescriptorIfAny()?.returnType?.isUnit() == false -> false
    else -> true
}

fun KotlinType.isSyntheticComposableFunction() = fqName?.asString()
    .orEmpty()
    .startsWith(COMPOSABLE_FUNCTION)

internal fun PropertyDescriptor.resolveDelegateType(): KotlinType? {
    val expression = (this.findPsi() as? KtProperty)?.delegateExpression
    val bindingContext = expression?.analyze() ?: return null
    return expression.getType(bindingContext)
}
