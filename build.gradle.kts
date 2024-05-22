plugins {
	id("java-library")
	id("net.labymod.gradle")
	id("net.labymod.gradle.addon")
}

version = "${project.extra["mod_version"]}"
group = "${project.extra["maven_group"]}"

labyMod {
	defaultPackageName = "${project.property("maven_group")}"
	addonInfo {
		namespace = "preiscxn"
		displayName = "PreisCxn"
		author = "TeddyBear_2004"
		description = "A simple addon for LabyMod that shows the price items on cytooxien.de"
		minecraftVersion = ">1.20.3"
		version = "${project.extra["mod_version"]}"
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
		implementation("javax.json:javax.json-api:1.1.4")
		implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.5")
		implementation("org.java-websocket:Java-WebSocket:1.5.6")
		implementation("io.projectreactor:reactor-core:3.6.5")
		implementation("com.google.guava:guava:33.2.0-jre")
		implementation("org.jetbrains:annotations:24.1.0")
		implementation("com.google.code.gson:gson:2.8.9")
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