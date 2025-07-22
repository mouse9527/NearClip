plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.mouse.clipsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mouse.clipsync"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
