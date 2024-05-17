plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
}

compose.desktop {
    application {
        mainClass = "ApplicationKt"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
//    implementation(libs.lifecycle.viewmodelKtx)
    implementation(project(":game"))
}
