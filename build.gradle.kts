plugins {
	id("java-library")
	id("net.labymod.gradle")
	id("net.labymod.gradle.addon")
	id("fabric-loom") version "1.6-SNAPSHOT"


}
version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

dependencies {
	minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
	mappings("net.fabricmc:yarn:${project.extra["yarn_mappings"]}:v2")
	modImplementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")
}

labyMod {
	defaultPackageName = "de.alive.preiscxn"
	addonInfo {
		namespace = "preiscxn"
		displayName = "PreisCxn"
		author = "TeddyBear_2004"
		description = "A simple addon for LabyMod that shows the price items on cytooxien.de"
		minecraftVersion = ">1.20.0"
		version = System.getenv().getOrDefault("VERSION", "0.0.1")
	}

	minecraft {
		registerVersions(
			"1.20.4",
			"1.20.5",
			"1.20.6",
		) { version, provider ->
			configureRun(provider, version)
		}

		subprojects.forEach {
			if (it.name != "game-runner") {
				filter(it.name)
			}
		}
	}

	addonDev {
		productionRelease()
	}
}

fun configureRun(provider: net.labymod.gradle.core.minecraft.provider.VersionProvider, gameVersion: String) {
	provider.runConfiguration {
		mainClass = "net.minecraft.launchwrapper.Launch"
		jvmArgs("-Dnet.labymod.running-version=${gameVersion}")
		jvmArgs("-Dmixin.debug=true")
		jvmArgs("-Dnet.labymod.debugging.all=true")
		jvmArgs("-Dmixin.env.disableRefMap=true")

		args("--tweakClass", "net.labymod.core.loader.vanilla.launchwrapper.LabyModLaunchWrapperTweaker")
		args("--labymod-dev-environment", "true")
		args("--addon-dev-environment", "true")
	}

	provider.javaVersion = JavaVersion.VERSION_21

	provider.mixin {
		minVersion = "0.8.2"
	}
}

subprojects {
	plugins.apply("java-library")
	plugins.apply("net.labymod.gradle")
	plugins.apply("net.labymod.gradle.addon")
	plugins.apply("fabric-loom")


	dependencies {
		minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
		mappings("net.fabricmc:yarn:${project.extra["yarn_mappings"]}:v2")
		modImplementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")

	}
	repositories {
		maven("https://libraries.minecraft.net/")
		maven("https://repo.spongepowered.org/repository/maven-public/")
	}
}