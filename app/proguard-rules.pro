# R8 configuration for MittOrd.
#
# Room, Hilt/Dagger, OkHttp and Coil ship their own consumer rules, so only the
# parts R8 cannot infer on its own are listed here.

# Keep line numbers in crash reports while hiding the original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Retrofit + Gson
# ---------------------------------------------------------------------------
# Generic signatures are erased by default; Retrofit reads the return type of
# every suspend function and Gson reads Map<String, String> reflectively, so
# both break silently without this.
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# The API declaration is only ever referenced through a dynamic proxy.
-keep,allowobfuscation interface com.reiny.mittord.data.api.TranslateApiService

# Gson instantiates these by reflection and matches fields to @SerializedName,
# so the fields themselves must survive shrinking.
-keep class com.reiny.mittord.data.model.** { *; }

# Retrofit's own reflection points.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
# Entities are constructed by generated code, but the column names have to keep
# matching the schema, so field names must not be renamed.
-keepclassmembers class com.reiny.mittord.database.entity.** { <fields>; }
