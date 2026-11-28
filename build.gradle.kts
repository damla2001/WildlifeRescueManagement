import org.gradle.api.tasks.scala.ScalaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

//connection between kotlin and scala

//tells Gradle to compile Kotlin first and then make the compiled Kotlin classes accessible to Scala

val compileKotlinTask = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<ScalaCompile>("compileScala") {
    dependsOn(compileKotlinTask)

    classpath += files(
        compileKotlinTask.flatMap { it.destinationDirectory }
    )
}