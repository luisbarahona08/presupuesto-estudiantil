package modelo;

import java.time.LocalDate;

public class Ingreso extends Transaccion {
    private String periodicidad;

    public Ingreso(int id, String origen, double monto, String periodicidad, LocalDate fecha) {
        super(id, origen, monto, fecha);
        this.periodicidad = periodicidad;
    }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }

    @Override
    public String getTipo() {
        return "INGRESO";
    }

    @Override
    public String toString() {
        return super.toString() + " | Periodicidad: " + periodicidad;
    }
}
