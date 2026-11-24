#!/usr/bin/env node
// Configura etiquetas, milestones y el tablero de GitHub Projects del
// repositorio con GitHub CLI (gh). Es idempotente: se puede repetir.
//
// Por defecto SOLO MUESTRA lo que haría. Para ejecutarlo de verdad: --aplicar
//
//   node scripts/github/configurar-github.mjs etiquetas            [--aplicar]
//   node scripts/github/configurar-github.mjs milestone-ut1        [--aplicar]
//   node scripts/github/configurar-github.mjs unidad UT2 "Título"  [--aplicar]
//   node scripts/github/configurar-github.mjs proyecto             [--aplicar]
//
// Requisitos: gh autenticado (gh auth status) con permisos repo y project.

import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const REPO = "DiegoHA17/PSP_DX";
const OWNER = REPO.split("/")[0];

const TITULO_PROYECTO = "PSP 2026-27";
// Project vacío («untitled») que se reutiliza en lugar de crear otro.
// Solo se reutiliza si sigue vacío y sin renombrar. El Project #1 no se toca.
const PROYECTO_REUTILIZABLE = 2;
const DESCRIPCION_PROYECTO = "Seguimiento de las prácticas de Programación de Servicios y Procesos (2.º DAM).";
const README_PROYECTO = `Tablero de las prácticas del repositorio ${REPO}.

**Estados (solo Issues en la vista Tablero)**
- Por hacer: Issue creado, trabajo sin empezar.
- En progreso: rama creada, en desarrollo.
- En revisión: Pull Request abierto, pendiente de revisión y CI.
- Terminado: Issue cerrado (requisitos cumplidos y PR fusionado).

Un PR fusionado no termina una práctica: la práctica termina cuando se cierra su Issue.
Unidad = etiqueta UTn y milestone «UTn · …». Tipo = practica, bug, enhancement, refactor, documentation.
Flujo completo: CONTRIBUTING.md del repositorio.`;

// Nombre nuevo, color y descripción. "anterior" = opción por defecto de GitHub cuyo id se conserva
// para que las automatizaciones que ya apuntan a ella sigan funcionando.
const ESTADOS = [
  { name: "Por hacer", anterior: "Todo", color: "GRAY", description: "Issue creado; trabajo sin empezar" },
  { name: "En progreso", anterior: "In Progress", color: "YELLOW", description: "Rama creada; en desarrollo" },
  { name: "En revisión", anterior: null, color: "PURPLE", description: "Pull Request abierto, pendiente de revisión y CI" },
  { name: "Terminado", anterior: "Done", color: "GREEN", description: "Issue cerrado: requisitos cumplidos y PR fusionado" },
];

const VISTAS = [
  { name: "Tablero", layout: "BOARD_LAYOUT", filter: "is:issue", campos: [] },
  {
    name: "Prácticas",
    layout: "TABLE_LAYOUT",
    filter: "is:issue label:practica",
    campos: ["Title", "Status", "Milestone", "Labels", "Sub-issues progress", "Linked pull requests"],
  },
  { name: "Pull requests", layout: "TABLE_LAYOUT", filter: "is:pr", campos: ["Title", "Status", "Milestone", "Labels", "Reviewers"] },
  {
    name: "Errores y mejoras",
    layout: "TABLE_LAYOUT",
    filter: "is:issue label:bug,enhancement,refactor,documentation",
    campos: ["Title", "Status", "Milestone", "Labels", "Linked pull requests"],
  },
];

const args = process.argv.slice(2);
const aplicar = args.includes("--aplicar");
const [orden, ...resto] = args.filter((a) => a !== "--aplicar");
const raiz = join(dirname(fileURLToPath(import.meta.url)), "..", "..");

