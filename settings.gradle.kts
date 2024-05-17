pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Gaided"
include(":engine", ":game", ":androidApp")

// All projects are in src folder.
rootProject.children.forEach {
    it.projectDir = File("src/${it.name}")
}
