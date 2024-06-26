import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.jetbrainsCompose)
//    alias(libs.plugins.compose.compiler)
}

//compose.desktop {
//    application {
//        mainClass = "ApplicationKt"
//    }
//}

kotlin {
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        this.mainRun {
            this.mainClass = "ApplicationKt"
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
//            implementation(compose.runtime)
        }

        desktopMain.dependencies {
//            implementation(compose.desktop.currentOs)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.coroutines.swing)
            implementation(project(":game"))
        }
    }
}
