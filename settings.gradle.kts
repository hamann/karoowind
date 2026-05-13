import java.util.Properties

val localProperties = Properties().apply {
    val f = file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

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
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = localProperties.getProperty("gpr.user")
                    ?: providers.environmentVariable("GITHUB_ACTOR").getOrElse("")
                password = localProperties.getProperty("gpr.key")
                    ?: providers.environmentVariable("GITHUB_TOKEN").getOrElse("")
            }
        }
    }
}

rootProject.name = "karoowind"
include(":app")
