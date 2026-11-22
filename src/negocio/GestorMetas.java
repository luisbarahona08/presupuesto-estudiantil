package negocio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.MetaAhorro;

/**
 * Módulo 3: Simulador de Metas de Ahorro.
 *
 * RF3.1 (CRUD): crear, consultar, actualizar y eliminar metas de ahorro.
 * RF3.2 (lógica): calcular la cuota requerida por día, compararla con el ahorro real
 * (que entregan los módulos 1 y 2) y dictaminar si la meta es "Factible" o "Inviable",
 * calculando la nueva fecha real de cumplimiento.
 *
 * Esta clase no lee datos del usuario ni imprime: eso lo hace la capa interfaz.
 * Cuando un dato es inválido lanza IllegalArgumentException (o IllegalStateException
 * si la operación no se puede hacer en el estado actual) con un mensaje claro.
 */
public class GestorMetas {

    public static final String DICTAMEN_FACTIBLE = "Factible";
    public static final String DICTAMEN_INVIABLE = "Inviable";

    /** Si harían falta más días que esto, no se calcula fecha (≈ 100 años). */
    private static final long MAX_DIAS_PROYECCION = 36_500;

    private final List<MetaAhorro> metas = new ArrayList<>();
    private int contadorId = 0; // solo crece, así un ID eliminado nunca se reutiliza

    // ---------------------------------------------------------------
    // RF3.1 - CRUD
    // ---------------------------------------------------------------

