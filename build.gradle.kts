plugins {
	id("java-library")
	id("net.labymod.gradle")
	id("net.labymod.gradle.addon")
}

version = "${project.extra["mod_version"]}-${project.extra["minecraft_version"]}"
group = "${project.extra["maven_group"]}"

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

subprojects {
	plugins.apply("java-library")
	plugins.apply("net.labymod.gradle")
	plugins.apply("net.labymod.gradle.addon")

	repositories {
		maven("https://libraries.minecraft.net/")
		maven("https://repo.spongepowered.org/repository/maven-public/")
	}

	dependencies{
		maven(mavenCentral(),"javax.json:javax.json-api:1.1.4")
		maven(mavenCentral(),"org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
		maven(mavenCentral(),"org.java-websocket:Java-WebSocket:1.5.6")
		maven(mavenCentral(),"io.projectreactor:reactor-core:3.6.5")
		maven(mavenCentral(),"com.google.guava:guava:33.2.0-jre")
		maven(mavenCentral(),"org.jetbrains:annotations:24.1.0")
		maven(mavenCentral(),"com.google.code.gson:gson:2.8.9")
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