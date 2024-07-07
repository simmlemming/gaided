plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            api(project(":data-model"))
            api(project(":engine"))
            api(project(":logger"))
            api(project(":board-stockfish"))
            api(project(":engine-stockfish"))
            implementation(libs.coroutines.core)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.compose.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.junit)
            implementation(libs.coroutines.test)
            implementation(libs.mockk)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.gaided.engine"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
