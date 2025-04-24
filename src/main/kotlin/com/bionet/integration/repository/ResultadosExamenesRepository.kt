package com.bionet.integration.repository

import com.bionet.integration.model.ResultadosExamenes
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ResultadosExamenesRepository : JpaRepository<ResultadosExamenes, Long> {
    fun existsByLaboratorioIdAndPacienteIdAndTipoExamenAndFechaExamen(
        laboratorioId: String,
        pacienteId: String,
        tipoExamen: String,
        fechaExamen: LocalDate
    ): Boolean
}