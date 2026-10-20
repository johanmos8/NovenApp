# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- NovenApp (P3-1 in specs/code-audit-v3) ---

# Keep line numbers so Play Console crash reports stay readable; the mapping file ships
# inside the bundle, so Play de-obfuscates them automatically.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson builds these models from assets/*.json by reflection, matching JSON keys to
# field names. If R8 renamed or removed the fields, every devotional and carol would
# load empty or fail to parse. Keep the classes and their fields as declared.
-keep class com.mirkwood.novenapp.presentation.model.DevotionalContent { *; }
-keep class com.mirkwood.novenapp.presentation.model.DevotionalDayContent { *; }
-keep class com.mirkwood.novenapp.presentation.model.ContentBlockData { *; }
-keep class com.mirkwood.novenapp.presentation.model.Song { *; }

# Gson 2.10.x ships no consumer rules of its own. TypeToken<List<Song>> needs the generic
# signature of its anonymous subclass kept to know the element type.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn sun.misc.**
