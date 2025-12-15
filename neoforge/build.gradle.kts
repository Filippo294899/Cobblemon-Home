plugins {
    id("java")
    id("dev.architectury.loom") version("1.11-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    kotlin("jvm") version("2.2.20")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll(listOf(
            "-Xjvm-default=all",
            "-Xno-call-assertions",
            "-Xno-param-assertions",
            "-Xskip-metadata-version-check"
        ))
    }
}


group = "org.cobblehome"
version = "1.7.1NeoForge1.0.0"

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.neoforged.net")
    maven("https://maven.nucleoid.xyz")
    maven("https://jitpack.io")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:21.1.182")

    modImplementation("com.cobblemon:neoforge:1.7.1+1.21.1")
    //Needed for cobblemon
    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0") {
        exclude("net.neoforged.fancymodloader", "loader")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    //gui
    modImplementation("eu.pb4:sgui:1.9.1+1.21.1-neoforge")
    // include("eu.pb4:sgui:1.6.1+1.21.1")

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
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(project.properties)
    }
}