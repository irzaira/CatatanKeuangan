plugins {
    id("com.android.application")
}

android {
    namespace = "com.irfandev.catatankeuangan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.irfandev.catatankeuangan"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Keystore tetap (dikomit di repo) supaya setiap build Actions bertanda tangan sama.
    // Dengan begitu APK baru bisa dipasang menimpa yang lama TANPA menghapus data.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
