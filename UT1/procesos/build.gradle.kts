plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "es.dam.psp.ut1"
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

// Permite ejecutar cualquier práctica desde la terminal:
//   ./gradlew run -Pmain=es.dam.psp.ut1.practicas.LanzadorKt
// Sin -Pmain se ejecuta el Lanzador.
application {
    mainClass.set(
        providers.gradleProperty("main")
            .orElse("es.dam.psp.ut1.practicas.LanzadorKt")
    )
}

tasks.named<JavaExec>("run") {
    // Directorio de trabajo = raíz del proyecto, igual que en IntelliJ
    // (allí se crean datos/ e historial.txt).
    workingDir = projectDir
    // El Lanzador lee órdenes del teclado.
    standardInput = System.`in`
}
