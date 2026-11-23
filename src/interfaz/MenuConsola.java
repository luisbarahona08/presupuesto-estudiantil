package interfaz;

import modelo.Gasto;
import modelo.Ingreso;
import negocio.GestorGastos;
import negocio.GestorIngresos;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuConsola {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GestorIngresos gestorIngresos = new GestorIngresos();
    private static final GestorGastos gestorGastos = new GestorGastos();

    public static void main(String[] args) {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("  Control de Presupuesto Estudiantil   ");
            System.out.println("1. Módulo 1: Gestión de Ingresos y Diagnóstico 50/30/20");
            System.out.println("2. Módulo 2: Control de Gastos y Alertas");
            System.out.println("3. Módulo 3: Simulador de Metas de Ahorro");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1 -> menuModuloIngresos();
                    case 2 -> System.out.println(">> Módulo 2 en desarrollo.");
                    case 3 -> System.out.println(">> Módulo 3 en desarrollo.");
                    case 0 -> System.out.println("Saliendo del sistema...");
                    default -> System.out.println("Opción no válida. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número entero válido.");
            }
        }
    }

    private static void menuModuloIngresos() {
        int op = -1;
        while (op != 0) {
            System.out.println("\n Modulo 1: Ingreso y Diagnostico 50/30/20 ");
            System.out.println("1. Registrar ingreso");
            System.out.println("2. Listar todos los ingresos");
            System.out.println("3. Actualizar ingreso");
            System.out.println("4. Eliminar ingreso");
            System.out.println("5. Ver Diagnóstico 50/30/20 del mes");
            System.out.println("0. Volver al menú principal");
            System.out.print("Opción: ");

            try {
                op = Integer.parseInt(scanner.nextLine());
                switch (op) {
                    case 1 -> {
                        System.out.print("Origen del ingreso (ej. Mesada, Trabajo): ");
                        String origen = scanner.nextLine();
                        System.out.print("Monto en USD: ");
                        double monto = Double.parseDouble(scanner.nextLine());
                        System.out.print("Periodicidad (Mensual/Quincenal/Eventual): ");
                        String per = scanner.nextLine();
                        System.out.print("Fecha (AAAA-MM-DD, Enter para hoy): ");
                        String fechaStr = scanner.nextLine();
                        LocalDate fecha = fechaStr.isEmpty() ? LocalDate.now() : LocalDate.parse(fechaStr);

                        Ingreso ing = gestorIngresos.registrarIngreso(origen, monto, per, fecha);
                        System.out.println("Ingreso registrado con éxito: " + ing);
                    }
                    case 2 -> {
                        List<Ingreso> lista = gestorIngresos.listarIngresos();
                        if (lista.isEmpty()) {
                            System.out.println("No hay ingresos registrados.");
                        } else {
                            lista.forEach(System.out::println);
                        }
                    }
                    case 3 -> {
                        System.out.print("ID del ingreso a actualizar: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Nuevo origen: ");
                        String origen = scanner.nextLine();
                        System.out.print("Nuevo monto USD: ");
                        double monto = Double.parseDouble(scanner.nextLine());
                        System.out.print("Nueva periodicidad: ");
                        String per = scanner.nextLine();

                        if (gestorIngresos.actualizarIngreso(id, origen, monto, per)) {
                            System.out.println("Ingreso actualizado correctamente.");
                        } else {
                            System.out.println("No se encontró un ingreso con ese ID.");
                        }
                    }
                    case 4 -> {
                        System.out.print("ID del ingreso a eliminar: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        if (gestorIngresos.eliminarIngreso(id)) {
                            System.out.println("Ingreso eliminado.");
                        } else {
                            System.out.println("No se encontró un ingreso con ese ID.");
                        }
                    }
                    case 5 -> {
                        System.out.print("Ingrese el mes (1-12): ");
                        int mes = Integer.parseInt(scanner.nextLine());
                        System.out.print("Ingrese el año (ej. 2026): ");
                        int anio = Integer.parseInt(scanner.nextLine());
                        System.out.println(gestorIngresos.generarDiagnostico503020(mes, anio));
                    }
                    case 0 -> {}
                    default -> System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Formato numérico inválido.");
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha inválido. Use AAAA-MM-DD.");
            } catch (IllegalArgumentException e) {
                System.out.println("Validación: " + e.getMessage());
            }
        }
    }
    private static double obtenerLimiteConsumoB(int mes, int anio) {
        double ingresoTotal = gestorIngresos.calcularIngresoTotal(mes, anio);
        if (ingresoTotal <= 0) {
            return 0.0;
        }
        double necesidades = Math.round(ingresoTotal * 0.50 * 100.0) / 100.0;
        double deseos = Math.round(ingresoTotal * 0.30 * 100.0) / 100.0;
        double ahorro = Math.round((ingresoTotal - necesidades - deseos) * 100.0) / 100.0;
        return Math.round((ingresoTotal - ahorro) * 100.0) / 100.0;
    }
}