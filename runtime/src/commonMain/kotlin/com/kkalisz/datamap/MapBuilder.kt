package com.kkalisz.datamap

/**
 * Annotation that generates a builder class for the annotated data class.
 * This annotation is supported across multiple platforms including JVM, JS, iOS, and WASM.
 *
 * Supported platforms:
 * - JVM (including Android)
 * - JavaScript (both browser and Node.js)
 * - iOS (arm64, x64, simulator)
 * - WASM (experimental support)
 *
 * Example usage:
 * ```kotlin
 * @MapBuilder
 * data class User(
 *     val name: String,
 *     val email: String?
 * )
 *
 * // Generated builder can be used like this:
 * val builder = UserBuilder()
 * builder.put("name", "John")
 * builder.put("email", "john@example.com")
 * val user = builder.build()
 * ```
 *
 * The generated builder code is platform-specific but provides the same API across all supported platforms.
 * This allows you to use the same code pattern regardless of your target platform.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class MapBuilder

/**
 * Interface for generated builder classes that provides map-like access to properties.
 *
 * @param T The type of object being built
 */
public interface MapDataBuilder<T> {
    /**
     * Checks if a property exists by its name.
     *
     * @param name The name of the property to check
     * @return true if the property exists, false otherwise
     */
    public fun contains(name: String): Boolean

    /**
     * Gets the value of a property by its name.
     *
     * @param name The name of the property to get
     * @return The value of the property, or null if it doesn't exist
     */
    public fun get(name: String): Any?

    /**
     * Sets the value of a property by its name.
     *
     * @param name The name of the property to set
     * @param value The value to set
     */
    public fun put(name: String, value: Any?)

    /**
     * Builds and returns the final object.
     *
     * @return The built object
     */
    public fun build(): T
}

/**
 * Extension function for safely getting and casting a value from a map.
 * Throws appropriate errors if the value does not exist or cannot be cast to the expected type.
 *
 * @param T The expected type of the value.
 * @param fieldName The field name to look up in the map.
 * @return The value cast to the expected type.
 * @throws IllegalStateException if the value is null or missing.
 * @throws ClassCastException if the value cannot be cast to the expected type.
 */
public inline fun <reified T> Map<String, Any?>.getRequiredValueOrThrow(fieldName: String): T {
    val value = getNotRequiredValueOrThrow<T>(fieldName)
    return checkNotNull(value) { "Property '$fieldName' is required but was null or missing." }
}

public inline fun <reified T> Map<String, Any?>.getNotRequiredValueOrThrow(fieldName: String): T? {
    val value = this[fieldName]
        ?: return null

    return value as? T ?: throw ClassCastException(
        "Property '$fieldName' has wrong type. Expected: ${T::class.simpleName}, but was: ${value::class.simpleName}"
    )
}
