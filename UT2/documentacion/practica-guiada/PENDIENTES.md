# Estado de la entrega de la práctica guiada

La implementación y la documentación de los Retos A, B y C están completas.
Las salidas originales de las pruebas, los prompts, las respuestas de Codex y
los diffs se conservan en [evidencias/](evidencias/README.md).

## Documentos privados

La carpeta local `entrega/` contiene:

- `UT2_Hoja_de_registro_FINAL.docx`.
- `UT2_Hoja_de_registro_FINAL.pdf`.
- `capturas/`, con las cuatro capturas reales de IntelliJ.
- `UT2_Entrega.zip`, con el Word, el PDF y las cuatro imágenes.

La carpeta completa está excluida de Git mediante `.gitignore`. Los documentos
con el nombre del alumno y las capturas con la ruta de usuario se entregan en la
plataforma del profesor; no se publican en GitHub. La versión pública de la hoja
es [HOJA_REGISTRO.md](HOJA_REGISTRO.md).

## Capturas realizadas

| Imagen | Ejecución | Resultado |
|--------|-----------|-----------|
| `captura1_TaquillaIA.png` | `Verificador` con `TaquillaIA` | R1–R5 rotas; veredicto incorrecto |
| `captura2_TaquillaSegura.png` | `Verificador` con `TaquillaSegura` | R1–R5 OK |
| `captura3_VerificadorEspera.png` | `VerificadorEspera` con `TaquillaEspera` | E1–E6 OK |
| `captura4_if_roto.png` | Variante temporal con `if` en lugar de `while` | `NoSuchElementException`, E3 rota en 4/5 rondas y veredicto incorrecto |

Las capturas son de nuevas ejecuciones; los números de fallos y los tiempos
pueden variar respecto a las mediciones anteriores de las tablas. Después de
obtenerlas se restauraron `fabrica = ::TaquillaSegura` y el `while` original.

## Seguimiento

- Issue principal: [#4](https://github.com/DiegoHA17/PSP_DX/issues/4).
- Retos A, B y C: [#5](https://github.com/DiegoHA17/PSP_DX/issues/5),
  [#6](https://github.com/DiegoHA17/PSP_DX/issues/6) y
  [#7](https://github.com/DiegoHA17/PSP_DX/issues/7).
- Pull request de integración: [#8](https://github.com/DiegoHA17/PSP_DX/pull/8),
  con commits identificables por reto y compilación en GitHub Actions.

Antes de enviar la entrega al profesor, el alumno debe revisar su reflexión y
la columna de revisión personal, que se han conservado en los documentos.
