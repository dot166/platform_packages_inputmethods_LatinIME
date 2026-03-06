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
        maven { setUrl("../../../prebuilts/fullsdk-darwin/extras/android/m2repository") }
        maven { setUrl("../../../prebuilts/fullsdk-linux/extras/android/m2repository") }
        mavenCentral()
        google()
    }
}
rootProject.name = "LatinIME"
