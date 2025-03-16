package com.kkalisz.datamap.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.kkalisz.datamap.MapBuilder
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

/**
 * A Kotlin Symbol Processor that generates builder classes for data classes annotated with @MapBuilder.
 * The generated builders provide a type-safe way to construct instances of the target class using a map-based approach.
 *
 * Features:
 * - Generates a builder class for each annotated data class
 * - Handles nullable and non-nullable properties
 * - Supports properties with default values
 * - Provides extension functions for convenient instance creation and modification
 */
class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    companion object {
        private const val BUILDER_SUFFIX = "Builder"
        private const val DATA_CLASS_ERROR_MESSAGE = "@MapBuilder can only be applied to data classes"
        private const val PACKAGE_NAME = "com.kkalisz.datamap"
        private const val MAP_DATA_BUILDER_CLASS = "MapDataBuilder"
        private const val BUILDER_PROVIDER_CLASS = "BuilderProvider"

        // Common function names
        private const val CONTAINS_FUNCTION = "contains"
        private const val GET_FUNCTION = "get"
        private const val PUT_FUNCTION = "put"
        private const val BUILD_FUNCTION = "build"
        private const val TO_MAP_BUILDER_FUNCTION = "toMapBuilder"
        private const val BUILD_INSTANCE_FUNCTION = "buildInstance"
        private const val VALUES_PROPERTY = "values"
        private const val NAME_PARAM = "name"
        private const val VALUE_PARAM = "value"

        // Common type names
        private val MAP_TYPE = ClassName("kotlin.collections", "MutableMap")
        private val MAP_DATA_BUILDER_TYPE = ClassName(PACKAGE_NAME, MAP_DATA_BUILDER_CLASS)
        private val BUILDER_PROVIDER_TYPE = ClassName(PACKAGE_NAME, BUILDER_PROVIDER_CLASS)

