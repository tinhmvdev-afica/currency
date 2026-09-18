import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.realm)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)

}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) localFile.inputStream().use { load(it) }
}

fun apiKey(name: String): String =
    providers.environmentVariable(name).orNull
        ?: localProperties.getProperty(name)
        ?: error("Missing $name: set an environment variable or add it to local.properties")

fun buildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

//android {
//    namespace = "com.example.currency"
//    compileSdk {
//        version = release(37)
//    }
//
//    defaultConfig {
//        applicationId = "com.example.currency"
//        minSdk = 24
//        targetSdk = 37
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            optimization {
//                enable = false
//            }
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    buildFeatures {
//        viewBinding = true
//    }
//}
android {
    namespace = "com.example.currency"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.currency"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "COINGECKO_API_KEY", buildConfigString(apiKey("COINGECKO_API_KEY")))
        buildConfigField("String", "CURRENCYFREAKS_API_KEY", buildConfigString(apiKey("CURRENCYFREAKS_API_KEY")))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
//    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
//    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("io.coil-kt:coil:2.7.0")

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")


    implementation(libs.realm.base)
    implementation(libs.hilt.android)
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

}
