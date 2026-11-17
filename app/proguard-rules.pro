# kotlinx.serialization: conservar serializadores de los modelos
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.cosplayjournal.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.cosplayjournal.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.cosplayjournal.app.**$$serializer { *; }

# Ktor / Supabase
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
