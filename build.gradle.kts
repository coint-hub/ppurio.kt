plugins {
    kotlin("jvm") version "1.5.21"
    `java-library`
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    val okHttp = "4.9.1"
    implementation("com.squareup.okhttp3:okhttp:$okHttp")
    implementation("com.google.code.gson:gson:2.8.7")

    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttp")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}