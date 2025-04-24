package com.bionet.integration.camel

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.file.GenericFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class CsvFileRoute : RouteBuilder() {

    private val logger = LoggerFactory.getLogger(CsvFileRoute::class.java)

    override fun configure() {
        val inputDir = File("input-labs")
        if (inputDir.exists() && inputDir.isDirectory) {
            logger.info("📂 Carpeta 'input-labs' encontrada. Procesando archivos...")

            from("file:input-labs?noop=true&include=.*\\.csv&delay=1000&idempotent=true")
                .routeId("csvProcessingRoute")
                .process { exchange ->
                    val file = exchange.getIn().getBody(GenericFile::class.java)
                    val fileName = file.fileName
                    val fileContent = exchange.getIn().getBody(String::class.java)

                    // Guardar el contenido original del archivo para restaurarlo después
                    exchange.setProperty("originalFileContent", fileContent)

                    // Validar el encabezado y parsear el contenido
                    val records = try {
                        parseCsv(fileContent)
                    } catch (e: Exception) {
                        logger.error("❌ Error al parsear el archivo: '$fileName' - Moviendo a 'error'")
                        File("error").mkdirs()
                        exchange.getIn().setHeader("CamelFileName", "error/$fileName")
                        exchange.getIn().setBody(exchange.getProperty("originalFileContent"))
                        return@process
                    }

                    // Validar los registros
                    if (!isValidCsv(records)) {
                        logger.error("❌ Archivo inválido: '$fileName' - Moviendo a 'error'")
                        File("error").mkdirs()
                        exchange.getIn().setHeader("CamelFileName", "error/$fileName")
                        exchange.getIn().setBody(exchange.getProperty("originalFileContent"))
                        return@process
                    }

                    // Pasar los registros al servicio para procesarlos
                    exchange.getIn().setBody(records)
                    exchange.getIn().setHeader("CamelFileName", "processed/$fileName")
                }
                .choice()
                .`when`(simple("\${header.CamelFileName} startsWith 'error/'"))
                .to("file:.")
                .otherwise()
                .to("bean:resultService?method=processCsvRecords")
                .process { exchange ->
                    // Restaurar el contenido original del archivo antes de moverlo
                    exchange.getIn().setBody(exchange.getProperty("originalFileContent"))
                }
                .to("file:.")
                .log("✅ Archivo procesado: \${header.CamelFileName}")
                .end()
        } else {
            logger.error("❌ La carpeta 'input-labs' no existe o no es un directorio.")
        }
    }

    private fun parseCsv(fileContent: String): List<Map<String, String>> {
        val lines = fileContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IllegalArgumentException("Archivo vacío")

        // Leer el encabezado
        val header = lines.first().split(",").map { it.trim() }
        val expectedHeader = listOf("laboratorio_id", "paciente_id", "tipo_examen", "resultado", "fecha_examen")
        if (header != expectedHeader) {
            throw IllegalArgumentException("Encabezado inválido. Se esperaba: $expectedHeader, pero se encontró: $header")
        }

        // Parsear las filas (omitimos el encabezado)
        return lines.drop(1).map { line ->
            val values = line.split(",").map { it.trim() }
            if (values.size != header.size) {
                throw IllegalArgumentException("Número de columnas incorrecto en la línea: $line")
            }
            header.zip(values).toMap()
        }
    }

    private fun isValidCsv(records: List<Map<String, String>>): Boolean {
        return records.all { record ->
            record["laboratorio_id"]?.isNotBlank() == true &&
                    record["paciente_id"]?.isNotBlank() == true &&
                    record["tipo_examen"]?.isNotBlank() == true &&
                    record["resultado"]?.isNotBlank() == true &&
                    record["fecha_examen"]?.isNotBlank() == true
        }
    }
}