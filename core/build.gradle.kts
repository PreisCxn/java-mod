plugins {
    id("java-library")
}

version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

dependencies {
    labyApi("core")
    implementation(project(":impl"))
    implementation(project(":api"))

    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("io.projectreactor:reactor-core:3.6.5")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}