/** Ejecuta gh (o solo lo muestra en modo simulación). */
function gh(argumentos, { lectura = false } = {}) {
  const texto = "gh " + argumentos.map((a) => (/[\s"]/.test(a) ? JSON.stringify(a) : a)).join(" ");
  if (!aplicar && !lectura) {
    console.log("[simulación] " + texto);
    return "";
  }
  if (!lectura) console.log("[ejecutando] " + texto);
  return execFileSync("gh", argumentos, { encoding: "utf8" }).trim();
}

/** Consulta o mutación GraphQL. Las mutaciones solo se muestran en modo simulación. */
function graphql(query, variables = {}, { lectura = false, resumen = "" } = {}) {
  if (!aplicar && !lectura) {
    console.log("[simulación] " + resumen);
    return null;
  }
  if (!lectura) console.log("[ejecutando] " + resumen);
  const salida = execFileSync("gh", ["api", "graphql", "--input", "-"], {
    encoding: "utf8",
    input: JSON.stringify({ query, variables }),
  });
  const json = JSON.parse(salida);
  if (json.errors) throw new Error(JSON.stringify(json.errors));
  return json.data;
}

function etiquetas() {
  const lista = JSON.parse(readFileSync(join(raiz, ".github", "labels.json"), "utf8"));
  for (const { name, color, description } of lista) {
    // --force actualiza color y descripción si la etiqueta ya existe.
    gh(["label", "create", name, "--repo", REPO, "--color", color, "--description", description, "--force"]);
  }
}

function buscarMilestone(titulo) {
  const json = gh(["api", `repos/${REPO}/milestones?state=all&per_page=100`], { lectura: true });
  return JSON.parse(json).find((m) => m.title === titulo);
}

/** Crea el milestone si no existe (sin fecha: no se inventan fechas de entrega). */
function milestone(titulo, descripcion, estado) {
  const existente = buscarMilestone(titulo);
  if (existente) {
    console.log(`Milestone ya existe: #${existente.number} "${titulo}" (${existente.state})`);
    return;
  }
  gh(["api", `repos/${REPO}/milestones`, "-f", `title=${titulo}`, "-f", `state=${estado}`, "-f", `description=${descripcion}`]);
}

function milestoneUt1() {
  milestone(
    "UT1 · Procesos y servicios",
    "Unidad evaluada y cerrada. Desarrollada antes de usar Issues y PR. " +
      "Problemas técnicos abiertos: UT1/documentacion/REVISION.md",
    "closed",
  );
}

function unidad(codigo, titulo) {
  if (!/^UT\d+$/.test(codigo ?? "") || !titulo) {
    console.error('Uso: unidad UT2 "Título de la unidad" [--aplicar]');
    process.exit(2);
  }
  const numero = codigo.slice(2);
  gh(["label", "create", codigo, "--repo", REPO, "--color", "1d76db", "--description", `Unidad ${numero} · ${titulo}`, "--force"]);
  milestone(`${codigo} · ${titulo}`, `Prácticas de la unidad ${numero}.`, "open");
}

// ---------------------------------------------------------------- Project

const CONSULTA_PROYECTOS = `query($login: String!) {
  user(login: $login) {
    id
    projectsV2(first: 50) {
      nodes {
        id number title closed public
        items(first: 1) { totalCount }
        repositories(first: 20) { nodes { nameWithOwner } }
      }
    }
  }
}`;

const CONSULTA_DETALLE = `query($id: ID!) {
  node(id: $id) {
    ... on ProjectV2 {
      fields(first: 50) {
        nodes {
          ... on ProjectV2FieldCommon { id name }
          ... on ProjectV2SingleSelectField { options { id name color description } }
        }
      }
      views(first: 20) { nodes { id name layout filter } }
    }
  }
}`;

/** Localiza el Project de la asignatura, reutilizando el #2 vacío si hace falta. */
function localizarProyecto() {
  const datos = graphql(CONSULTA_PROYECTOS, { login: OWNER }, { lectura: true });
  const proyectos = datos.user.projectsV2.nodes;

  const existente = proyectos.find((p) => p.title === TITULO_PROYECTO);
  if (existente) {
    console.log(`Project ya configurado: #${existente.number} "${TITULO_PROYECTO}"`);
    return existente;
  }

  const reutilizable = proyectos.find(
    (p) => p.number === PROYECTO_REUTILIZABLE && p.items.totalCount === 0 && / untitled project$/.test(p.title),
  );
  if (reutilizable) {
    console.log(`Se reutiliza el Project #${reutilizable.number} (vacío) como "${TITULO_PROYECTO}".`);
    return reutilizable;
  }

  console.log(`No hay Project reutilizable: se creará uno nuevo.`);
  const creado = graphql(
    `mutation($owner: ID!, $title: String!) { createProjectV2(input: {ownerId: $owner, title: $title}) { projectV2 { id number title items(first:1){totalCount} repositories(first:1){nodes{nameWithOwner}} } } }`,
    { owner: datos.user.id, title: TITULO_PROYECTO },
    { resumen: `crear Project "${TITULO_PROYECTO}"` },
  );
  return creado?.createProjectV2.projectV2 ?? null;
}

function proyecto() {
  const p = localizarProyecto();
  if (!p) return; // simulación de un Project que aún no existe

  // Título, descripción y README. No se cambia la visibilidad (sigue privado).
  graphql(
    `mutation($id: ID!, $title: String!, $desc: String!, $readme: String!) { updateProjectV2(input: {projectId: $id, title: $title, shortDescription: $desc, readme: $readme}) { projectV2 { number } } }`,
    { id: p.id, title: TITULO_PROYECTO, desc: DESCRIPCION_PROYECTO, readme: README_PROYECTO },
    { resumen: `título, descripción y README del Project #${p.number} (visibilidad sin cambios)` },
  );

  // Vínculo con el repositorio.
  if (p.repositories.nodes.some((r) => r.nameWithOwner === REPO)) {
    console.log(`Ya vinculado a ${REPO}.`);
  } else {
    const repoId = gh(["api", `repos/${REPO}`, "--jq", ".node_id"], { lectura: true });
    graphql(
      `mutation($p: ID!, $r: ID!) { linkProjectV2ToRepository(input: {projectId: $p, repositoryId: $r}) { repository { nameWithOwner } } }`,
      { p: p.id, r: repoId },
      { resumen: `vincular el Project #${p.number} a ${REPO}` },
    );
  }

  const detalle = graphql(CONSULTA_DETALLE, { id: p.id }, { lectura: true }).node;
  const campos = detalle.fields.nodes.filter((c) => c.id);
  const idCampo = (nombre) => campos.find((c) => c.name === nombre)?.id;

  // Estados: se conservan los ids de Todo / In Progress / Done.
  const status = campos.find((c) => c.name === "Status");
  const opciones = ESTADOS.map((e) => {
    const previa = status.options.find((o) => o.name === e.name || o.name === e.anterior);
    return { ...(previa ? { id: previa.id } : {}), name: e.name, color: e.color, description: e.description };
  });
  const iguales =
    status.options.length === opciones.length &&
    opciones.every((o, i) => ["name", "color", "description"].every((k) => status.options[i][k] === o[k]));
  if (iguales) {
    console.log("Estados ya configurados.");
  } else {
    graphql(
      `mutation($f: ID!, $o: [ProjectV2SingleSelectFieldOptionInput!]) { updateProjectV2Field(input: {fieldId: $f, singleSelectOptions: $o}) { projectV2Field { ... on ProjectV2SingleSelectField { options { name } } } } }`,
      { f: status.id, o: opciones },
      { resumen: `estados de Status: ${opciones.map((o) => o.name + (o.id ? " (id conservado)" : " (nuevo)")).join(" · ")}` },
    );
  }

  // Vistas: la vista inicial "View 1" se convierte en "Tablero".
  const vistas = detalle.views.nodes;
  for (const v of VISTAS) {
    let actual = vistas.find((x) => x.name === v.name);
    if (!actual && v.name === "Tablero") actual = vistas.find((x) => x.name === "View 1");
    const configuracion = v.campos.length ? { visibleFieldIds: v.campos.map(idCampo).filter(Boolean) } : undefined;

    if (!actual) {
      const creada = graphql(
        `mutation($p: ID!, $n: String!, $l: ProjectV2ViewLayout!) { createProjectV2View(input: {projectId: $p, name: $n, layout: $l}) { projectV2View { id } } }`,
        { p: p.id, n: v.name, l: v.layout },
        { resumen: `crear vista "${v.name}" (${v.layout})` },
      );
      actual = { id: creada?.createProjectV2View.projectV2View.id, name: v.name };
    } else if (actual.name === v.name && actual.layout === v.layout && actual.filter === v.filter && !configuracion) {
      console.log(`Vista "${v.name}" ya configurada.`);
      continue;
    }

    graphql(
      `mutation($v: ID!, $n: String!, $l: ProjectV2ViewLayout!, $f: String!, $c: ProjectV2ViewConfigurationInput) { updateProjectV2View(input: {viewId: $v, name: $n, layout: $l, filter: $f, configuration: $c}) { projectV2View { name } } }`,
      { v: actual.id ?? "(nueva)", n: v.name, l: v.layout, f: v.filter, c: configuracion ?? null },
      { resumen: `vista "${v.name}": ${v.layout}, filtro "${v.filter}"${v.campos.length ? ", campos " + v.campos.join(", ") : ""}` },
    );
  }

  console.log(`\nPendiente en la web (no lo permite la API): ver CONTRIBUTING.md, sección «Milestones y tablero».`);
}

const ordenes = { etiquetas, "milestone-ut1": milestoneUt1, unidad: () => unidad(...resto), proyecto };
if (!ordenes[orden]) {
  console.error('Órdenes: etiquetas | milestone-ut1 | unidad UTn "Título" | proyecto   (añade --aplicar para ejecutar)');
  process.exit(2);
}
if (!aplicar) console.log("Modo simulación: no se modifica nada en GitHub. Añade --aplicar para ejecutar.\n");
ordenes[orden]();
