package dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoHistorialResultado {

    private final int pedidoId;
    private final String fecha;
    private final String estado;
    private final BigDecimal total;
    private final List<DetallePedidoResultado> productos;

    private BigDecimal costoEnvio;
    private String venceReserva;
    private String pagoReportadoEn;
    private String referenciaPago;

    public PedidoHistorialResultado(
            int pedidoId,
            String fecha,
            String estado,
            BigDecimal total,
            List<DetallePedidoResultado> productos) {

        this.pedidoId = pedidoId;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
        this.productos = List.copyOf(productos);
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<DetallePedidoResultado> getProductos() {
        return productos;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public String getVenceReserva() {
        return venceReserva;
    }

    public void setVenceReserva(String venceReserva) {
        this.venceReserva = venceReserva;
    }

    public String getPagoReportadoEn() {
        return pagoReportadoEn;
    }

    public void setPagoReportadoEn(String pagoReportadoEn) {
        this.pagoReportadoEn = pagoReportadoEn;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }
}