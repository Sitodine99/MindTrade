
# MANTENER CLASES DEL MODELO (Strategy, Rentabilidad, etc.)
-keep class com.example.mindtrade.model.** { *; }

# FIREBASE (Firestore, Auth, Realtime Database)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# GSON (Evita problemas con JSON en modelos)
-keep class com.google.gson.** { *; }

# RETROFIT Y OKHTTP
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.moshi.** { *; }

# KOTLIN REFLECTION (Evita fallos en datos dinámicos)
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.** { *; }

#EVITA QUE PROGUARD ELIMINE CLASES CRÍTICAS
-dontwarn com.google.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn androidx.**
-dontwarn kotlin.**

#MANTENER LOS NOMBRES ORIGINALES (DEBUGGING)
-keepattributes SourceFile,LineNumberTable
