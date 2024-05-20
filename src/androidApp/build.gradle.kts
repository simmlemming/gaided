plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
//    alias(libs.plugins.composeMultiplatform)
}

android {
    namespace = "com.gaided"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "com.gaided"
        minSdk = libs.versions.android.min.sdk.get().toInt()
        targetSdk = libs.versions.android.target.sdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":game"))
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
//    implementation(compose.runtime)
//    implementation(compose.foundation)
//    implementation(compose.material3)
//    implementation(compose.preview)
//    implementation(compose.uiTooling)
//    implementation(libs.androidxCore.ktx)
//    implementation(libs.constraintlayout)
//    implementation(libs.coroutines.core)
//    implementation(libs.lifecycle.runtimeKtx)
//    implementation(libs.lifecycle.viewmodelKtx)
}