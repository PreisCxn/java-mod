plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

dependencies {
    labyApi("core")

    api(project(":api"))
    api(project(":impl"))
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