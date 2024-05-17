plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvm()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":engine"))
            implementation(libs.coroutines.core)
            implementation(libs.lifecycle.viewmodelKtx)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
