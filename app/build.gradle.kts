plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.Sumeru.WearBus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.Sumeru.WearBus"
        minSdk = 16
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.contentpager)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("androidx.multidex:multidex:2.0.1")
}