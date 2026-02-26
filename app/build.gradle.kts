import java.io.File

plugins {
    alias(libs.plugins.android.application)
}

// 安全获取API密钥 - 仅使用环境变量和local.properties，无硬编码回退
fun getApiCredentials(): Pair<String, String> {
    // 首先尝试环境变量（最安全的方式）
    val envId = System.getenv("WEARBUS_API_DEV_ID")
    val envKey = System.getenv("WEARBUS_API_DEV_KEY")
    
    if (!envId.isNullOrEmpty() && !envKey.isNullOrEmpty()) {
        println("✅ 使用环境变量中的API密钥")
        return Pair(envId, envKey)
    }
    
    // 如果环境变量不存在，检查local.properties
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val properties = mutableMapOf<String, String>()
        localPropertiesFile.forEachLine { line ->
            if (line.contains("=") && !line.startsWith("#")) {
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    properties[parts[0].trim()] = parts[1].trim()
                }
            }
        }
        
        val idFromProps = properties["api.dev.id"] ?: ""
        val keyFromProps = properties["api.dev.key"] ?: ""
        
        if (idFromProps.isNotEmpty() && keyFromProps.isNotEmpty()) {
            println("✅ 使用local.properties中的API密钥")
            return Pair(idFromProps, keyFromProps)
        }
    }
    
    // 不再提供硬编码回退，强制用户配置密钥
    throw GradleException("API密钥未配置！\n\n" +
        "🔒 为了安全，本构建系统不再提供硬编码的API密钥。\n\n" +
        "请通过以下方式之一配置API密钥：\n\n" +
        "方式1 - 环境变量（推荐）:\n" +
        "设置环境变量 WEARBUS_API_DEV_ID 和 WEARBUS_API_DEV_KEY\n\n" +
        "Windows PowerShell:\n" +
        "\$env:WEARBUS_API_DEV_ID=\"<你的API_ID>\"\n" +
        "\$env:WEARBUS_API_DEV_KEY=\"<你的API_KEY>\"\n\n" +
        "Linux/Mac:\n" +
        "export WEARBUS_API_DEV_ID=<你的API_ID>\n" +
        "export WEARBUS_API_DEV_KEY=<你的API_KEY>\n\n" +
        "方式2 - local.properties文件:\n" +
        "在项目根目录创建或编辑local.properties文件，添加：\n" +
        "api.dev.id=<你的API_ID>\n" +
        "api.dev.key=<你的API_KEY>\n\n" +
        "💡 安全优势：\n" +
        "- ✅ 密钥不会出现在任何代码文件中\n" +
        "- ✅ 不会被Git跟踪和泄露\n" +
        "- ✅ 不会被反编译获取\n" +
        "- ✅ 支持不同环境使用不同密钥\n" +
        "- ✅ 每个开发者可以有自己的密钥\n\n" +
        "⚠️ 注意：build.gradle.kts中不再包含任何硬编码的API密钥！\n\n" +
        "🔍 获取API密钥：\n" +
        "请联系项目管理员或查看项目文档获取API密钥。\n\n" +
        "📋 当前状态：\n" +
        "- 环境变量: " + (if (System.getenv("WEARBUS_API_DEV_ID") != null) "已配置" else "未配置") + "\n" +
        "- local.properties: " + (if (rootProject.file("local.properties").exists()) "存在" else "不存在") + "\n\n" +
        "📖 详细配置指南：\n" +
        "请参考项目根目录的 API_KEY_SECURITY_GUIDE.md 文件")
}

val (apiDevId, apiDevKey) = getApiCredentials()

android {
    namespace = "com.Sumeru.WearBus"
    compileSdk = 36
    
    // 禁用JDK工具链，使用系统默认Java
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        applicationId = "com.Sumeru.WearBus"
        minSdk = 21
        targetSdk = 36
        versionCode = 10
        versionName = "2.1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 不再生成BuildConfig常量，避免明文密钥泄露
        // 密钥将通过JNI Native代码提供
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    // NDK 配置 - 用于编译 C++ 安全密钥库
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    ndkVersion = "25.2.9519653"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.contentpager)

    // Room（直接使用明确版本，避免版本别名问题）
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("androidx.multidex:multidex:2.0.1")

    // 位置服务：FusedLocationProviderClient / LocationServices
    implementation("com.google.android.gms:play-services-location:21.3.0")
}