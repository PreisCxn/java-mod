plugins {
    id("maven-publish")
}

version = "${project(":").property("mod_version")}"
group = "${project.property("maven_group")}"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":inventoryscanner"))
    implementation(project(":listener"))
}

tasks.test {
    useJUnitPlatform()
}

base {
    archivesName.set(project.property("archives_base_name") as String)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
    options.release.set(21)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for retrieving dependencies.
    }
}

tasks.register<Copy>("generateVersion") {
    doFirst {
        delete("src/main/java/de/alive/preiscxn/Version.java")
        println("mod_version is: ${project.property("mod_version")}")
    }
    from("src/templates/version_template.java")
    into("src/main/java/de/alive/preiscxn/impl")
    include("version_template.java")
    rename("version_template.java", "Version.java")
    expand("version" to project.property("mod_version"))
    doLast {
        println("generateVersion task has been executed")
    }
    outputs.upToDateWhen { false } // Add this line
}

tasks.named("compileJava") {
    dependsOn("generateVersion")
}

tasks.named("sourcesJar") {
    dependsOn("generateVersion")
}

tasks.named("build") {
    dependsOn("generateVersion")
    //dependsOn("checkstyleMain")
}
