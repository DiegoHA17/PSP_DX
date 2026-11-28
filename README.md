# Programación de Servicios y Procesos

Módulo 0490 · 2.º DAM · Curso 2026-27

Repositorio de las prácticas del módulo, organizado por unidades didácticas. Cada
unidad contiene sus proyectos (Gradle de consola y, cuando corresponde, Android
Studio) y su documentación. El trabajo de cada práctica se sigue con Issues y se
integra en `main` mediante Pull Requests.

## Unidades

| Unidad | Contenido | Estado | Código y documentación |
|--------|-----------|--------|------------------------|
| UT1 | Procesos y servicios | Evaluada ([problemas abiertos](UT1/documentacion/REVISION.md)) | [`UT1/`](UT1/README.md) |
| UT2 | Programación multihilo | En curso | [`UT2/`](UT2/README.md) |

Las siguientes unidades se añadirán como `UT3/`, `UT4/`… a medida que se
publiquen sus prácticas.

## Ejecución

Requisitos: JDK 21, IntelliJ IDEA (proyectos de consola) y Android Studio con el
SDK correspondiente (proyectos Android). Todos los proyectos incluyen el wrapper
de Gradle, así que no hace falta instalarlo.

```bash
cd UT1/procesos
./gradlew build                                              # en Windows: .\gradlew build
./gradlew run -Pmain=es.dam.psp.ut1.practicas.LanzadorKt
```

Las instrucciones completas de cada práctica están en el README de su unidad.

## Forma de trabajo

1. Cada práctica tiene un **Issue** (plantilla «Nueva práctica») con requisitos,
   tareas y criterios de aceptación, la etiqueta de su unidad y el milestone de
   la unidad.
2. Se desarrolla en una **rama** desde `main`: `feat/ut2-practica1`,
   `fix/ut2-practica1-error`, `docs/ut2-documentacion`.
3. Se integra con un **Pull Request** que enlaza el Issue (`Closes #N`) y pasa
   la compilación automática de GitHub Actions.

Detalles, convención de commits y etiquetas: [`CONTRIBUTING.md`](CONTRIBUTING.md).
Historia de la UT1 (anterior a este flujo): [`UT1/documentacion/REVISION.md`](UT1/documentacion/REVISION.md).

## Autor

Diego Herrera Ayuso
