# Android application vndk use core libraries
-keep class android.app.** { *; }
-keep class android.content.** { *; }
-keep class android.view.** { *; }

# Hilt
-keep class com.google.dagger.hilt.android.internal.managers.** { *; }
-keep class com.google.dagger.hilt.android.internal.view.** { *; }
-keep class com.google.dagger.hilt.android.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# Keep all classes in the application package
-keep class com.fracorbas.motivationapp.** { *; }

# Keep R classes
-keep class **.R$* { *; }

# Keep all Activities, Services and BroadcastReceivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Remove unused code
-dontwarn **
-ignorewarnings
