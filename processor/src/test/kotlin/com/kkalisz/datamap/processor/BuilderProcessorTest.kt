@file:OptIn(ExperimentalCompilerApi::class)

package com.kkalisz.datamap.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.File

class BuilderProcessorTest {
    private fun getGeneratedFile(compilation: KotlinCompilation, fileName: String): File? {
        val kspDir = File(compilation.workingDir, "ksp/sources/kotlin")
        return kspDir.walkTopDown().find { it.name == fileName }
    }

    @Test
    fun `test simple compilation`() {
        val source = SourceFile.kotlin(
            "Simple.kt", """
            fun main() {
                println("Hello, World!")
            }
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `test builder generation for simple data class`() {
        val source = SourceFile.kotlin(
            "User.kt", """
            package test

            import com.kkalisz.datamap.MapBuilder

            @MapBuilder
            data class User(
                val name: String,
                val email: String?
            )
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = getGeneratedFile(compilation, "UserBuilder.kt")
        assertNotNull(generatedFile, "Builder file was not generated")
    }

    @Test
    fun `test builder contains method`() {
        val source = SourceFile.kotlin(
            "User.kt", """
            package test

            import com.kkalisz.datamap.MapBuilder

            @MapBuilder
            data class User(
                val name: String,
                val age: Int
            )
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = getGeneratedFile(compilation, "UserBuilder.kt")
        assertNotNull(generatedFile, "Builder file was not generated")

        val generatedCode = generatedFile.readText()

        assertTrue(
            generatedCode.contains("override fun contains(name: String): Boolean = values.containsKey(name)\n"),
            "contains method was not generated"
        )
    }

    @Test
    fun `test builder usage with buildInstance`() {
        val source = SourceFile.kotlin(
            "User.kt", """
            package test

            import com.kkalisz.datamap.MapBuilder
            import com.kkalisz.datamap.BuilderProvider

            @MapBuilder
            data class User(
                val name: String,
                val age: Int,
                val email: String?
            )

            fun main() {
                val user = User("John", 25, "john@example.com")
                val user2 = user.buildInstance {
                    put("name", "Jane")
                    put("age", 30)
                    put("email", null)
                }
                assert(user2 == User("Jane", 30, null))
            }
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = getGeneratedFile(compilation, "UserBuilder.kt")
        assertNotNull(generatedFile, "Builder file was not generated")

        val generatedCode = generatedFile.readText()

        assertTrue(generatedCode.contains("class UserBuilder"), "Builder class was not generated")
        assertTrue(generatedCode.contains("fun User.toMapBuilder"), "mapBuilder method was not generated")
    }

    @Test
    fun `test type safety in builder`() {
        val source = SourceFile.kotlin(
            "User.kt", """
            package test

            import com.kkalisz.datamap.MapBuilder
            import com.kkalisz.datamap.BuilderProvider

            @MapBuilder
            data class User(
                val name: String,
                val age: Int
            )

            fun main() {
                val user = User("John", 25)
                try {
                    val user2 = user.buildInstance {
                        put("name", 42) // Wrong type: Int instead of String
                    }
                    throw AssertionError("Should have thrown ClassCastException")
                } catch (e: ClassCastException) {
                    // Expected exception
                }

                try {
                    val user3 = user.buildInstance {
                        put("age", "30") // Wrong type: String instead of Int
                    }
                    throw AssertionError("Should have thrown ClassCastException")
                } catch (e: ClassCastException) {
                    // Expected exception
                }
            }
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = getGeneratedFile(compilation, "UserBuilder.kt")
        assertNotNull(generatedFile, "Builder file was not generated")

        val generatedCode = generatedFile.readText()
        assertTrue(generatedCode.contains("class UserBuilder"), "Builder class was not generated")
        assertTrue(generatedCode.contains("fun User.toMapBuilder"), "mapBuilder method was not generated")

    }

    @Test
    fun `test complex data class with multiple properties`() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(complexUserSource)
            inheritClassPath = true
            messageOutputStream = System.out
            configureKsp(useKsp2 = true){
                useKapt4 = true
                jvmTarget = "21"
                symbolProcessorProviders.add(BuilderProcessorProvider())
            }
        }

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = getGeneratedFile(compilation, "ComplexUserBuilder.kt")
        assertNotNull(generatedFile, "Builder file was not generated")
        val generatedCode = generatedFile.readText()
        assertTrue(generatedCode.contains("class ComplexUserBuilder"), "Builder class was not generated")
        assertTrue(generatedCode.contains("fun ComplexUser.toMapBuilder()"), "mapBuilder method was not generated")
    }
}
