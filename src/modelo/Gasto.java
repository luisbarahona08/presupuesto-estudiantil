package modelo;

import java.time.LocalDate;

public class Gasto extends Transaccion {
    private String categoria;

    public Gasto(int id, String descripcion, double monto, String categoria, LocalDate fecha) {
        super(id, descripcion, monto, fecha);
        setCategoria(categoria);
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría del gasto no puede estar vacía.");
        }
        this.categoria = categoria.trim();
    }

    @Override
    public String getTipo() {
        return "GASTO";
    }

    @Override
    public String toString() {
        return super.toString() + " | Categoría: " + categoria;
    }
}