import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.tomwmth"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://lib.alpn.cloud/alpine-public")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.18") {
        exclude("org.slf4j")
    }
    implementation("dev.tomwmth:viego:1.0.2") {
        exclude("org.slf4j")
    }
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("it.unimi.dsi:fastutil:8.5.12")

    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")

    val lombok = "org.projectlombok:lombok:1.18.30"
    compileOnly(lombok)
    annotationProcessor(lombok)
}

configurations {
    configureEach {
        exclude("club.minnced")
    }
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
    mergeServiceFiles()

    exclude("META-INF/maven/")
    exclude("META-INF/proguard/")
    exclude("**/**.kotlin_module")
    exclude("**/package-info.class")
    exclude("**/module-info.class")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
