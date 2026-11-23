package modelo;

import java.time.LocalDate;

public abstract class Transaccion {
    private int id;
    private String descripcion;
    private double monto;
    private LocalDate fecha;

    public Transaccion(int id, String descripcion, double monto, LocalDate fecha) {
        this.id = id;
        this.descripcion = descripcion;
        setMonto(monto);
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        this.monto = Math.round(monto * 100.0) / 100.0;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format("ID: %d | Fecha: %s | %s | Monto: $%.2f", id, fecha, descripcion, monto);
    }
}
