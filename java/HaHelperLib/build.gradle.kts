plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    // Hoplite to load configs
    implementation("com.sksamuel.hoplite:hoplite-core:2.6.3")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.6.3")

    // GRPC libs
    implementation(project(":HaHelperProto"))
    implementation("io.grpc:grpc-netty:1.33.1")

    // Logger facade
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
