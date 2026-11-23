# Práctica 1 — Planificación de procesos

El trabajo de esta práctica está en el documento de entrega
(`documentacion/UT-1_entrega_v1.pdf`, páginas 2-4). Esta página **no lo rehace**:
recoge la comprobación de los resultados y las dos correcciones que hay que
trasladar al documento.

## Datos

| Proceso | Llegada | Ráfaga | Prioridad (menor = más prioridad) |
|---------|--------:|-------:|----------------------------------:|
| P1 | 0 | 6 | 3 |
| P2 | 1 | 2 | 1 |
| P3 | 2 | 8 | 4 |
| P4 | 3 | 3 | 2 |
| P5 | 5 | 4 | 5 |

Retorno = fin − llegada · Espera = retorno − ráfaga.

## Comprobación de los cuatro algoritmos pedidos

Diagramas de Gantt (coinciden con los del documento):

```text
FCFS             | P1 0-6 | P2 6-8 | P3 8-16 | P4 16-19 | P5 19-23 |
SJF no apropiat. | P1 0-6 | P2 6-8 | P4 8-11 | P5 11-15 | P3 15-23 |
RR (q = 3)       | P1 0-3 | P2 3-5 | P3 5-8 | P4 8-11 | P1 11-14 | P5 14-17 | P3 17-20 | P5 20-21 | P3 21-23 |
Prioridades n.a. | P1 0-6 | P2 6-8 | P4 8-11 | P3 11-19 | P5 19-23 |
```

En RR, en t = 3 llega P4 a la vez que P1 agota su quantum: P4 entra antes en la
cola (regla del enunciado), por eso la cola en t = 3 es P2, P3, P4, P1.

| Algoritmo | Fin P1..P5 | Retorno P1..P5 | Espera P1..P5 | Retorno medio | Espera media |
|-----------|-----------|----------------|---------------|--------------:|-------------:|
| FCFS | 6, 8, 16, 19, 23 | 6, 7, 14, 16, 18 | 0, 5, 6, 13, 14 | **12,2** | **7,6** |
| SJF no apropiativo | 6, 8, 23, 11, 15 | 6, 7, 21, 8, 10 | 0, 5, 13, 5, 6 | **10,4** | **5,8** |
| Round Robin q = 3 | 14, 5, 23, 11, 21 | 14, 4, 21, 8, 16 | 8, 2, 13, 5, 12 | **12,6** | **8,0** |
| Prioridades no apropiativo | 6, 8, 19, 11, 23 | 6, 7, 17, 8, 18 | 0, 5, 9, 5, 14 | **11,2** | **6,6** |

Las tablas por algoritmo del documento son **correctas** (fin, retorno, espera y
medias). SRTF y prioridades apropiativo también son correctos, pero no se piden.

## Correcciones que hay que hacer en el documento

1. **Tabla resumen (apartado b), columna «Retorno medio»: es errónea.** Contiene
   el tiempo de *respuesta* medio, no el de retorno. Valores correctos:

   | Algoritmo | Espera media | Retorno medio |
   |-----------|-------------:|--------------:|
   | FCFS | 7,6 | 12,2 |
   | SJF no apropiativo | 5,8 | 10,4 |
   | Round Robin q = 3 | 8,0 | 12,6 |
   | Prioridades no apropiativo | 6,6 | 11,2 |
   | *(extra) SRTF* | *4,6* | *9,2* |
   | *(extra) Prioridades apropiativo* | *5,6* | *10,2* |

2. **«¿Cuál da la menor espera media?»**: de los cuatro algoritmos pedidos es
   **SJF no apropiativo (5,8)**. SRTF (4,6) es menor, pero es apropiativo y no
   está entre los cuatro del enunciado; conviene decirlo así.

3. *(Revisable)* «¿A qué proceso perjudica más FCFS?»: el documento dice P2. Con
   los números, el más perjudicado es **P5 por espera (14)** o **P4 si se mide
   retorno/ráfaga (16/3 ≈ 5,3)**, ambos por el efecto convoy tras P3. El resto
   de respuestas (SJF → P3, RR → P3, prioridades → P5) son coherentes con las
   tablas.

La respuesta del apartado c) (Round Robin para sistemas interactivos; quantum 1
→ muchos cambios de contexto; quantum 20 → se comporta como FCFS) es correcta.
