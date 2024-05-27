plugins {
    id("maven-publish")
}

repositories {
    mavenCentral()
}

version = "${project(":").property("mod_version")}"
group = "${project.property("maven_group")}"

tasks.test {
    useJUnitPlatform()
}
