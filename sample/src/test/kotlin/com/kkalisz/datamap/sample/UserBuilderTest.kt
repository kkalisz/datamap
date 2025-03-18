package com.kkalisz.datamap.sample

import com.kkalisz.datamap.MapDataBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserBuilderTest {
    @Test
    fun `test create new instance with builder`() {
        val builder = UserBuilder()
        builder.put("id", 1L)
        builder.put("name", "John Doe")
        builder.put("email", "john@example.com")
        builder.put("age", 30)

        val user = builder.build()

        assertEquals(1L, user.id)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals(30, user.age)
        assertTrue(user.isActive) // default value
    }

    @Test
    fun `test modify existing instance`() {
        val user = User(1L, "John Doe", "john@example.com", 30)
        val newUser =
            user
                .toMapBuilder()
                .apply {
                    put("name", "Jane Doe")
                    put("email", null)
                }.build()

        assertEquals(1L, newUser.id)
        assertEquals("Jane Doe", newUser.name)
        assertNull(newUser.email)
        assertEquals(30, newUser.age)
        assertTrue(newUser.isActive)
    }

    @Test
    fun `test get properties from builder`() {
        val builder = UserBuilder()
        builder.put("name", "John Doe")

        assertEquals("John Doe", builder["name"])
        assertNull(builder["email"])
    }

    @Test
    fun `test required properties validation`() {
        val builder = UserBuilder()
        builder.put("name", "John Doe")
        builder.put("age", 30)
        // Missing required 'id' property

        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `test reflection support`() {
        val user = User(1L, "John Doe", "john@example.com", 30)
        assertTrue(user.toMapBuilder() is MapDataBuilder<User>)
        user.toMapBuilder().build()

        val builder = user.toMapBuilder()
        assertTrue(builder is MapDataBuilder<*>)

        val newUser =
            user.buildInstance {
                put("name", "Jane Doe")
            }

        assertEquals("Jane Doe", newUser.name)
    }

    @Test
    fun `test creating instance with null for nullable parameter`() {
        val builder = UserBuilder()
        builder.put("id", 1L)
        builder.put("name", "John Doe")
        builder.put("email", null)
        builder.put("age", 30)

        val user = builder.build()

        assertEquals(1L, user.id)
        assertEquals("John Doe", user.name)
        assertNull(user.email)
        assertEquals(30, user.age)
        assertTrue(user.isActive)
    }

    @Test
    fun `test setting null for non-nullable parameter should fail`() {
        val builder = UserBuilder()
        builder.put("id", 1L)
        builder.put("name", null) // name is non-nullable
        builder.put("email", "john@example.com")
        builder.put("age", 30)

        assertFailsWith<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun `test type validation for nullable parameter`() {
        val builder = UserBuilder()
        builder.put("id", 1L)
        builder.put("name", "John Doe")
        builder.put("email", 123) // Wrong type for nullable email
        builder.put("age", 30)

        assertFailsWith<ClassCastException> {
            builder.build()
        }
    }
}
