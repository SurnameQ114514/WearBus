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
# 保持数据模型类
-keep class com.Sumeru.WearBus.BusLineDetail { *; }
-keep class com.Sumeru.WearBus.Station { *; }

# 保持Parcelable实现
-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# 保持序列化相关类
-keepnames class com.Sumeru.WearBus.BusLineDetail
-keepnames class com.Sumeru.WearBus.Station