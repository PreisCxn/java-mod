plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
}

group = "de.alive"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    //labyApi("api")

    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
}

tasks.test {
    useJUnitPlatform()
}
