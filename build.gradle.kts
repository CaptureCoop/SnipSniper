import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.TimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    kotlin("jvm") version "1.9.22"
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
val fullVersion = "$version-$type rev-${grgit.head().abbreviatedId}"
val artifactName = "${project.name}.jar"

val groupMain = "SnipSniper"
val groupRun = "SnipSniper run"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

fun DependencyHandlerScope.implementation(group: String, name: String) {
    implementation(group = group, name = name, version = property("version.$name") as String)
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "")
    implementation(group = "com.1stleg", name = "jnativehook") //Used for global keyboard and mouse events
    implementation(group = "org.apache.commons", name = "commons-lang3")
    implementation(group = "org.apache.commons", name = "commons-text")
    implementation(group = "org.json", name = "json")
    implementation(group = "com.formdev", name = "flatlaf") //Swing Theme
    implementation(group = "com.erigir", name = "mslinks") //Utility for windows shortcuts

    implementation(group = "com.github.CaptureCoop", name = "defaultdepot")
    implementation(group = "com.github.CaptureCoop", name = "legiblelogger")
    implementation(group = "com.github.CaptureCoop", name = "colorcomposer")
}

//SnipSniper includes another repository where we store json files with information about SnipSniper
//The idea is that we have one common place where we explain SnipSniper features which we can then bind into the executable & a website
//This function itself refreshes the sub-repository
val taskRefreshWiki = tasks.create("refreshWiki") {
    group = groupMain
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

tasks.build { group = groupMain }

tasks.clean { group = groupMain }

tasks.create("getVersion") {
    group = groupMain
    doLast { println(fullVersion) }
}

tasks.withType<JavaExec> {
    group = groupRun
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
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}
