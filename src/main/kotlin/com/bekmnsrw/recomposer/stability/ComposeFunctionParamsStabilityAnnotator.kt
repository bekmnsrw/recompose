package com.bekmnsrw.recomposer.stability

import com.bekmnsrw.recomposer.stability.Constants.COMPOSE_PACKAGE_NAME
import com.bekmnsrw.recomposer.stability.Constants.FUN
import com.bekmnsrw.recomposer.stability.Constants.IMMUTABLE_FQ_NAME
import com.bekmnsrw.recomposer.stability.Constants.NON_SKIPPABLE_COMPOSABLE
import com.bekmnsrw.recomposer.stability.Constants.NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE
import com.bekmnsrw.recomposer.stability.Constants.NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE
import com.bekmnsrw.recomposer.stability.Constants.NON_SKIPPABLE_COMPOSABLE_FQ_NAME
import com.bekmnsrw.recomposer.stability.Constants.NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE
import com.bekmnsrw.recomposer.stability.Constants.STABLE_COLLECTIONS
import com.bekmnsrw.recomposer.stability.Constants.STABLE_FQ_NAME
import com.bekmnsrw.recomposer.stability.Constants.SUPPRESS
import com.bekmnsrw.recomposer.stability.Stability.*
import com.bekmnsrw.recomposer.util.Constants.COMPOSABLE_FQ_NAME
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.builtins.isKFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.highlighter.AnnotationHostKind
import org.jetbrains.kotlin.idea.inspections.RemoveAnnotationFix
import org.jetbrains.kotlin.idea.inspections.collections.isCollection
import org.jetbrains.kotlin.idea.quickfix.KotlinSuppressIntentionAction
import org.jetbrains.kotlin.idea.quickfix.RemoveArgumentFix
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isPrimitiveType
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlinx.serialization.compiler.resolve.toClassDescriptor

class HighlightFunctionParametersAction : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is KtNamedFunction || !element.hasAnnotation(COMPOSABLE_FQ_NAME) ||
            !element.isRestartable() || element.hasAnnotation(NON_SKIPPABLE_COMPOSABLE_FQ_NAME)
        ) {
            return
        }

        val nonSkippableParams = mutableMapOf<KtParameter, Unstable>()

        for (valueParameter in element.valueParameters) {
            val returnType = valueParameter.descriptor?.type ?: continue
            val stability = stabilityOf(returnType)
            val fqName = returnType.fqName
            val isRequired = valueParameter.defaultValue == null
            val isFromCompose = fqName?.startsWith(COMPOSE_PACKAGE_NAME) == true

            if (!isFromCompose && stability is Unstable && isRequired) {
                nonSkippableParams += valueParameter to stability
            }
        }

        showMessages(element, holder, nonSkippableParams)
    }

    private fun showMessages(
        function: KtNamedFunction,
        holder: AnnotationHolder,
        nonSkippableParams: Map<KtParameter, Unstable>,
    ) {
        val suppressAnnotation = function.annotationEntries.firstOrNull { annotation ->
            val isSuppress = annotation.getQualifiedName()?.contains(SUPPRESS) == true
            isSuppress && annotation.valueArgumentList?.arguments.orEmpty().any {
                it.getArgumentExpression()?.text?.contains(NON_SKIPPABLE_COMPOSABLE) == true
            }
        }

        val hasUnstableParams = nonSkippableParams.isNotEmpty()
        val isUnstable = suppressAnnotation == null && hasUnstableParams
        val isNowStable = suppressAnnotation != null && !hasUnstableParams

        when {
            isUnstable -> {
                val kind = AnnotationHostKind(FUN, function.name.orEmpty(), newLineNeeded = true)
                val intentionAction = KotlinSuppressIntentionAction(function, NON_SKIPPABLE_COMPOSABLE, kind)

                holder.newAnnotation(HighlightSeverity.ERROR, NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE)
                    .range(function.nameIdentifier?.originalElement ?: function.originalElement)
                    .withFix(intentionAction)
                    .create()

                nonSkippableParams.forEach { (param, stability) ->
                    holder.newAnnotation(HighlightSeverity.ERROR, "Parameter '${param.name}' is UNSTABLE. Reason: ${stability.reason} Change these arguments so that they become stable")
                        .range(param.originalElement)
                        .withFix(intentionAction)
                        .create()
                }
            }

            isNowStable -> {
                if (suppressAnnotation?.valueArguments?.size == 1) {
                    holder.newAnnotation(HighlightSeverity.WARNING, NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE)
                        .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                        .range(suppressAnnotation.originalElement)
                        .newFix(RemoveAnnotationFix(NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE, suppressAnnotation))
                        .registerFix()
                        .create()
                    return
                }
                suppressAnnotation?.valueArgumentList?.arguments.orEmpty()
                    .filter { it.getArgumentExpression()?.text?.contains(NON_SKIPPABLE_COMPOSABLE) == true }
                    .forEach { annotationValue ->
                        holder.newAnnotation(
                            HighlightSeverity.WARNING,
                            "Remove unused $NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE"
                        )
                            .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                            .range(annotationValue.getArgumentExpression()!!.originalElement)
                            .newFix(RemoveArgumentFix(annotationValue))
                            .registerFix()
                            .create()
                    }
            }
        }
    }
}

