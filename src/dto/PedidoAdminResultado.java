package dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoAdminResultado
        extends PedidoHistorialResultado {

    private final int usuarioId;
    private final String nombreCliente;
    private final String correoCliente;

    private String nombreDestinatario;
    private String telefonoDestinatario;
    private String departamento;
    private String ciudad;
    private String direccion;
    private String barrio;
    private String indicaciones;

    public PedidoAdminResultado(
            int pedidoId,
            String fecha,
            String estado,
            BigDecimal total,
            List<DetallePedidoResultado> productos,
            int usuarioId,
            String nombreCliente,
            String correoCliente) {

        super(
            pedidoId,
            fecha,
            estado,
            total,
            productos
        );

        this.usuarioId = usuarioId;
        this.nombreCliente = nombreCliente;
        this.correoCliente = correoCliente;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public String getNombreDestinatario() {
        return nombreDestinatario;
    }

    public String getTelefonoDestinatario() {
        return telefonoDestinatario;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getBarrio() {
        return barrio;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setDatosEnvio(
            String nombreDestinatario,
            String telefonoDestinatario,
            String departamento,
            String ciudad,
            String direccion,
            String barrio,
            String indicaciones) {

        this.nombreDestinatario = nombreDestinatario;
        this.telefonoDestinatario = telefonoDestinatario;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.barrio = barrio;
        this.indicaciones = indicaciones;
    }

}