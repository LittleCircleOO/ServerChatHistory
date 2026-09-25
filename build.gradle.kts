plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

val modVersion = property("mod.version") as String
val modId = property("mod.id") as String
val loaderVersion = Regex("\\d+\\.\\d+").find(property("deps.fabric_loader") as String)!!.value
val mcCompat = sc.properties["mod.mc_compat"] as String

tasks {
    processResources {
        val props = buildMap {
            put("version", "$modVersion+${sc.current.version}")
            put("minecraft", mcCompat)
            put("loader", loaderVersion)
        }
        inputs.properties(props)

        filesMatching("fabric.mod.json") {
            expand(props)
        }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("java", mixinJava)
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
        }
    }

    withType<Jar> {
        from(rootProject.file("LICENSE")) {
            rename { "${it}-chathistory" }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modVersion"))
    }
}
