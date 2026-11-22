package interfaz;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.DoubleSupplier;

import modelo.MetaAhorro;
import negocio.GestorMetas;

/**
 * Submenú de consola del Módulo 3 (Simulador de Metas de Ahorro).
 *
 * MenuConsola (Luis) crea un objeto de esta clase y llama a mostrar(). Para dictaminar si una meta
 * es factible necesita saber cuánto ahorra realmente el estudiante por día; ese dato lo entregan
 * los módulos 1 y 2 y llega aquí como un DoubleSupplier. Ejemplo de uso desde MenuConsola:
 *
 *   MenuMetas menuMetas = new MenuMetas(gestorMetas, scanner,
 *           () -> (ahorroMensualDelMes + margenDelMes) / 30.0);
 *   menuMetas.mostrar();
 *
 * Toda lectura de datos está dentro de try-catch: un error de digitación nunca cierra el programa.
 */
public class MenuMetas {

    private final GestorMetas gestor;
    private final Scanner sc;
    private final DoubleSupplier ahorroDiarioReal;

    public MenuMetas(GestorMetas gestor, Scanner sc, DoubleSupplier ahorroDiarioReal) {
        this.gestor = gestor;
        this.sc = sc;
        this.ahorroDiarioReal = ahorroDiarioReal;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== METAS DE AHORRO =====");
            System.out.println("1. Crear meta");
            System.out.println("2. Consultar metas");
            System.out.println("3. Actualizar meta");
            System.out.println("4. Registrar aporte");
            System.out.println("5. Eliminar meta");
            System.out.println("6. Evaluar factibilidad (simulador)");
            System.out.println("0. Volver");
            opcion = leerEntero("Seleccione una opción", 0, 6);
            try {
                switch (opcion) {
                    case 1 -> crearMeta();
                    case 2 -> consultarMetas();
                    case 3 -> actualizarMeta();
                    case 4 -> registrarAporte();
                    case 5 -> eliminarMeta();
                    case 6 -> evaluarMeta();
                    default -> { }
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                // Errores de regla de negocio lanzados por GestorMetas: se muestran y se vuelve al menú.
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    // ---------------- Opciones ----------------

    private void crearMeta() {
        System.out.println("\n--- Crear meta de ahorro ---");
        String nombre = leerTexto("Nombre de la meta");
        double objetivo = leerDecimalPositivo("Monto objetivo ($)");
        LocalDate fecha = leerFechaFutura("Fecha límite (AAAA-MM-DD)");
        MetaAhorro meta = gestor.crearMeta(nombre, objetivo, fecha);
        System.out.println("Meta creada correctamente con ID " + meta.getId() + " (estado: " + meta.getEstado() + ").");
    }

    private void consultarMetas() {
        System.out.println("\n--- Consultar metas ---");
        System.out.println("1. Todas   2. Solo 'En Proceso'   3. Solo 'Cumplida'   4. Buscar por ID");
        int op = leerEntero("Seleccione", 1, 4);
        List<MetaAhorro> lista;
        switch (op) {
            case 2 -> lista = gestor.consultarPorEstado(MetaAhorro.ESTADO_EN_PROCESO);
            case 3 -> lista = gestor.consultarPorEstado(MetaAhorro.ESTADO_CUMPLIDA);
            case 4 -> {
                MetaAhorro m = gestor.obtenerMeta(leerTexto("ID de la meta"));
                lista = List.of(m);
            }
            default -> lista = gestor.listarMetas();
        }
        if (lista.isEmpty()) {
            System.out.println("No hay metas para mostrar.");
            return;
        }
        for (MetaAhorro m : lista) {
            System.out.println(m.mostrarDetalle());
        }
    }

    private void actualizarMeta() {
        System.out.println("\n--- Actualizar meta ---");
        MetaAhorro meta = gestor.obtenerMeta(leerTexto("ID de la meta"));
        System.out.println("Meta actual: " + meta.mostrarDetalle());
        String nombre = leerTexto("Nuevo nombre");
        double objetivo = leerDecimalPositivo("Nuevo monto objetivo ($)");
        LocalDate fecha = leerFechaFutura("Nueva fecha límite (AAAA-MM-DD)");
        gestor.actualizarMeta(meta.getId(), nombre, objetivo, fecha);
        System.out.println("Meta actualizada correctamente: " + meta.mostrarDetalle());
    }

    private void registrarAporte() {
        System.out.println("\n--- Registrar aporte ---");
        MetaAhorro meta = gestor.obtenerMeta(leerTexto("ID de la meta"));
        System.out.println("Meta: " + meta.mostrarDetalle());
        double monto = leerDecimalPositivo("Monto del aporte ($)");
        gestor.registrarAporte(meta.getId(), monto);
        System.out.println("Aporte registrado. " + meta.mostrarDetalle());
    }

    private void eliminarMeta() {
        System.out.println("\n--- Eliminar meta ---");
        MetaAhorro meta = gestor.obtenerMeta(leerTexto("ID de la meta"));
        System.out.println("Se eliminará: " + meta.mostrarDetalle());
        if (leerSiNo("¿Está seguro de eliminarla? Esta acción no se puede deshacer (s/n)")) {
            gestor.eliminarMeta(meta.getId());
            System.out.println("Meta eliminada correctamente.");
        } else {
            System.out.println("Operación cancelada. La meta no se eliminó.");
        }
    }

    private void evaluarMeta() {
        System.out.println("\n--- Simulador de factibilidad ---");
        MetaAhorro meta = gestor.obtenerMeta(leerTexto("ID de la meta"));
        double ahorro = ahorroDiarioReal.getAsDouble();
        GestorMetas.Dictamen d = gestor.evaluarFactibilidad(meta.getId(), ahorro);

        System.out.println("Meta: " + meta.getNombre());
        System.out.println(String.format(Locale.US, "Falta por ahorrar: $%.2f", meta.getMontoFaltante()));
        System.out.println("Días restantes hasta la fecha límite: " + d.getDiasRestantes());
        System.out.println(String.format(Locale.US, "Cuota requerida: $%.2f por día", d.getCuotaRequerida()));
        System.out.println(String.format(Locale.US, "Ahorro real: $%.2f por día", d.getAhorroDiarioReal()));
        System.out.println("Dictamen: " + d.getResultado().toUpperCase());
        if (d.getFechaReal() != null) {
            System.out.println("Fecha real estimada de cumplimiento: " + d.getFechaReal()
                    + " (en " + d.getDiasNecesarios() + " días)");
        }
        System.out.println(d.getMensaje());
    }

    // ---------------- Lectura con validación (try-catch) ----------------

    private String leerTexto(String campo) {
        while (true) {
            System.out.print(campo + ": ");
            String t = sc.nextLine().trim();
            if (!t.isEmpty()) {
                return t;
            }
            System.out.println("Error: el campo \"" + campo + "\" es obligatorio.");
        }
    }

    private int leerEntero(String campo, int min, int max) {
        while (true) {
            System.out.print(campo + ": ");
            String t = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(t);
                if (n < min || n > max) {
                    System.out.println("Error: ingrese un número entre " + min + " y " + max + ".");
                } else {
                    return n;
                }
            } catch (NumberFormatException e) {
                System.out.println(t.isEmpty()
                        ? "Error: el campo \"" + campo + "\" es obligatorio."
                        : "Error: ingrese un número entero válido.");
            }
        }
    }

    /** Decimal mayor que 0; acepta punto o coma como separador. */
    private double leerDecimalPositivo(String campo) {
        while (true) {
            System.out.print(campo + ": ");
            String t = sc.nextLine().trim().replace(',', '.');
            try {
                double d = Double.parseDouble(t);
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    System.out.println("Error: ingrese un número válido.");
                } else if (d <= 0) {
                    System.out.println("Error: \"" + campo + "\" debe ser mayor que 0.");
                } else {
                    return d;
                }
            } catch (NumberFormatException e) {
                System.out.println(t.isEmpty()
                        ? "Error: el campo \"" + campo + "\" es obligatorio."
                        : "Error: \"" + campo + "\" debe ser numérico (ej. 250.50).");
            }
        }
    }

    private LocalDate leerFecha(String campo) {
        while (true) {
            String t = leerTexto(campo);
            try {
                return LocalDate.parse(t);
            } catch (DateTimeParseException e) {
                System.out.println("Error: fecha inválida. Use el formato AAAA-MM-DD (ej. 2027-03-15).");
            }
        }
    }

    /** Pide la fecha hasta que sea válida y posterior a hoy (así no se pierden los demás datos). */
    private LocalDate leerFechaFutura(String campo) {
        while (true) {
            LocalDate f = leerFecha(campo);
            if (f.isAfter(LocalDate.now())) {
                return f;
            }
            System.out.println("Error: la fecha límite debe ser posterior a hoy (" + LocalDate.now() + ").");
        }
    }

    private boolean leerSiNo(String campo) {
        while (true) {
            String t = leerTexto(campo).toLowerCase();
            if (t.equals("s") || t.equals("si") || t.equals("sí")) {
                return true;
            }
            if (t.equals("n") || t.equals("no")) {
                return false;
            }
            System.out.println("Error: responda con 's' o 'n'.");
        }
    }
}
