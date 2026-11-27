plugins {
    kotlin("jvm") version "2.4.20"
    id("scala")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":care-planner"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

scala {
    scalaVersion = "2.13.18"
}

tasks.test {
    useJUnitPlatform()
}
