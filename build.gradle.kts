plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    // Removed the separate compose compiler plugin as it's built-in for this setup or handled via extension version
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
