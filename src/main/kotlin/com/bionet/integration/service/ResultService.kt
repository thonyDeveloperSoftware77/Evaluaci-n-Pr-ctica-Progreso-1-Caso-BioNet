package com.bionet.integration.service

import com.bionet.integration.model.ResultadosExamenes
import com.bionet.integration.repository.ResultadosExamenesRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ResultService(private val repository: ResultadosExamenesRepository) {

    private val logger = LoggerFactory.getLogger(ResultService::class.java)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun processCsvRecords(records: List<Map<String, String>>) {
        records.forEach { record ->
            val examen = ResultadosExamenes(
                laboratorioId = record["laboratorio_id"]!!,
                pacienteId = record["paciente_id"]!!,
                tipoExamen = record["tipo_examen"]!!,
                resultado = record["resultado"]!!,
                fechaExamen = LocalDate.parse(record["fecha_examen"], dateFormatter)
            )

            val exists = repository.existsByLaboratorioIdAndPacienteIdAndTipoExamenAndFechaExamen(
                examen.laboratorioId, examen.pacienteId, examen.tipoExamen, examen.fechaExamen
            )

            if (!exists) {
                repository.save(examen)
                logger.info("✅ Registro insertado: ${examen.pacienteId}, ${examen.tipoExamen}")
            } else {
                logger.warn("⚠️ Registro duplicado omitido: ${examen.pacienteId}, ${examen.tipoExamen}")
            }
        }
    }
}