plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.zhuomo.glasspomodoro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zhuomo.glasspomodoro"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.2"
    }

    signingConfigs {
        val envKeystorePath = System.getenv("KEYSTORE_PATH")
        val envStorePass = System.getenv("KEYSTORE_PASSWORD")
        val envKeyAlias = System.getenv("KEY_ALIAS")
        val envKeyPass = System.getenv("KEY_PASSWORD")

        // 仅在所有签名环境变量都存在且非空时创建 release 签名配置
        // 这些变量由 GitHub Secrets 通过 Actions 工作流注入
        // 注意：System.getenv() 对空环境变量返回空字符串 "" 而非 null
        if (!envKeystorePath.isNullOrBlank() && !envStorePass.isNullOrBlank()
            && !envKeyAlias.isNullOrBlank() && !envKeyPass.isNullOrBlank()) {
            create("release") {
                storeFile = file(envKeystorePath)
                storePassword = envStorePass
                keyAlias = envKeyAlias
                keyPassword = envKeyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            // Debug 使用 Android 默认 debug 签名，无需任何密码
        }
    }

    lint {
        // 禁用与 Kotlin 2.0 不兼容的 Lint 检查（已知 AGP/Lint bug）
        disable += setOf("NullSafeMutableLiveData", "ObsoleteSdkInt")
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room (focus statistics)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore (settings)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil (image loading for wallpapers)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Network (Bing wallpaper API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
}
