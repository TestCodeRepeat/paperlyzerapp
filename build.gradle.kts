val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project
val kermit_version: String by project
val kotest_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.21"
    id("io.kotest") version "0.3.8"
}

group = "com.flyingobjex"
version = "0.0.1"

application {
    mainClass.set("com.flyingobjex.ApplicationKt")
}


repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

tasks.register("stage") {
    dependsOn(":installDist")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.129-kotlin-1.4.20")
    implementation("io.ktor:ktor-freemarker:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest_version")
    testImplementation("io.kotest:kotest-framework-engine-jvm:$kotest_version")
    testImplementation ("io.kotest:kotest-runner-junit5:$kotest_version")

    implementation("co.touchlab:kermit:$kermit_version")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.11.2")

    implementation("org.litote.kmongo:kmongo:4.2.7")

}
