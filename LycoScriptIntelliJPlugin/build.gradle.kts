plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "it.lycoris.lycoscript.idea"
version = "1.0.2"

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.1.1")
    }

    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("it.lycoris.lycoscript.compiler:LycoScriptCompiler:1.0.0")
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