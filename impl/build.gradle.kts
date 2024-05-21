plugins {
    id("java")
}

version = "${project(":").property("mod_version")}"
group = project.property("maven_group") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":inventoryscanner"))
    implementation(project(":listener"))
    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("com.google.code.gson:gson:2.8.9")

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
        expand("version" to project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

tasks.named("build") {
    dependsOn("generateVersion")
}