    /** Crea una meta en estado "En Proceso" con ID único (M001, M002, ...). */
    public MetaAhorro crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite) {
        return crearMeta(nombre, montoObjetivo, fechaLimite, LocalDate.now());
    }

    public MetaAhorro crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, LocalDate hoy) {
        validarFechaFutura(fechaLimite, hoy);
        String id = String.format("M%03d", contadorId + 1);
        MetaAhorro meta = new MetaAhorro(id, nombre, montoObjetivo, fechaLimite); // valida nombre y monto
        contadorId++;
        metas.add(meta);
        return meta;
    }

    /** Devuelve una copia de la lista de metas (no se puede modificar desde afuera). */
    public List<MetaAhorro> listarMetas() {
        return Collections.unmodifiableList(new ArrayList<>(metas));
    }

    /** Consulta las metas de un estado, por ejemplo "En Proceso" o "Cumplida". */
    public List<MetaAhorro> consultarPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado a consultar es obligatorio.");
        }
        List<MetaAhorro> resultado = new ArrayList<>();
        for (MetaAhorro m : metas) {
            if (m.getEstado().equalsIgnoreCase(estado.trim())) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    /** Busca una meta por ID; devuelve null si no existe. */
    public MetaAhorro buscarPorId(String id) {
        if (id == null) return null;
        for (MetaAhorro m : metas) {
            if (m.getId().equalsIgnoreCase(id.trim())) {
                return m;
            }
        }
        return null;
    }

    /** Igual que buscarPorId, pero lanza excepción si la meta no existe. */
    public MetaAhorro obtenerMeta(String id) {
        MetaAhorro meta = buscarPorId(id);
        if (meta == null) {
            throw new IllegalArgumentException("No existe una meta con el ID: " + (id == null ? "(vacío)" : id.trim()));
        }
        return meta;
    }

    /** Actualiza nombre, monto objetivo y fecha límite. Si no hay error, aplica todos los cambios. */
    public MetaAhorro actualizarMeta(String id, String nombre, double montoObjetivo, LocalDate fechaLimite) {
        return actualizarMeta(id, nombre, montoObjetivo, fechaLimite, LocalDate.now());
    }

    public MetaAhorro actualizarMeta(String id, String nombre, double montoObjetivo,
                                     LocalDate fechaLimite, LocalDate hoy) {
        MetaAhorro meta = obtenerMeta(id);
        validarFechaFutura(fechaLimite, hoy);
        // Se prueba todo en una meta temporal antes de tocar la real: o se cambia todo o nada.
        new MetaAhorro(meta.getId(), nombre, montoObjetivo, fechaLimite);
        meta.setNombre(nombre);
        meta.setMontoObjetivo(montoObjetivo);
        meta.setFechaLimite(fechaLimite);
        actualizarEstado(meta);
        return meta;
    }

    /** Suma un aporte al dinero ahorrado de la meta; si llega al objetivo pasa a "Cumplida". */
    public MetaAhorro registrarAporte(String id, double monto) {
        if (Double.isNaN(monto) || Double.isInfinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El aporte debe ser mayor que 0.");
        }
        MetaAhorro meta = obtenerMeta(id);
        meta.setMontoAhorrado(meta.getMontoAhorrado() + monto);
        actualizarEstado(meta);
        return meta;
    }

    /** Elimina una meta. Devuelve true si existía. (La confirmación se pide en la interfaz.) */
    public boolean eliminarMeta(String id) {
        MetaAhorro meta = buscarPorId(id);
        if (meta == null) return false;
        return metas.remove(meta);
    }

    // ---------------------------------------------------------------
    // RF3.2 - Lógica: cuota requerida y dictamen de factibilidad
    // ---------------------------------------------------------------

    /** Días que quedan hasta la fecha límite (0 o negativo si ya venció). */
    public long calcularDiasRestantes(MetaAhorro meta, LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, meta.getFechaLimite());
    }

    /**
     * CuotaRequerida = (Objetivo - Ahorrado) / DíasRestantes.
     * Devuelve 0 si la meta ya llegó al objetivo.
     *
     * @throws IllegalStateException si la fecha límite ya venció o es hoy y todavía falta dinero
     */
    public double calcularCuotaRequerida(MetaAhorro meta, LocalDate hoy) {
        double faltante = meta.getMontoFaltante();
        if (faltante == 0) return 0;
        long dias = calcularDiasRestantes(meta, hoy);
        if (dias <= 0) {
            throw new IllegalStateException("La fecha límite de la meta ya venció o es hoy: "
                    + "no se puede calcular la cuota diaria. Actualice la fecha límite.");
        }
        return faltante / dias;
    }

    /** Resultado de comparar la cuota requerida con el ahorro real. */
    public static class Dictamen {
        private final String resultado;          // "Factible" o "Inviable"
        private final double cuotaRequerida;     // $/día que hacen falta
        private final double ahorroDiarioReal;   // $/día que realmente se ahorra
        private final long diasRestantes;        // hasta la fecha límite
        private final long diasNecesarios;       // con el ahorro real; -1 si no se puede estimar
        private final LocalDate fechaReal;       // nueva fecha real de cumplimiento; null si no se puede estimar
        private final String mensaje;

        Dictamen(String resultado, double cuotaRequerida, double ahorroDiarioReal, long diasRestantes,
                 long diasNecesarios, LocalDate fechaReal, String mensaje) {
            this.resultado = resultado;
            this.cuotaRequerida = cuotaRequerida;
            this.ahorroDiarioReal = ahorroDiarioReal;
            this.diasRestantes = diasRestantes;
            this.diasNecesarios = diasNecesarios;
            this.fechaReal = fechaReal;
            this.mensaje = mensaje;
        }

        public String getResultado() { return resultado; }
        public double getCuotaRequerida() { return cuotaRequerida; }
        public double getAhorroDiarioReal() { return ahorroDiarioReal; }
        public long getDiasRestantes() { return diasRestantes; }
        public long getDiasNecesarios() { return diasNecesarios; }
        public LocalDate getFechaReal() { return fechaReal; }
        public String getMensaje() { return mensaje; }
        public boolean esFactible() { return DICTAMEN_FACTIBLE.equals(resultado); }
    }

    /**
     * Compara la cuota requerida con el ahorro diario real y dictamina la meta.
     *
     * @param ahorroDiarioReal dólares que el estudiante ahorra realmente por día (lo calculan los
     *                         módulos 1 y 2; si es 0 o negativo, la meta no avanza)
     */
    public Dictamen evaluarFactibilidad(String id, double ahorroDiarioReal) {
        return evaluarFactibilidad(id, ahorroDiarioReal, LocalDate.now());
    }

    public Dictamen evaluarFactibilidad(String id, double ahorroDiarioReal, LocalDate hoy) {
        if (Double.isNaN(ahorroDiarioReal) || Double.isInfinite(ahorroDiarioReal)) {
            throw new IllegalArgumentException("El ahorro diario real no es un número válido.");
        }
        MetaAhorro meta = obtenerMeta(id);
        double faltante = meta.getMontoFaltante();
        long diasRestantes = calcularDiasRestantes(meta, hoy);

        // Meta que ya llegó al objetivo.
        if (faltante == 0) {
            return new Dictamen(DICTAMEN_FACTIBLE, 0, ahorroDiarioReal, diasRestantes, 0, hoy,
                    "La meta ya alcanzó su objetivo.");
        }

        // Cuántos días harían falta con el ahorro real y qué fecha resulta.
        long diasNecesarios = -1;
        LocalDate fechaReal = null;
        if (ahorroDiarioReal > 0) {
            long dias = (long) Math.ceil(faltante / ahorroDiarioReal - 1e-9);
            if (dias <= MAX_DIAS_PROYECCION) {
                diasNecesarios = Math.max(1, dias);
                fechaReal = hoy.plusDays(diasNecesarios);
            }
        }

        // Fecha límite vencida: no existe cuota diaria; la meta no se cumplió a tiempo.
        if (diasRestantes <= 0) {
            String msg = fechaReal != null
                    ? "La fecha límite ya venció. Con el ahorro actual se lograría el " + fechaReal + "."
                    : "La fecha límite ya venció y con el ahorro actual la meta no avanza.";
            return new Dictamen(DICTAMEN_INVIABLE, 0, ahorroDiarioReal, diasRestantes,
                    diasNecesarios, fechaReal, msg);
        }

        double cuota = faltante / diasRestantes;
        boolean factible = ahorroDiarioReal >= cuota - 1e-9;
        if (factible) {
            return new Dictamen(DICTAMEN_FACTIBLE, cuota, ahorroDiarioReal, diasRestantes, diasNecesarios,
                    fechaReal, "Con el ahorro actual la meta se cumple antes de la fecha límite.");
        }
        String msg = fechaReal != null
                ? "El ahorro actual no alcanza para la cuota requerida. Nueva fecha real: " + fechaReal + "."
                : "El ahorro actual no alcanza y no permite estimar una fecha de cumplimiento.";
        return new Dictamen(DICTAMEN_INVIABLE, cuota, ahorroDiarioReal, diasRestantes, diasNecesarios,
                fechaReal, msg);
    }

    // ---------------------------------------------------------------
    // Utilidades internas
    // ---------------------------------------------------------------

    private void validarFechaFutura(LocalDate fecha, LocalDate hoy) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha límite es obligatoria.");
        }
        if (!fecha.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha límite debe ser posterior a hoy (" + hoy + ").");
        }
    }

    /** Pasa la meta a "Cumplida" si llegó al objetivo, o de vuelta a "En Proceso" si ya no. */
    private void actualizarEstado(MetaAhorro meta) {
        if (meta.getMontoAhorrado() >= meta.getMontoObjetivo()) {
            meta.setEstado(MetaAhorro.ESTADO_CUMPLIDA);
        } else {
            meta.setEstado(MetaAhorro.ESTADO_EN_PROCESO);
        }
    }
}
