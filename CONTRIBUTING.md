# Forma de trabajo

Guía del flujo que sigue este repositorio a partir de la UT2. La UT1 se
desarrolló antes de adoptarlo; su historia está en
[`UT1/documentacion/REVISION.md`](UT1/documentacion/REVISION.md).

## Ciclo de una práctica

1. **Issue.** Crear un Issue con la plantilla *Nueva práctica*. Asignarle la
   etiqueta de la unidad (`UT2`…), el milestone de la unidad y, si se usa, el
   tablero del Project. Si la práctica tiene partes grandes, crear un
   **sub-issue** por parte desde el propio Issue («Create sub-issue»).
2. **Rama** desde `main` actualizado:
   ```bash
   git switch main && git pull
   git switch -c feat/ut2-practica1
   ```
3. **Commits** pequeños y descriptivos (ver convención).
4. **Comprobaciones** locales: `./gradlew build` en el proyecto afectado y la
   ejecución de la práctica.
5. **Pull Request** contra `main` con la plantilla. En la descripción,
   `Closes #N` solo si al fusionar la práctica queda realmente terminada; si no,
   `Refs #N`.
6. **Revisión**: leer el diff completo en GitHub y esperar a que GitHub Actions
   pase. Etiqueta `needs-review` mientras esté pendiente.
7. **Fusión** a `main` con *Create a merge commit* (conserva los commits de la
   rama) y borrado de la rama ya fusionada.
8. **Cierre**: el Issue se cierra al fusionar (`Closes #N`) o a mano cuando se
   cumplan todos sus criterios de aceptación.

## Ramas

`<tipo>/ut<N>-<descripcion-corta>` en minúsculas y con guiones.

| Tipo | Uso | Ejemplo |
|------|-----|---------|
| `feat` | Nueva práctica o funcionalidad | `feat/ut2-practica1` |
| `fix` | Corrección de un error | `fix/ut2-practica1-error` |
| `docs` | Solo documentación | `docs/ut2-documentacion` |
| `refactor` | Reorganización sin cambiar el comportamiento | `refactor/ut2-practica3-limpieza` |
| `ci` | Workflows de GitHub Actions | `ci/compilar-ut2` |

## Commits

`<tipo>(<ámbito>): <descripción en minúscula, en imperativo>`

- Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `ci`, `chore`.
- Ámbito: `ut2-practica1`, `ut2`, o se omite si afecta a todo el repositorio.
- El cuerpo explica el porqué cuando no es evidente.

```text
feat(ut2-practica1): implementar contador concurrente con hilos
fix(ut2-practica1): evitar condicion de carrera al sumar
docs(ut2): documentar resultados de la practica 2
```

En la UT1 se usó el ámbito `practicaN` (por ejemplo `fix(practica3): …`).

## Etiquetas

| Grupo | Etiquetas | Uso |
|-------|-----------|-----|
| Unidad | `UT1`, `UT2`… | Una por Issue/PR. Se crea al empezar cada unidad |
| Tipo | `practica`, `bug`, `enhancement`, `refactor`, `documentation` | Las plantillas ponen la suya |
| Prioridad | `priority:high`, `priority:medium`, `priority:low` | Solo si hay que priorizar |
| Estado | `blocked`, `needs-review` | Bloqueado por algo externo · PR o Issue pendiente de revisar |

La definición completa está en [`.github/labels.json`](.github/labels.json).

## Milestones y tablero

- Un **milestone por unidad** (`UT2 · <título de la unidad>`), sin fecha
  inventada: solo se pone la fecha de entrega oficial si se conoce. Se cierra
  cuando la unidad está evaluada, aunque queden Issues técnicos abiertos.
- Tablero [**PSP 2026-27**](https://github.com/users/DiegoHA17/projects/2)
  (GitHub Projects, privado), vinculado a este repositorio. Se configura con
  `node scripts/github/configurar-github.mjs proyecto --aplicar`.

### Estados

| Estado | Significado | Cómo llega |
|--------|-------------|------------|
| Por hacer | Issue creado, sin empezar | Automático al añadirse al tablero |
| En progreso | Rama creada, en desarrollo | A mano, al crear la rama |
| En revisión | PR abierto, pendiente de revisión y CI | Automático al vincular un PR al Issue |
| Terminado | Issue cerrado | Automático al cerrarse el Issue (`Closes #N` o a mano) |

Una práctica solo está *Terminada* cuando se cierra su Issue: fusionar un PR
con `Refs #N` no la termina. Por eso la vista **Tablero** muestra solo Issues
(`is:issue`) y los PR tienen su propia vista.

### Vistas

| Vista | Tipo | Filtro |
|-------|------|--------|
| Tablero | Kanban por estado | `is:issue` |
| Prácticas | Tabla con milestone y progreso de sub-issues | `is:issue label:practica` |
| Pull requests | Tabla | `is:pr` |
| Errores y mejoras | Tabla | `is:issue label:bug,enhancement,refactor,documentation` |

Para ver una unidad, se añade al filtro `label:UT2` o `milestone:"UT2 · …"`.

### Automatizaciones (Project → ⋯ → Workflows)

Activas: *Item added to project* → Por hacer, *Item closed* → Terminado,
*Pull request merged* → Terminado, *Auto-close issue*, *Auto-add sub-issues to
project* y *Pull request linked to issue*.

Ajustes que la API de GitHub no permite y se hacen una vez en la web:

1. *Pull request linked to issue*: estado destino **En revisión**.
2. *Item added to project*: aplicar solo a **Issues** (los PR no deben
   aparecer como «Por hacer»).
3. *Auto-add to project*: activarlo para `DiegoHA17/PSP_DX` con el filtro
   `is:issue,pr is:open`.
4. En la vista *Prácticas*: **Group by → Milestone** (agrupar por unidad).

## Añadir una unidad nueva

1. Crear la carpeta `UT<N>/` con sus proyectos y un `README.md` como el de
   [`UT1/README.md`](UT1/README.md).
2. Añadir la fila de la unidad en el [`README.md`](README.md) principal.
3. Crear la etiqueta `UT<N>` y el milestone (ver
   [`scripts/github/configurar-github.mjs`](scripts/github/configurar-github.mjs)).
4. Añadir el proyecto Gradle de consola a la matriz de
   [`.github/workflows/compilar.yml`](.github/workflows/compilar.yml) y, si
   hay proyecto Android, su ruta y un job en
   [`.github/workflows/android.yml`](.github/workflows/android.yml).
