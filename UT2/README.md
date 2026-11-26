# UT2 — Programación multihilo

**Estado:** en curso.

Contenidos: creación y control de hilos (`thread`, `join`, `interrupt`), estados
de un hilo, condiciones de carrera, `synchronized`, atómicas y cerrojos, y
coordinación con `wait()`/`notifyAll()`.

## Prácticas

| Práctica | Código | Documentación |
|----------|--------|---------------|
| Guiada con IA: la taquilla concurrente | [`guiada/`](hilos/src/main/kotlin/es/dam/psp/ut2/guiada) (`TaquillaSegura.kt`, `TaquillaEspera.kt`) | [practica-guiada](documentacion/practica-guiada/README.md) |

## Proyectos

```text
UT2/
├── hilos/           Gradle de consola · Kotlin 2.0 · JVM 21 · Gradle 8.5
│   ├── AGENTS.md    reglas para opencode
│   └── src/main/kotlin/es/dam/psp/ut2/guiada/
└── documentacion/   una carpeta por práctica
```

El enunciado dice que el proyecto no trae `gradlew`; aquí se le ha añadido el
mismo wrapper que a `UT1/procesos` para poder compilarlo en GitHub Actions. En
IntelliJ se abre igual (File > Open > `UT2/hilos`) y se ejecuta con el ▶ de cada
`main`.

## Ejecución

```bash
cd UT2/hilos
./gradlew build
./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorKt          # Retos A y B
./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorEsperaKt    # Reto C
```
