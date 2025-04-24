CREATE TABLE resultados_examenes (
    id BIGSERIAL PRIMARY KEY,
    laboratorio_id VARCHAR(50) NOT NULL,
    paciente_id VARCHAR(50) NOT NULL,
    tipo_examen VARCHAR(100) NOT NULL,
    resultado TEXT NOT NULL,
    fecha_examen DATE NOT NULL,
    CONSTRAINT unique_result UNIQUE (laboratorio_id, paciente_id, tipo_examen, fecha_examen)
);

CREATE TABLE log_cambios_resultados (
    id BIGSERIAL PRIMARY KEY,
    operacion VARCHAR(10) NOT NULL,
    paciente_id VARCHAR(50) NOT NULL,
    tipo_examen VARCHAR(100) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION log_resultados_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO log_cambios_resultados (operacion, paciente_id, tipo_examen, fecha)
VALUES (TG_OP, NEW.paciente_id, NEW.tipo_examen, CURRENT_TIMESTAMP);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER log_resultados_trigger
    AFTER INSERT OR UPDATE ON resultados_examenes
    FOR EACH ROW
    EXECUTE FUNCTION log_resultados_trigger_function();