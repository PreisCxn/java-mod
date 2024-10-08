pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	val labyGradlePluginVersion = "0.3.48"
	plugins {
		id("net.labymod.gradle") version (labyGradlePluginVersion)
	}

	buildscript {
		repositories {
			maven("https://dist.labymod.net/api/v1/maven/release/")
			maven("https://repo.spongepowered.org/repository/maven-public")
			mavenCentral()
		}

		dependencies {
			classpath("net.labymod.gradle", "addon", labyGradlePluginVersion)
		}
	}

}

plugins.apply("net.labymod.gradle")

include("api")
include("listener")
include("inventoryscanner")
include("core")
include("impl")
include("fabric:fabric_1_20_4")
include("fabric:fabric_1_20_5")
include("fabric:fabric_1_20_6")
include("fabric:fabric_1_21")
include("fabric:fabric_1_21_1")
include("fabric:fabric_core")
