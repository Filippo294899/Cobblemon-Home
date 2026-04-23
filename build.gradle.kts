
plugins {
    id("java")
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
    id("architectury-plugin") version "3.4.161"
    kotlin("jvm") version "2.2.20"  // ✅ Updated to match fabric-language-kotlin
}

group = "org.cobblemonhome"
version = "1.7.1Release1.0.0"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven { url = uri("https://maven.nucleoid.xyz") }
    maven { url =  uri("https://jitpack.io") }
}

dependencies { minecraft("net.minecraft:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.104.0+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.104.0+1.21.1"))

    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
    modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")

    //gui
    modImplementation("eu.pb4:sgui:1.6.1+1.21.1")
    include("eu.pb4:sgui:1.6.1+1.21.1")

    modImplementation ("com.github.therealbush:translator:1.1.1")
    //gson
    implementation("com.google.code.gson:gson:2.10.1")

    modImplementation("org.jetbrains.exposed:exposed-core:0.56.0")
    modImplementation("org.jetbrains.exposed:exposed-dao:0.56.0")
    modImplementation("org.jetbrains.exposed:exposed-jdbc:0.56.0")
    modImplementation("org.jetbrains.exposed:exposed-java-time:0.56.0")

    // INCLUDE TUTTE LE DIPENDENZE NEL JAR FINALE
    include("org.postgresql:postgresql:42.7.3")
    include("org.jetbrains.exposed:exposed-core:0.56.0")
    include("org.jetbrains.exposed:exposed-dao:0.56.0")
    include("org.jetbrains.exposed:exposed-jdbc:0.56.0")
    include("org.jetbrains.exposed:exposed-java-time:0.56.0")

    // Per MySQL
    modImplementation("mysql:mysql-connector-java:8.0.33")
    modImplementation("com.mysql:mysql-connector-j:8.0.33")
    include("com.mysql:mysql-connector-j:8.0.33")

    // Per SQLite
    modImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    include("org.xerial:sqlite-jdbc:3.46.0.0")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")



}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}