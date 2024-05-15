plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

dependencies {
    labyApi("core")

    implementation(project(":api"))
    implementation(project(":impl"))

    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("com.google.code.gson:gson:2.8.9")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Configure the Shadow plugin
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    dependencies {
        configurations = listOf(project.configurations.getByName("runtimeClasspath"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}