plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.igor_shaula"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}