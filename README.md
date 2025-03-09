# DataMap Builder Generator

A Kotlin Symbol Processing (KSP) plugin that generates builder classes for Kotlin data classes. The generated builders allow you to get and put properties in a map-like way and finally build a new instance. Supports both JVM and Android platforms.

## Usage

1. Add the KSP plugin to your project:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

dependencies {
    implementation("com.kkalisz.datamap:runtime:1.0-SNAPSHOT")
    ksp("com.kkalisz.datamap:processor:1.0-SNAPSHOT")
}
```

2. Annotate your data class with `@MapBuilder`:

```kotlin
import com.kkalisz.datamap.MapBuilder

@MapBuilder
data class User(
    val name: String,
    val email: String?
)
```

3. Use the generated builder:

```kotlin
// Create a new instance
val builder = UserBuilder()
builder.put("name", "John")
builder.put("email", "john@example.com")
val user = builder.build()

// Or modify existing instance
val updatedUser = user.buildInstance {
    put("name", "Jane")
}

// Access properties
val name = builder["name"] as String

// Modify existing instance using toMapBuilder
val newUser = user.toMapBuilder().apply {
    put("name", "Jane Doe")
    put("email", null)
}.build()
```

## Features

- Map-like access to properties with type-safe building
- Extension functions for convenient copying
- Null-safety for optional properties
- Reflection support through `BuilderProvider` interface
- Multiplatform support (JVM and Android)
- Type-safe code generation with KSP

## Code Quality

The project uses [detekt](https://detekt.dev/) for static code analysis with custom configuration. To run the analysis:

```bash
./gradlew detekt
```

## Continuous Integration

The project uses GitHub Actions for:
- Automated builds and tests
- Code quality checks
- Automated version management
- Publishing releases

## Building from Source

1. Clone the repository
2. Build the project: `./gradlew build`

The project uses Gradle with Kotlin DSL and version catalog for dependency management. Minimum requirements:
- JDK 17 for building
- Android SDK 21+ for Android artifacts

## License

MIT License
