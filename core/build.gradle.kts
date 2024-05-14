plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
    id("checkstyle")
}

version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

repositories {
    mavenCentral()
}

dependencies {
    labyApi("core")

    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.extra["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabric_version"]}")

    // Uncomment the following line to enable the deprecated Fabric API modules.
    // These are included in the Fabric API production distribution and allow you to update your mod to the latest modules at a later more convenient time.
    // modImplementation "net.fabricmc.fabric-api:fabric-api-deprecated:${project.fabric_version}"

    // Fügen Sie die Abhängigkeit für javax.websocket-client hinzu
    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation(project(path = ":api", configuration = "namedElements"))
    implementation(project(":inventoryscanner"))
    implementation(project(":listener"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("io.projectreactor:reactor-test:3.6.5")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.register<Copy>("generateVersion") {
    doFirst {
        delete("src/main/java/de/alive/preiscxn/Version.java")
        println("mod_version is: ${project.extra["mod_version"]}")
    }
    from("src/templates/version_template.java")
    into("src/main/java/de/alive/preiscxn/")
    include("version_template.java")
    rename("version_template.java", "Version.java")
    expand(mapOf("version" to "${project.extra["mod_version"]}"))
    doLast {
        println("generateVersion task has been executed")
    }
    outputs.upToDateWhen { false }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn("generateVersion")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("generateVersion")
}

tasks.named("build") {
    dependsOn("generateVersion")
    // dependsOn("checkstyleMain")
}
