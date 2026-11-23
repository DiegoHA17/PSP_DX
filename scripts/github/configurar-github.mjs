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
const ESTADOS = [
  { name: "Por hacer", color: "GRAY", description: "Pendiente de empezar" },
  { name: "En progreso", color: "YELLOW", description: "Rama abierta y en desarrollo" },
  { name: "En revisión", color: "PURPLE", description: "Pull Request abierto" },
  { name: "Terminado", color: "GREEN", description: "Fusionado o completado" },
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

function proyecto() {
  const lista = JSON.parse(gh(["project", "list", "--owner", OWNER, "--format", "json"], { lectura: true }));
  let numero = lista.projects.find((p) => p.title === TITULO_PROYECTO)?.number;

  if (numero) {
    console.log(`Project ya existe: #${numero} "${TITULO_PROYECTO}"`);
  } else {
    const creado = gh(["project", "create", "--owner", OWNER, "--title", TITULO_PROYECTO, "--format", "json"]);
    if (!aplicar) {
      console.log(`[simulación] gh project link <nuevo> --owner ${OWNER} --repo ${REPO}`);
      console.log(`[simulación] estados del campo Status: ${ESTADOS.map((e) => e.name).join(" · ")}`);
      return;
    }
    numero = JSON.parse(creado).number;
  }

  gh(["project", "link", String(numero), "--owner", OWNER, "--repo", REPO]);

  // Sustituye las opciones del campo Status (Todo / In Progress / Done) por las cuatro de la asignatura.
  const campos = JSON.parse(gh(["project", "field-list", String(numero), "--owner", OWNER, "--format", "json"], { lectura: true }));
  const status = campos.fields.find((c) => c.name === "Status");
  const actuales = (status?.options ?? []).map((o) => o.name);
  if (ESTADOS.every((e) => actuales.includes(e.name))) {
    console.log("El campo Status ya tiene los cuatro estados.");
    return;
  }
  const opciones = ESTADOS.map((e) => `{name: ${JSON.stringify(e.name)}, color: ${e.color}, description: ${JSON.stringify(e.description)}}`).join(", ");
  const mutacion = `mutation { updateProjectV2Field(input: {fieldId: "${status.id}", singleSelectOptions: [${opciones}]}) { projectV2Field { ... on ProjectV2SingleSelectField { options { name } } } } }`;
  try {
    gh(["api", "graphql", "-f", `query=${mutacion}`]);
  } catch (error) {
    console.error("No se pudieron cambiar los estados automáticamente; edítalos a mano en el campo Status del Project.");
    console.error(String(error.stderr ?? error.message));
    process.exitCode = 1;
  }
}

const ordenes = { etiquetas, "milestone-ut1": milestoneUt1, unidad: () => unidad(...resto), proyecto };
if (!ordenes[orden]) {
  console.error("Órdenes: etiquetas | milestone-ut1 | unidad UTn \"Título\" | proyecto   (añade --aplicar para ejecutar)");
  process.exit(2);
}
if (!aplicar) console.log("Modo simulación: no se modifica nada en GitHub. Añade --aplicar para ejecutar.\n");
ordenes[orden]();
