plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("jacoco")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }

    val mainSourceSet = sourceSets.getByName("main")
    sourceDirectories.setFrom(files(mainSourceSet.allSource.srcDirs))
    classDirectories.setFrom(files(mainSourceSet.output.classesDirs).asFileTree.matching {
        exclude("**/MainKt.class", "**/MainKt\$*.class")
    })

    executionData.setFrom(fileTree(buildDir).include("jacoco/test.exec"))

    doLast {
        println("file://${reports.html.entryPoint}")
    }
}