private fun stabilityOf(kotlinType: KotlinType): Stability = when {
    KotlinBuiltIns.isPrimitiveType(kotlinType) ||
            kotlinType.isSyntheticComposableFunction() ||
            kotlinType.isEnum() ||
            KotlinBuiltIns.isString(kotlinType) -> Stable

    kotlinType.isFunctionType || kotlinType.isKFunctionType -> {
        val unstableArgs = checkArgumentStability(kotlinType.arguments)
        if (unstableArgs.isEmpty()) Stable else Unstable(unstableArgs)
    }

    kotlinType.isNullable() -> stabilityOf(kotlinType.makeNotNullable())

    kotlinType.isCollection() -> {
        val isStableCollection = kotlinType.fqName.toString() in STABLE_COLLECTIONS
        val unstableArgs = checkArgumentStability(kotlinType.arguments)

        when {
            isStableCollection && unstableArgs.isEmpty() -> Stable
            !isStableCollection && unstableArgs.isNotEmpty() -> Unstable("1) Collection '${kotlinType.fqName}' is UNSTABLE. Use appropriate collection from 'kotlinx.collections.immutable' instead. 2) " + unstableArgs)
            isStableCollection -> Unstable("Collection '${kotlinType.fqName}' is UNSTABLE. Use appropriate collection from 'kotlinx.collections.immutable' instead.")
            unstableArgs.isNotEmpty() -> Unstable(unstableArgs)
            else -> Unknown
        }
    }

    kotlinType.constructor.declarationDescriptor is ClassDescriptor -> {
        val classDescriptor = kotlinType.toClassDescriptor
        if (classDescriptor != null) {
            if (classDescriptor.annotations.hasAnnotation(STABLE_FQ_NAME) ||
                classDescriptor.annotations.hasAnnotation(IMMUTABLE_FQ_NAME)) {
                Stable
            } else {
                stabilityOf(classDescriptor)
            }
        } else Unknown
    }

    else -> Unknown
}

private fun stabilityOf(declaration: ClassDescriptor): Stability {
    if (declaration.kind.isEnumClass) return Stable
    if (declaration.defaultType.isPrimitiveType()) return Stable

    for (member in declaration.unsubstitutedMemberScope.getDescriptorsFiltered()) {
        when (member) {
            is PropertyDescriptor -> {
                if (!member.kind.isReal) continue

                if (member.getter?.isDefault == false && !member.isVar && !member.isDelegated) continue

                if (member.isVar && !member.isDelegated) {
                    return Unstable("type '${declaration.name}' contains non-delegated var '${member.name}'.")
                }
            }
        }
    }
    return Stable
}

private fun checkArgumentStability(arguments: List<TypeProjection>): String {
    var unstableArgs = ""

    arguments.forEach { argument ->
        val stability = stabilityOf(argument.type)
        if (stability is Unstable) {
            unstableArgs += "${argument.type} is UNSTABLE because ${stability.reason} "
        }
    }

    return unstableArgs
}
