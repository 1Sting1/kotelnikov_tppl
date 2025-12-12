plugins {
    kotlin("jvm") version "2.1.0"
    jacoco
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    doLast {
        println("Detailed coverage report: file://${layout.buildDirectory.get()}/reports/jacoco/test/html/index.html")
    }
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.example.pascal.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    workingDir = rootProject.projectDir
}