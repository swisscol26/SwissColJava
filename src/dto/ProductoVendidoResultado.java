package dto;

import java.math.BigDecimal;

public class ProductoVendidoResultado {

    private final int productoId;
    private final String nombre;
    private final long unidadesVendidas;
    private final BigDecimal totalVendido;

    public ProductoVendidoResultado(
            int productoId,
            String nombre,
            long unidadesVendidas,
            BigDecimal totalVendido) {

        this.productoId = productoId;
        this.nombre = nombre;
        this.unidadesVendidas = unidadesVendidas;
        this.totalVendido = totalVendido;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public long getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }
}
