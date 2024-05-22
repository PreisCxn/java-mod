plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "${project(":").property("mod_version")}"
group = "${project.property("maven_group")}"

dependencies {
    labyApi("core")
    api(project(":api"))
    api(project(":impl"))

    maven(mavenCentral(),"javax.json:javax.json-api:1.1.4")
    maven(mavenCentral(),"org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
    maven(mavenCentral(),"org.java-websocket:Java-WebSocket:1.5.6")
    maven(mavenCentral(),"io.projectreactor:reactor-core:3.6.5")
    maven(mavenCentral(),"com.google.guava:guava:33.2.0-jre")
    maven(mavenCentral(),"org.jetbrains:annotations:24.1.0")
    maven(mavenCentral(),"com.google.code.gson:gson:2.8.9")
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
        include(project(":api"))
        include(project(":impl"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}