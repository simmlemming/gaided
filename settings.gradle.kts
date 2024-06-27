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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "Gaided"

// Include all directories in src.
File("src")
    .listFiles { file -> file.isDirectory }
    .orEmpty()
    .forEach { include(it.name) }

// Change root project dir for all children.
rootProject.children.forEach {
    it.projectDir = File("src/${it.name}")
}
