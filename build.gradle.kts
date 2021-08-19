plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // java 1.6 지원
    val okHttp = "3.12.13"
    implementation("com.squareup.okhttp3:okhttp:$okHttp")
    implementation("com.google.code.gson:gson:2.8.7")

    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttp")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    sourceCompatibility = JavaVersion.VERSION_1_6.toString()
    targetCompatibility = JavaVersion.VERSION_1_6.toString()
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_6.toString()
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}