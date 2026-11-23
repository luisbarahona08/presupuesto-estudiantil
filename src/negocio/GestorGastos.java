package negocio;

import modelo.Gasto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestorGastos {
    private final List<Gasto> listaGastos;
    private int contadorId;
    // Porcentajes sobre el límite de consumo B (por defecto 62.5% Necesidades y 37.5% Deseos -> 50% + 30% del 80% de B, o ajustables al 100%)
    private final Map<String, Double> porcentajesCategoria;

    public GestorGastos() {
        this.listaGastos = new ArrayList<>();
        this.contadorId = 1;
        this.porcentajesCategoria = new LinkedHashMap<>();
        this.porcentajesCategoria.put("NECESIDADES", 62.5);
        this.porcentajesCategoria.put("DESEOS", 37.5);
    }

    // RF2.1 - Crear gasto con ID único
    public Gasto registrarGasto(String descripcion, double monto, String categoria, LocalDate fecha) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del gasto no puede estar vacía.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser posterior a hoy.");
        }

        Gasto nuevo = new Gasto(contadorId++, descripcion.trim(), monto, categoria, fecha);
        listaGastos.add(nuevo);
        return nuevo;
    }

    // RF2.1 - Consultar / filtrar por mes y/o categoría (mes <= 0 o categoría vacía/null se omiten como filtro)
    public List<Gasto> consultarGastos(Integer mes, Integer anio, String categoria) {
        List<Gasto> filtrados = new ArrayList<>();
        boolean filtrarFecha = (mes != null && mes > 0 && anio != null && anio > 0);
        boolean filtrarCat = (categoria != null && !categoria.trim().isEmpty());

        for (Gasto g : listaGastos) {
            boolean coincideFecha = !filtrarFecha ||
                    (g.getFecha().getMonthValue() == mes && g.getFecha().getYear() == anio);
            boolean coincideCat = !filtrarCat ||
                    g.getCategoria().equalsIgnoreCase(categoria.trim());

            if (coincideFecha && coincideCat) {
                filtrados.add(g);
            }
        }
        return filtrados;
    }

    // RF2.1 - Actualizar gasto por ID
    public boolean actualizarGasto(int id, String nuevaDescripcion, double nuevoMonto, String nuevaCategoria, LocalDate nuevaFecha) {
        for (Gasto g : listaGastos) {
            if (g.getId() == id) {
                if (nuevaDescripcion == null || nuevaDescripcion.trim().isEmpty()) {
                    throw new IllegalArgumentException("La descripción no puede estar vacía.");
                }
                if (nuevaFecha == null || nuevaFecha.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("La fecha es inválida o posterior a hoy.");
                }
                g.setDescripcion(nuevaDescripcion.trim());
                g.setMonto(nuevoMonto);
                g.setCategoria(nuevaCategoria);
                g.setFecha(nuevaFecha);
                return true;
            }
        }
        return false;
    }

    // RF2.1 - Buscar gasto por ID (útil para pedir confirmación antes de eliminar)
    public Gasto buscarPorId(int id) {
        for (Gasto g : listaGastos) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    // RF2.1 - Eliminar gasto por ID
    public boolean eliminarGasto(int id) {
        return listaGastos.removeIf(g -> g.getId() == id);
    }

    // Total de gastos G en un mes y año
    public double calcularTotalGastos(int mes, int anio) {
        double total = 0.0;
        for (Gasto g : consultarGastos(mes, anio, null)) {
            total += g.getMonto();
        }
        return redondear(total);
    }

    // RF2.2 - Margen disponible M = B - G
    public double calcularMargen(double limiteConsumoB, int mes, int anio) {
        double gastosG = calcularTotalGastos(mes, anio);
        return redondear(limiteConsumoB - gastosG);
    }

    // RF2.2 - Ajustar porcentajes validando que sumen 100%
    public void ajustarPorcentajes(Map<String, Double> nuevosPorcentajes) {
        if (nuevosPorcentajes == null || nuevosPorcentajes.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar al menos una categoría con porcentaje.");
        }

        double suma = 0.0;
        Map<String, Double> normalizados = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : nuevosPorcentajes.entrySet()) {
            String cat = entry.getKey();
            Double pct = entry.getValue();
            if (cat == null || cat.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
            }
            if (pct == null || pct < 0 || pct > 100) {
                throw new IllegalArgumentException("Cada porcentaje debe estar entre 0 y 100.");
            }
            suma += pct;
            normalizados.put(cat.trim().toUpperCase(), redondear(pct));
        }

        if (Math.abs(redondear(suma) - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    String.format("Los porcentajes deben sumar exactamente 100%% (suman %.2f%%).", suma)
            );
        }

        porcentajesCategoria.clear();
        porcentajesCategoria.putAll(normalizados);
    }

    public Map<String, Double> getPorcentajesCategoria() {
        return new LinkedHashMap<>(porcentajesCategoria);
    }

    // RF2.2 - Alertas si M = 0, M < 0 (sobregasto) o si se pasa una categoría
    public List<String> generarAlertas(double limiteConsumoB, int mes, int anio) {
        List<String> alertas = new ArrayList<>();
        double gastosG = calcularTotalGastos(mes, anio);
        double margenM = redondear(limiteConsumoB - gastosG);

        if (limiteConsumoB <= 0 && gastosG > 0) {
            alertas.add("ALERTA: Tienes gastos registrados sin límite de consumo (B) definido en el mes.");
        } else if (margenM < 0) {
            alertas.add(String.format("ALERTA DE SOBREGAS­TO (M < 0): Excediste el límite B en $%.2f (Margen M: $%.2f).",
                    Math.abs(margenM), margenM));
        } else if (margenM == 0.0 && limiteConsumoB > 0) {
            alertas.add("ALERTA DE LÍMITE ALCANZADO (M = 0): Ya consumiste el 100% de tu límite B ($0.00 disponibles).");
        }

        for (Map.Entry<String, Double> entry : porcentajesCategoria.entrySet()) {
            String categoria = entry.getKey();
            double pct = entry.getValue();
            double topeCategoria = redondear((limiteConsumoB * pct) / 100.0);
            double gastadoCategoria = 0.0;

            for (Gasto g : consultarGastos(mes, anio, categoria)) {
                gastadoCategoria += g.getMonto();
            }
            gastadoCategoria = redondear(gastadoCategoria);

            if (gastadoCategoria > topeCategoria) {
                alertas.add(String.format(
                        "ALERTA DE CATEGORÍA [%s]: Gastaste $%.2f y superaste tu tope del %.1f%% ($%.2f) por $%.2f.",
                        categoria, gastadoCategoria, pct, topeCategoria, redondear(gastadoCategoria - topeCategoria)
                ));
            }
        }

        return alertas;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}