import java.nio.charset.Charset

plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.8.9"
    id("babric-loom-extension") version "1.8.9"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
}


fun getPropertyOrEnvVar(property: String, envVar: String): String =
    getPropertyOrEnvVar(property, envVar) { it ?: "" }

fun getToggle(property: String, envVar: String): Boolean = getPropertyOrEnvVar(property, envVar) { it.toBoolean()}

fun <T> getPropertyOrEnvVar(property: String, envVar: String, op: (String?) -> T): T =
    op(project.findProperty(property) as String? ?: System.getenv(envVar))

val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val stapi_version: String by project
val ami_version: String by project
val bhcreative_version: String by project
val modmenu_version: String by project
val archives_base_name: String by project
val next_version: String by project
val artifact_id: String by project
val api_version: String by project

val publish_to_github_packages = getToggle("gpr.publish", "GPR_PUBLISH")
val gh_username = getPropertyOrEnvVar("gpr.username", "GITHUB_ACTOR")
val gh_token = getPropertyOrEnvVar("gpr.key", "GITHUB_TOKEN")
val gh_repo = project.findProperty("gpr.repo") as String

val publish_to_modrinth  = getToggle("modrinth.publish", "MODRINTH_PUBLISH")
val modrinth_id = project.findProperty("modrinth.id") as String
val modrinth_token = getPropertyOrEnvVar("modrinth.token", "MODRINTH_TOKEN")

val publish_to_glass_maven  = getToggle("glass.publish", "GLASS_MAVEN_PUBLISH")
val glass_username = getPropertyOrEnvVar("glass.username", "GLASS_MAVEN_USERNAME")
val glass_password = getPropertyOrEnvVar("glass.password", "GLASS_MAVEN_PASSWORD")

val releasing = project.hasProperty("releasing")


java {
    withSourcesJar()
}

group = maven_group
version = next_version

if (!releasing) {
    version = "${version}-SNAPSHOT"
}

tasks.jar {
    archiveBaseName.value(archives_base_name)
}


repositories {
    maven {
        name = "Babric"
        url = uri("https://maven.glass-launcher.net/babric")
    }
    // Used for mappings.
    maven {
        name = "Glass Releases"
        url = uri("https://maven.glass-launcher.net/releases")
    }

    maven {
        name = "Glass Snapshots"
        url = uri("https://maven.glass-launcher.net/snapshots")
    }

    maven(uri("https://jitpack.io"))

    maven {
        name = "Froge"
        url = uri("https://maven.minecraftforge.net/")
    }

    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }

    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.glasslauncher:biny:${yarn_mappings}:v2")
    modImplementation("babric:fabric-loader:${loader_version}")

    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.2")
    implementation("blue.endless:jankson:1.2.1")

    modImplementation("net.modificationstation:StationAPI:${stapi_version}")

    // Optional, but convenient mods for mod creators and users alike.
    modRuntimeOnly("net.glasslauncher.mods:ModMenu:${modmenu_version}") {
        isTransitive = false
    }

    modRuntimeOnly("net.glasslauncher.mods:glass-networking:1.0.2") {
        isTransitive = false
    }

    modRuntimeOnly("me.carleslc:Simple-Yaml:1.8.4")
    modRuntimeOnly("net.glasslauncher.mods:GlassConfigAPI:3.0.0") {
        isTransitive = false
    }

    modRuntimeOnly("net.glasslauncher.mods:AlwaysMoreItems:${ami_version}") {
        isTransitive = false
    }

    modRuntimeOnly("com.github.matthewperiut:retrocommands:0.5.2") {
        isTransitive = false
    }

    modRuntimeOnly ("maven.modrinth:bh-creative:${bhcreative_version}") {
        isTransitive = false
    }

    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0") {
        exclude("net.fabricmc")
    }

    modImplementation("net.zekromaster.minecraft:terminal:0.2.0-SNAPSHOT")
}

tasks {
    withType<ProcessResources> {
        inputs.property("version", project.version)

        filesMatching("**/fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { options.release = 17 }

tasks.jar {
    from("LICENSE") {
        rename { "LICENSE_${archives_base_name}"}
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = artifact_id
            from(components["java"])
        }
    }
    repositories {
        if (publish_to_github_packages) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${gh_repo}") // Github Package
                credentials {
                    username = gh_username
                    password = gh_token
                }
            }
        }
        if (publish_to_glass_maven) {
            maven {
                name = "GlassMaven"
                url = uri("https://maven.glass-launcher.net/${if (releasing) "releases" else "snapshots"}")
                credentials {
                    username = glass_username
                    password = glass_password
                }
            }
        }
    }
}

if (publish_to_modrinth) {
    modrinth {
        token.set(modrinth_token)
        projectId.set(modrinth_id)
        versionNumber.set(project.version.toString())
        versionType.set("release")
        uploadFile.set(tasks.remapJar)
        gameVersions.addAll("b1.7.3")
        loaders.add("fabric")
        dependencies {
            required.project("stationapi")
            required.project("fabric-language-kotlin")
            required.project("terminal-stapi")
        }
        syncBodyFrom = project.file("README.md").readText(Charset.forName("UTF-8"))
    }

    tasks.modrinth {
        dependsOn(tasks.modrinthSyncBody)
    }
}

task("upload") {
    dependsOn(tasks.publish)
    if (publish_to_modrinth && releasing) {
        dependsOn(tasks.modrinth)
    }
}

kotlin {
    jvmToolchain(17)
}