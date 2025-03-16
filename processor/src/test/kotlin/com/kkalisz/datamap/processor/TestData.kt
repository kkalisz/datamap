package com.kkalisz.datamap.processor

import com.tschuchort.compiletesting.SourceFile

val complexUserSource = SourceFile.kotlin(
    "ComplexUser.kt", """
            package test

            import com.kkalisz.datamap.MapBuilder
            import com.kkalisz.datamap.BuilderProvider

            data class Address(
                val street: String,
                val city: String,
                val zipCode: String
            )

            @MapBuilder
            data class ComplexUser(
                val name: String,
                val age: Int,
                val email: String?,
                val address: Address,
                val tags: List<String>,
                val scores: Map<String, Int>
            )

            fun main() {
                val address = Address("123 Main St", "City", "12345")
                val user = ComplexUser(
                    name = "John",
                    age = 25,
                    email = "john@example.com",
                    address = address,
                    tags = listOf("user", "active"),
                    scores = mapOf("math" to 90, "science" to 85)
                )

                val user2 = user.buildInstance {
                    put("name", "Jane")
                    put("age", 30)
                    put("email", null)
                    put("address", Address("456 Oak St", "Town", "67890"))
                    put("tags", listOf("user", "premium"))
                    put("scores", mapOf("math" to 95, "science" to 92))
                }

                assert(user2.name == "Jane")
                assert(user2.age == 30)
                assert(user2.email == null)
                assert(user2.address.street == "456 Oak St")
                assert(user2.tags == listOf("user", "premium"))
                assert(user2.scores["math"] == 95)
            }
            """
)
