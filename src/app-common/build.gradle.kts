plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.gaided.game.app.common"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":chess-ui"))
    implementation(project(":data-model"))
    implementation(project(":chess-game"))
    implementation(project(":logger"))
    implementation(libs.coroutines.core)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.lifecycle.viewmodelKtx)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}
