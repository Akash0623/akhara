# ===============================================
# Akhara ProGuard / R8 Rules — Production Hardened
# ===============================================

# ---- General ----
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontusemixedcaseclassnames

# ---- Room (SQLite) ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-dontwarn androidx.room.paging.**

# ---- Room entity classes (explicit) ----
-keep class com.akhara.data.db.entity.** { *; }
-keep class com.akhara.data.db.dao.** { *; }
-keep class com.akhara.data.db.AkharaDatabase { *; }
-keep class com.akhara.data.db.AkharaDatabase$* { *; }

# ---- SQLCipher ----
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# ---- Jetpack Compose ----
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ---- Navigation Compose ----
-keep class androidx.navigation.** { *; }

# ---- Lifecycle & ViewModel ----
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ---- WorkManager ----
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ---- Biometric ----
-keep class androidx.biometric.** { *; }

# ---- Security Crypto ----
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# ---- Kotlin coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ---- Kotlin serialization ----
-keepattributes RuntimeVisibleAnnotations

# ---- App security classes ----
-keep class com.akhara.security.** { *; }

# ---- Notifications ----
-keep class com.akhara.notifications.** { *; }

# ---- Prevent stripping of Application class ----
-keep class com.akhara.AkharaApp { *; }
-keep class com.akhara.MainActivity { *; }

# ---- Optimization passes ----
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'a'
