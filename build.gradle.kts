plugins {
    idea
    java
    id("gg.essential.loom") version "1.13.41"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.github.tahmid_23.examplemod"
version = "1.0-SNAPSHOT"

val mcVersion: String by project
val modid: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    runs {
        getByName("client") {
            // If you don't want mixins, remove these lines
            property("mixin.debug", "true")
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            if (org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        // If you don't want mixins, remove this line
        mixinConfig("mixins.$modid.json")
        accessTransformer(file("src/main/resources/${modid}_at.cfg"))
    }
    // If you don't want mixins, remove these lines
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.mcp)
    forge(libs.forge)

    // If you don't want mixins, remove these lines
    shadowImpl(libs.mixin) {
        isTransitive = false
    }
    annotationProcessor(variantOf(libs.mixin.processor) { classifier("processor") })

    // If you don't want to log in with your real minecraft account, remove this line
    runtimeOnly(libs.devauth)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"

        // If you don't want mixins, remove these lines
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
        this["FMLAT"] = "${modid}_at.cfg"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("${modid}_at.cfg", "META-INF/${modid}_at.cfg")
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.get().forEach {
            println("Copying jars into mod: ${it.files}")
        }
    }

    // If you want to include other dependencies and shadow them, you can relocate them in here
    fun relocate(name: String) = relocate(name, "$group.deps.$name")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("")
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

tasks.assemble {
    dependsOn(tasks.remapJar)
}
