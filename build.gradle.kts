@file:Suppress("UnstableApiUsage")

plugins {
    id("org.jetbrains.kotlin.jvm").version("2.0.0")
    id("quiet-fabric-loom") version "1.7-SNAPSHOT"
}

val modId = project.properties["mod_id"].toString()
version = project.properties["version"].toString()
group = project.properties["group"].toString()

val minecraftVersion = project.properties["minecraft_version"].toString()

base.archivesBaseName = project.properties["mod_name"].toString()

repositories {
    mavenCentral()
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.parchmentmc.org")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.impactdev.net/repository/development/")
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val modImplementationInclude by configurations.register("modImplementationInclude")

configurations {
    modImplementationInclude
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        // TODO: Fix hardcoded minecraft version once Parchment updates
        parchment("org.parchmentmc.data:parchment-1.21:${project.properties["parchment_version"]}")
    })
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"].toString()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"].toString()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"].toString()}")

    modImplementation("com.cobblemon:fabric:1.6.0+1.21-SNAPSHOT")

    // Adventure Text!
    modImplementation(include("net.kyori:adventure-platform-fabric:5.14.2")!!)

    // PermissionsAPI
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")

    modImplementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("id" to modId, "version" to version)
    }

    filesMatching("**/lang/*.json") {
        expand("id" to modId)
    }
}

tasks.remapJar {
    archiveFileName.set("${project.name}-fabric-$minecraftVersion-${project.version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask> {
    from("LICENSE") {
        rename { "${it}_${modId}" }
    }
}