        // Common code patterns
        private const val APPLY_BLOCK_START = "apply {\n"
        private const val APPLY_BLOCK_END = "}"
    }

    /**
     * Main processing method that handles all classes annotated with @MapBuilder.
     * This method is called by the KSP framework during compilation.
     *
     * @param resolver The symbol resolver provided by KSP
     * @return List of symbols that couldn't be processed (invalid or incomplete)
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        @Suppress("UnsafeCallOnNullableType")
        val symbols = resolver.getSymbolsWithAnnotation(MapBuilder::class.qualifiedName!!)
        val unprocessed = symbols.filter { !it.validate() }.toList()

        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { processClass(it as KSClassDeclaration) }

        return unprocessed
    }

    /**
     * Processes a single class declaration and generates the corresponding builder class.
     * Validates that the class is a data class and generates all necessary code files.
     *
     * @param classDecl The class declaration to process
     */
    private fun processClass(classDecl: KSClassDeclaration) {
        if (!classDecl.modifiers.contains(Modifier.DATA)) {
            logger.error(DATA_CLASS_ERROR_MESSAGE, classDecl)
            return
        }

        val className = classDecl.simpleName.asString()
        val packageName = classDecl.packageName.asString()
        val builderClassName = "$className$BUILDER_SUFFIX"

        val properties = classDecl.getAllProperties().toList()

        val fileSpec = FileSpec.builder(packageName, builderClassName)
            .addImport(PACKAGE_NAME, "getRequiredValueOrThrow")
            .addImport(PACKAGE_NAME, "getNotRequiredValueOrThrow")
            .addType(generateBuilderClass(classDecl, properties))
            .addFunction(generateMapBuilderExtension(classDecl))
            .addFunction(generateBuildInstanceExtension(classDecl))
            .build()

        fileSpec.writeTo(codeGenerator, false)
    }

    private fun generateBuilderClass(
        classDecl: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>
    ): TypeSpec {
        val className = classDecl.simpleName.asString()
        val classType = classDecl.asType(emptyList())

        return TypeSpec.classBuilder("$className$BUILDER_SUFFIX")
            .addSuperinterface(MAP_DATA_BUILDER_TYPE.parameterizedBy(classType.toClassName()))
            .addProperty(
                PropertySpec.builder(VALUES_PROPERTY, MAP_TYPE.parameterizedBy(STRING, ANY.copy(nullable = true)))
                    .initializer("mutableMapOf()")
                    .build()
            )
            .addFunction(generateContainsFunction())
            .addFunction(generateGetFunction())
            .addFunction(generatePutFunction())
            .addFunction(generateBuildFunction(classDecl, properties))
            .build()
    }

    private fun generateContainsFunction(): FunSpec {
        return FunSpec.builder(CONTAINS_FUNCTION)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(NAME_PARAM, String::class)
            .returns(Boolean::class)
            .addStatement("return $VALUES_PROPERTY.containsKey($NAME_PARAM)")
            .build()
    }

    private fun generateGetFunction(): FunSpec {
        return FunSpec.builder(GET_FUNCTION)
            .addModifiers(KModifier.OVERRIDE)
            .addModifiers(KModifier.OPERATOR)
            .addParameter(NAME_PARAM, String::class)
            .returns(Any::class.asClassName().copy(nullable = true))
            .addStatement("return $VALUES_PROPERTY[$NAME_PARAM]")
            .build()
    }

    private fun generatePutFunction(): FunSpec {
        return FunSpec.builder(PUT_FUNCTION)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(NAME_PARAM, String::class)
            .addParameter(VALUE_PARAM, Any::class.asClassName().copy(nullable = true))
            .addStatement("$VALUES_PROPERTY[$NAME_PARAM] = $VALUE_PARAM")
            .build()
    }

    /**
     * Generates code for handling a nullable property in the build function.
     */
    private fun generateNullablePropertyCode(
        codeBlock: CodeBlock.Builder,
        propName: String,
        typeName: TypeName
    ) {
        codeBlock.add(
            "%L = $VALUES_PROPERTY.getNotRequiredValueOrThrow<%T>(%S)",
            propName,
            typeName,
            propName
        )
    }

    /**
     * Generates code for handling a required (non-nullable) property without default value.
     */
    private fun generateRequiredPropertyCode(
        codeBlock: CodeBlock.Builder,
        propName: String,
        typeName: TypeName
    ) {
        codeBlock.add(
            "%L = $VALUES_PROPERTY.getRequiredValueOrThrow<%T>(%S)",
            propName,
            typeName,
            propName
        )
    }

    /**
     * Generates the build function that creates an instance of the target class from the collected values.
     * Handles different property types (nullable, required, with defaults) appropriately.
     */
    private fun generateBuildFunction(
        classDecl: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>
    ): FunSpec {
        val className = classDecl.asType(emptyList()).toClassName()

        val codeBlock = CodeBlock.builder()
            .add("return %T(\n", className)
            .indent()

        properties.forEachIndexed { index, prop ->
            val propName = prop.simpleName.asString()
            val propType = prop.type.resolve()
            val typeName = propType.toTypeNameWithGenerics()

            when {
                propType.isMarkedNullable -> generateNullablePropertyCode(codeBlock, propName, typeName)
                else -> generateRequiredPropertyCode(codeBlock, propName, typeName)
            }

            codeBlock.add(if (index < properties.size - 1) ",\n" else "\n")
        }

        codeBlock.unindent().add(")")

        return FunSpec.builder(BUILD_FUNCTION)
            .addModifiers(KModifier.OVERRIDE)
            .returns(className)
            .addCode(codeBlock.build())
            .build()
    }

    /**
     * Generates an extension function that converts a class instance to its builder representation.
     * This allows for easy modification of existing instances.
     */
    private fun generateMapBuilderExtension(classDecl: KSClassDeclaration): FunSpec {
        val className = classDecl.asType(emptyList()).toClassName()
        val builderClassName = ClassName(className.packageName, "${className.simpleName}$BUILDER_SUFFIX")
        val copyBuilderType = MAP_DATA_BUILDER_TYPE.parameterizedBy(className)

        val codeBlock = CodeBlock.builder()
            .add("return %T().$APPLY_BLOCK_START", builderClassName)
            .indent()

        classDecl.getAllProperties().forEach { prop ->
            val propName = prop.simpleName.asString()
            codeBlock.addStatement("$VALUES_PROPERTY[%S] = this@$TO_MAP_BUILDER_FUNCTION.%N", propName, propName)
        }

        codeBlock.unindent()
            .add(APPLY_BLOCK_END)

        return FunSpec.builder(TO_MAP_BUILDER_FUNCTION)
            .receiver(className)
            .returns(copyBuilderType)
            .addCode(codeBlock.build())
            .build()
    }

    /**
     * Generates an extension function for BuilderProvider that creates a new instance using a builder pattern.
     * This provides a convenient way to create new instances with a DSL-like syntax.
     */
    private fun generateBuildInstanceExtension(classDecl: KSClassDeclaration): FunSpec {
        val className = classDecl.asType(emptyList()).toClassName()
        val copyBuilderType = MAP_DATA_BUILDER_TYPE.parameterizedBy(className)
        val builderProviderType = BUILDER_PROVIDER_TYPE.parameterizedBy(className)

        return FunSpec.builder(BUILD_INSTANCE_FUNCTION)
            .receiver(className)
            .addParameter(
                ParameterSpec.builder(
                    "initialize",
                    LambdaTypeName.get(
                        receiver = copyBuilderType,
                        returnType = Unit::class.asClassName()
                    )
                ).build()
            )
            .returns(className)
            .addCode(
                CodeBlock.builder()
                    .addStatement("return toMapBuilder().apply(initialize).build()")
                    .build()
            )
            .build()
    }
}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger)
    }
}
