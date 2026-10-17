package dto;

import java.math.BigDecimal;

public class PedidoResultado {

    private final int pedidoId;
    private final String estado;
    private final BigDecimal total;

    public PedidoResultado(
            int pedidoId,
            String estado,
            BigDecimal total) {

        this.pedidoId = pedidoId;
        this.estado = estado;
        this.total = total;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
