plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "es.dam.psp.ut2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// Para lanzar los verificadores desde la terminal (en IntelliJ basta con el ▶ de cada main):
//   ./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorKt
//   ./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorEsperaKt
application {
    mainClass.set(
        providers.gradleProperty("main")
            .orElse("es.dam.psp.ut2.guiada.VerificadorKt")
    )
}
