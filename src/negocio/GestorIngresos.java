package negocio;

import modelo.Ingreso;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorIngresos {
    private List<Ingreso> listaIngresos;
    private int contadorId;

    public GestorIngresos() {
        this.listaIngresos = new ArrayList<>();
        this.contadorId = 1;
    }

    public Ingreso registrarIngreso(String origen, double monto, String periodicidad, LocalDate fecha) {
        if (origen == null || origen.trim().isEmpty()) {
            throw new IllegalArgumentException("El origen del ingreso no puede estar vacío.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser posterior a hoy.");
        }
        Ingreso nuevo = new Ingreso(contadorId++, origen.trim(), monto, periodicidad, fecha);
        listaIngresos.add(nuevo);
        return nuevo;
    }

    public List<Ingreso> listarIngresos() {
        return new ArrayList<>(listaIngresos);
    }

    public List<Ingreso> listarPorMes(int mes, int anio) {
        List<Ingreso> filtrados = new ArrayList<>();
        for (Ingreso ing : listaIngresos) {
            if (ing.getFecha().getMonthValue() == mes && ing.getFecha().getYear() == anio) {
                filtrados.add(ing);
            }
        }
        return filtrados;
    }

    public boolean actualizarIngreso(int id, String nuevoOrigen, double nuevoMonto, String nuevaPeriodicidad) {
        for (Ingreso ing : listaIngresos) {
            if (ing.getId() == id) {
                ing.setDescripcion(nuevoOrigen);
                ing.setMonto(nuevoMonto);
                ing.setPeriodicidad(nuevaPeriodicidad);
                return true;
            }
        }
        return false;
    }

    public boolean eliminarIngreso(int id) {
        return listaIngresos.removeIf(ing -> ing.getId() == id);
    }

    public double calcularIngresoTotal(int mes, int anio) {
        double total = 0.0;
        for (Ingreso ing : listarPorMes(mes, anio)) {
            total += ing.getMonto();
        }
        return redondear(total);
    }

    public String generarDiagnostico503020(int mes, int anio) {
        double ingresoTotal = calcularIngresoTotal(mes, anio);
        if (ingresoTotal <= 0) {
            return "No hay ingresos registrados para el período " + mes + "/" + anio + ".";
        }

        double necesidades = redondear(ingresoTotal * 0.50);
        double deseos = redondear(ingresoTotal * 0.30);
        double ahorro = redondear(ingresoTotal - necesidades - deseos);

        double limiteConsumo = redondear(ingresoTotal - ahorro);
        double margenDiario = redondear(limiteConsumo / 30.0);

        return String.format(
                "\n Diagnostico Presupuetario 50/30/20 (%02d/%d) \n" +
                        "Ingreso Total Consolidado (I): $%.2f\n" +
                        "---------------------------------------------------\n" +
                        "  * Necesidades Básicas (50%%): $%.2f\n" +
                        "  * Deseos / Ocio (30%%):       $%.2f\n" +
                        "  * Ahorro Objetivo (20%%):     $%.2f\n" +
                        "---------------------------------------------------\n" +
                        "Límite Máximo de Consumo (B):  $%.2f\n" +
                        "Margen Diario Recomendado:     $%.2f / día\n",
                mes, anio, ingresoTotal, necesidades, deseos, ahorro, limiteConsumo, margenDiario
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}