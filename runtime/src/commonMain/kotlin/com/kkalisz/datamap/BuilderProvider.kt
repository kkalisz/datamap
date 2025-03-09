package com.kkalisz.datamap

/**
 * Interface implemented by classes annotated with [MapBuilder] to provide
 * a way to create a builder from an existing instance.
 *
 * @param T The type of the class implementing this interface
 */
public interface BuilderProvider<T> {
    /**
     * Creates a new builder initialized with the current instance's values.
     * This method is implemented by the MapBuilder Plugin.
     *
     * @return A builder initialized with the current instance's values
     */
    public fun mapBuilder(): MapDataBuilder<T> = TODO("Implemented in MapBuilder Plugin")

    /**
     * Creates a copy of the current instance with modifications specified in the initialize block.
     *
     * Example usage:
     * ```kotlin
     * val modifiedUser = user.copy { builder ->
     *     builder.put("name", "Jane")
     * }
     * ```
     *
     * @param initialize A block that configures the builder
     * @return A new instance with the specified modifications
     */
    public fun copy(initialize: MapDataBuilder<T>.() -> Unit): T {
        val builder = mapBuilder()
        builder.initialize()
        return builder.build()
    }
}
