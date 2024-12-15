plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")

    // Apply the Kotlin Spring plugin
    alias(libs.plugins.kotlin.spring)
}

dependencies {

    // ZXing for QR Code generation
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")

    // QRCode cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")

    // OpenCSV
    implementation("com.opencsv:opencsv:5.8")

    // Include Spring Boot Starter Web as an implementation dependency
    implementation(libs.spring.boot.starter.web)

    // Add Kotlin test library for unit testing
    testImplementation(libs.kotlin.test)

    // Add Mockito Kotlin library for mocking in tests
    testImplementation(libs.mockito.kotlin)

    // Add JUnit Jupiter library for writing and running tests
    testImplementation(libs.junit.jupiter)

    // Add JUnit Platform Launcher for launching tests
    testRuntimeOnly(libs.junit.platform.launcher)
}
