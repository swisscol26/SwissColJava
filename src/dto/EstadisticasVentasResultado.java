package dto;

import java.math.BigDecimal;
import java.util.List;

public class EstadisticasVentasResultado {

    private final long pedidosConfirmados;
    private final BigDecimal totalVendido;
    private final List<ProductoVendidoResultado> productosMasVendidos;

    public EstadisticasVentasResultado(
            long pedidosConfirmados,
            BigDecimal totalVendido,
            List<ProductoVendidoResultado> productosMasVendidos) {

        this.pedidosConfirmados = pedidosConfirmados;
        this.totalVendido = totalVendido;
        this.productosMasVendidos =
            List.copyOf(productosMasVendidos);
    }

    public long getPedidosConfirmados() {
        return pedidosConfirmados;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    public List<ProductoVendidoResultado> getProductosMasVendidos() {
        return productosMasVendidos;
    }
}