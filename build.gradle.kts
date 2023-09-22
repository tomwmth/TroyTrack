import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.tomwmth"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    shade(this, "net.dv8tion:JDA:5.0.0-beta.13")
    shade(this, "com.github.tomwmth:Javan:1f7e406")
    shade(this, "com.google.code.gson:gson:2.10.1")
    shade(this, "com.google.guava:guava:32.1.2-jre")
    shade(this, "it.unimi.dsi:fastutil:8.5.12")
    shade(this, "org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    val lombok = "org.projectlombok:lombok:1.18.28"
    compileOnly(lombok)
    annotationProcessor(lombok)
}

configurations {
    configureEach {
        exclude("club.minnced")
    }
}

tasks.withType<JavaExec> {
    mainClass = "dev.tomwmth.troytrack.Runner"

    val run = File("run/");
    run.mkdirs();
    workingDir = run
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Created-By" to "Gradle",
            "Built-JDK" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})",
            "Built-By" to System.getProperty("user.name"),
            "Built-Date" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
            "Main-Class" to "dev.tomwmth.troytrack.Runner"
        )
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    outputs.upToDateWhen { false }

    minimize {
        exclude("**/module-info.class")
        exclude("**/**.kotlin_module")
        exclude("META-INF/maven/")
        exclude("META-INF/proguard/")
    }

    // Add shaded dependencies
    configurations.clear()
    configurations.add(project.configurations.getByName("shadow"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

fun shade(scope: DependencyHandlerScope, dependency: String) {
    scope.implementation(dependency)
    scope.shadow(dependency)
}