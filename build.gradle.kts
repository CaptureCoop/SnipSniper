import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.TimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.ajoberstar.grgit") version "4.1.1" //Used to determine the status of the repo
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

group = "net.snipsniper"
version = File("version.txt").readLines()[0]
//The type of release, either stable/release, dev or dirty. Used to determine how to build & passed onto SnipSniper
//Dev = "Clean build", but not stable
//Dirty = Uncommitted changes
val type = System.getProperty("type") ?: if(!grgit.status().isClean && System.getenv("GITHUB_RUN_NUMBER") == null) "dirty" else "dev"
val artifactName = "${project.name} $version-$type rev-${grgit.head().abbreviatedId}.jar"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.1stleg:jnativehook:2.1.0") //Used for global keyboard and mouse events
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.json:json:20220320")
    implementation("com.formdev:flatlaf:1.6") //Swing Theme
    implementation("com.erigir:mslinks:0.0.2+5") //Utility for windows shortcuts

    implementation("org.capturecoop:CCUtils:1.9.6") //CaptureCoop Common Utils
    implementation("org.capturecoop:CCLogger:1.6.6") //CaptureCoop logger
    implementation("org.capturecoop:CCColorUtils:1.0.4") //CaptureCoop Color utils & Color Chooser
}

//SnipSniper includes another repository where we store json files with information about SnipSniper
//The idea is that we have one common place where we explain SnipSniper features which we can then bind into the executable & a website
//This function itself refreshes the sub-repository
val taskRefreshWiki = tasks.create("refreshWiki") {
    group = "SnipSniper"
    fun d(vararg commands: String) {
        exec {
            workingDir("src/main/resources/net/snipsniper/resources/wiki/")
            commandLine(*commands)
        }
    }
    d("git", "checkout", "master")
    d("git", "submodule", "update")
    d("git", "pull")
}

//This returns os.version or the detailed windows build, if on windows
//(Thanks microsoft for returning 10 while linux returns a detailed build nr!)
fun getSystemVersion(): String {
    if(!OperatingSystem.current().isWindows) return System.getProperty("os.version")
    BufferedReader(InputStreamReader(Runtime.getRuntime().exec("cmd.exe /c ver").inputStream)).also { reader ->
        StringBuilder().also { sb ->
            reader.readLines().forEach {l -> if(l.isNotEmpty()) sb.append(l)}
            return sb.toString()
        }
    }
}

tasks.build {
    group = "SnipSniper"
}

tasks.withType<JavaExec> {
    group = "SnipSniper Run"
    dependsOn(tasks.build)
    doFirst {
        val runDir = File(project.buildDir, "run").also { it.mkdirs() }
        workingDir = runDir
        File(project.buildDir, "libs").copyRecursively(runDir, true)
        classpath(File(runDir, artifactName).absolutePath)
        standardInput = System.`in` //This allows input in our IDE
        minHeapSize = properties["snipsniper.run.xms"] as String
        maxHeapSize = properties["snipsniper.run.xmx"] as String
    }
}
tasks.create("run", JavaExec::class) { }
tasks.create("runDebug", JavaExec::class) { args("-debug") }
tasks.create("runEditor", JavaExec::class) { args("-editor") }
tasks.create("runViewer", JavaExec::class) { args("-viewer") }

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from("src/main/resources/") {
        include("net/snipsniper/resources/cfg/buildinfo.cfg")

        val buildDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        expand (
            "buildType" to type,
            "version" to version,
            "buildDate" to "$buildDate (${TimeZone.getDefault().id})",
            "githash" to grgit.head().abbreviatedId,
            "githashFull" to grgit.head().id,
            "branch" to grgit.branch.current().name,
            "osname" to System.getProperty("os.name"),
            "osversion" to getSystemVersion(),
            "osarch" to System.getProperty("os.arch"),
            "javavendor" to System.getProperty("java.vendor"),
            "javaver" to System.getProperty("java.version")
        )
    }
}

tasks.withType<Jar> {
    dependsOn(taskRefreshWiki)
    archiveFileName.set(artifactName)
    if(type != "stable" && type != "release")
        sourceSets.main.get().resources.srcDir("src/main/resources-dev")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest { attributes["Main-Class"] = "net.snipsniper.MainKt" }
    dependsOn(configurations.runtimeClasspath)
    from({
        sourceSets.main.get().output
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}