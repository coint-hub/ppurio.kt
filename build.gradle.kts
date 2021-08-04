plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
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