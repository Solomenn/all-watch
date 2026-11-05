plugins {
    id("com.android.application")
}

android {
    namespace = "com.solomenn.allwatch.relayprobe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.solomenn.allwatch.relayprobe"
        minSdk = 26
        targetSdk = 30
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
