plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "it.lycoris.lycoscript.idea"
version = "1.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    maven {
        url = uri("https://repo.eclipse.org/content/repositories/lsp4j")
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1.1")
        plugin("com.redhat.devtools.lsp4ij:0.19.3")
    }

    implementation(files("src/main/resources/server/LycoScriptCompiler.jar"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    patchPluginXml {
        sinceBuild.set("261")
        untilBuild.set("261.*")
    }

    runIde {
        doFirst {
            val filteredArgs = jvmArgs.filterNot { arg ->
                arg.contains("coroutines-javaagent")
            }
            jvmArgs = filteredArgs
        }

        jvmArgs("-XX:+IgnoreUnrecognizedVMOptions")
    }

    buildSearchableOptions {
        enabled = false
    }
}

val compilerProjectDir = file("../Compiler")
val compilerJarPath = "$compilerProjectDir/target/LycoScriptCompiler.jar"

tasks.register<Exec>("buildCompiler") {
    group = "lycoscript"
    description = "Compile LycoScriptCompiler using Maven"

    workingDir = compilerProjectDir

    val mvnCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "mvn.cmd" else "mvn"

    commandLine(mvnCommand, "clean", "package")
}

tasks.register<Copy>("copyCompilerJar") {
    group = "lycoscript"
    description = "Copy the Fat JAR of the compiler in the resources of the plugin"

    dependsOn("buildCompiler")

    from(compilerJarPath)
    into("src/main/resources/server")
}

tasks.named("processResources") {
    dependsOn("copyCompilerJar")
}

tasks.named("compileJava") {
    dependsOn("copyCompilerJar")
}