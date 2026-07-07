rootProject.name = "LifeHub"

plugins {
    // See documentation on https://scaffoldit.dev
    id("dev.scaffoldit") version "0.2.+"
}

// Would you like to do a split project?
// Create a folder named "common", then configure details with `common { }`

hytale {
    usePatchline("release")
    useVersion("latest")

    repositories {
        // Any external repositories besides: MavenLocal, MavenCentral, HytaleMaven, and CurseMaven
    }

    dependencies {
        implementation("org.jdbi:jdbi3-core:3.45.4")
        implementation("com.zaxxer:HikariCP:6.2.1")
        implementation("org.flywaydb:flyway-core:11.1.0")
        implementation("org.flywaydb:flyway-mysql:11.1.0")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
        implementation("org.slf4j:slf4j-api:2.0.16")

        runtimeOnly("com.mysql:mysql-connector-j:9.1.0")

        // Any external dependency you also want to include
        implementation("org.mariadb.jdbc:mariadb-java-client:3.5.3")
        implementation("com.zaxxer:HikariCP:6.3.0")
    }

    manifest {
        Group = "Saltt"
        Name = "LifeHub"
        Main = "dev.saltt.lifehub.Main"
    }
}