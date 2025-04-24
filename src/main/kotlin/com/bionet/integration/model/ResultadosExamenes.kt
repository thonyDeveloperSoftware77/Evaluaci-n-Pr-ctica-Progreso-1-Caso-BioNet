package com.bionet.integration.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "resultados_examenes",
    indexes = [Index(name = "idx_unique_result", columnList = "laboratorio_id,paciente_id,tipo_examen,fecha_examen", unique = true)]
)
data class ResultadosExamenes(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "laboratorio_id", nullable = false)
    val laboratorioId: String,

    @Column(name = "paciente_id", nullable = false)
    val pacienteId: String,

    @Column(name = "tipo_examen", nullable = false)
    val tipoExamen: String,

    @Column(name = "resultado", nullable = false)
    val resultado: String,

    @Column(name = "fecha_examen", nullable = false)
    val fechaExamen: LocalDate
)