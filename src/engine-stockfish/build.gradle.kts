import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm()
    explicitApi = ExplicitApiMode.Strict

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":engine"))
            implementation(project(":data-model"))
            implementation(project(":logger"))
            implementation(project(":network"))
            implementation(libs.coroutines.core)
            implementation(libs.gson)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.junit)
            implementation(libs.mockk)
        }
    }
}

android {
    namespace = "com.gaided.engine.stockfish"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
