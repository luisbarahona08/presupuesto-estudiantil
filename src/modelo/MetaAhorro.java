package modelo;

import java.time.LocalDate;

/**
 * Meta de ahorro del estudiante (entidad del paquete modelo).
 *
 * NOTA: versión provisional escrita por Jahir para poder compilar y probar GestorMetas.
 * Luis es dueño del paquete modelo: si su MetaAhorro tiene otros nombres, solo hay que
 * ajustar las llamadas en GestorMetas. Lo que necesita GestorMetas de esta clase es:
 * id, nombre, montoObjetivo, montoAhorrado, fechaLimite y estado.
 */
public class MetaAhorro {

    public static final String ESTADO_EN_PROCESO = "En Proceso";
    public static final String ESTADO_CUMPLIDA = "Cumplida";

    private String id;
    private String nombre;
    private double montoObjetivo;
    private double montoAhorrado;
    private LocalDate fechaLimite;
    private String estado;

    public MetaAhorro(String id, String nombre, double montoObjetivo, LocalDate fechaLimite) {
        setId(id);
        setNombre(nombre);
        setMontoObjetivo(montoObjetivo);
        setFechaLimite(fechaLimite);
        this.montoAhorrado = 0;
        this.estado = ESTADO_EN_PROCESO;
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la meta es obligatorio.");
        }
        this.id = id.trim();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la meta es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public double getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(double montoObjetivo) {
        if (Double.isNaN(montoObjetivo) || Double.isInfinite(montoObjetivo) || montoObjetivo <= 0) {
            throw new IllegalArgumentException("El monto objetivo debe ser mayor que 0.");
        }
        this.montoObjetivo = montoObjetivo;
    }

    public double getMontoAhorrado() { return montoAhorrado; }
    public void setMontoAhorrado(double montoAhorrado) {
        if (Double.isNaN(montoAhorrado) || Double.isInfinite(montoAhorrado) || montoAhorrado < 0) {
            throw new IllegalArgumentException("El monto ahorrado no puede ser negativo.");
        }
        this.montoAhorrado = montoAhorrado;
    }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) {
        if (fechaLimite == null) {
            throw new IllegalArgumentException("La fecha límite es obligatoria.");
        }
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la meta es obligatorio.");
        }
        this.estado = estado.trim();
    }

    /** Dinero que todavía falta para llegar al objetivo (nunca negativo). */
    public double getMontoFaltante() {
        return Math.max(0, montoObjetivo - montoAhorrado);
    }

    public String mostrarDetalle() {
        return String.format(java.util.Locale.US,
                "[%s] %s | Objetivo: $%.2f | Ahorrado: $%.2f | Fecha límite: %s | Estado: %s",
                id, nombre, montoObjetivo, montoAhorrado, fechaLimite, estado);
    }

    @Override
    public String toString() {
        return mostrarDetalle();
    }
}
