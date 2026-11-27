plugins {
    scala
    `java-library`
}

repositories {
    mavenCentral()
}

scala {
    scalaVersion = "2.13.18"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}