import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.TimeZone
import java.time.LocalDate
import java.time.format.DateTimeFormatter

tasks {
    withType<JavaCompile> {
        options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
    }
}

plugins {
    kotlin("jvm") version "1.5.31"
    id("application")
    id("org.ajoberstar.grgit") version "4.1.1"
}

group = "net.snipsniper"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.1stleg:jnativehook:2.1.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.json:json:20211205")
    implementation("com.formdev:flatlaf:1.6")
    implementation("com.erigir:mslinks:0.0.2+5")

    implementation("org.capturecoop:CCUtils:1.9.4")
    implementation("org.capturecoop:CCLogger:1.6.2")
    implementation("org.capturecoop:CCColorUtils:1.0.3")
}

fun refreshWiki() {
    val workingDir = "src//main//resources//net//snipsniper//resources//wiki//"
    exec {
        workingDir(workingDir)
        commandLine( "git", "checkout", "master")
    }
    exec {
        workingDir(workingDir)
        commandLine("git", "submodule", "update")
    }
    exec {
        workingDir(workingDir)
        commandLine("git", "pull")
    }
}

tasks.register("precum") {
    refreshWiki()

    var type = System.getProperty("type")
    if(type == null) type = "dev"

    if(!grgit.status().isClean && System.getenv("GITHUB_RUN_NUMBER") == null)
        type = "dirty"

    var projectVersion = ""
    File("version.txt").forEachLine { projectVersion += it }

    val f = File(rootProject.projectDir.absolutePath + "//src//main//resources//net//snipsniper//resources//cfg//buildinfo.cfg")
    f.createNewFile()

    f.writeText("# This file is generated by gradle upon build")
    f.appendText("\ntype=$type")
    f.appendText("\nversion=$projectVersion")
    f.appendText("\nbuilddate=" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + " (" + TimeZone.getDefault().id + ")")
    f.appendText("\ngithash=" + grgit.head().abbreviatedId)
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "net.snipsniper.Main"
    }

    dependsOn(configurations.runtimeClasspath)
    from({
        sourceSets.main.get().output
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

application {
    mainClass.set("net.snipsniper.Main")
}