@file:Suppress("UnstableApiUsage")

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.8.10")
    id("quiet-fabric-loom") version "1.4-SNAPSHOT"
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
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:2023.09.03@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"].toString()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"].toString()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"].toString()}")

    // Impactor
    modImplementation("net.impactdev.impactor:common:5.1.1-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:economy:5.1.1-SNAPSHOT")
    modImplementation("net.impactdev.impactor.api:text:5.1.1-SNAPSHOT")

    modImplementation("ca.landonjw.gooeylibs:fabric:3.0.0-1.20.1-SNAPSHOT@jar")
    modImplementation("com.cobblemon:fabric:1.4.1+1.20.1")

    // Placeholders
    modImplementation("eu.pb4:placeholder-api:2.1.2+$minecraftVersion")
    modImplementation("io.github.miniplaceholders:miniplaceholders-api:2.2.2")
    modImplementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.2")

    // Adventure Text!
    modImplementation(include("net.kyori:adventure-platform-fabric:5.9.0")!!)

    // PermissionsAPI
    modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")

    // Database
    implementation(include("org.mongodb:mongodb-driver-sync:4.11.0")!!)
    implementation(include("org.mongodb:mongodb-driver-core:4.11.0")!!)
    implementation(include("org.mongodb:bson:4.11.0")!!)

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
    options.release.set(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask> {
    from("LICENSE") {
        rename { "${it}_${modId}" }
    }
}