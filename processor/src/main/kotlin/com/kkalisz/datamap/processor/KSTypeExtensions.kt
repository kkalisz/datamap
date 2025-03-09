package com.kkalisz.datamap.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

fun KSType.toTypeNameWithGenerics(): TypeName {
    // Get the classifier declaration, since this represents the type's class or interface
    val classDeclaration = this.declaration as? KSClassDeclaration
        ?: error("Expected type declaration to be a KSClassDeclaration, but got: ${this.declaration}")

    // Convert the KSClassDeclaration into a KotlinPoet ClassName
    val rawClassName = classDeclaration.toClassName()

    // Process generic type arguments if they exist
    return if (this.arguments.isNotEmpty()) {
        val genericTypeNames = this.arguments.map { typeArg ->
            requireNotNull(
                typeArg.type?.resolve()?.toTypeNameWithGenerics(),
                { "Unresolved generic argument: ${typeArg.type?.resolve()?.toClassName()}" })
        }
        rawClassName.parameterizedBy(genericTypeNames)
    } else {
        rawClassName // No generics, just the raw type
    }
}
