plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

val javaVersion = 25

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    //compileOnly(libs.jetbrains.annotations)
    //compileOnly(libs.jspecify)

    implementation("com.github.saltAgain:LifeCommon:v1.0.1")



    implementation("org.jdbi:jdbi3-core:3.45.4")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-core:11.1.0")
    implementation("org.flywaydb:flyway-mysql:11.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.slf4j:slf4j-api:2.0.16")

    runtimeOnly("org.slf4j:slf4j-jdk14:2.0.16")
    runtimeOnly("com.mysql:mysql-connector-j:9.1.0")

    compileOnly(fileTree("libs") {
        include("*.jar")
    })
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    mergeServiceFiles()

    dependencies {
        exclude(dependency("com.hypixel.hytale:Server:.*"))
        exclude(dependency("dev.scaffoldit:.*:.*"))

        exclude(dependency("curse.maven:hyui-.*:.*"))
        exclude(dependency("curse.maven:multiplehud-.*:.*"))
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    withSourcesJar()
}