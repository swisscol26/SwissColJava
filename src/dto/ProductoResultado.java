package dto;

import modelo.Producto;

public class ProductoResultado {

    private final boolean exito;
    private final String mensaje;
    private final Producto producto;

    private transient final int codigoHttp;

    private ProductoResultado(
            boolean exito,
            int codigoHttp,
            String mensaje,
            Producto producto) {

        this.exito = exito;
        this.codigoHttp = codigoHttp;
        this.mensaje = mensaje;
        this.producto = producto;
    }

    public static ProductoResultado error(
            int codigoHttp,
            String mensaje) {

        return new ProductoResultado(
            false,
            codigoHttp,
            mensaje,
            null
        );
    }

    public static ProductoResultado exitoso(
            int codigoHttp,
            String mensaje,
            Producto producto) {

        return new ProductoResultado(
            true,
            codigoHttp,
            mensaje,
            producto
        );
    }

    public boolean isExito() {
        return exito;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Producto getProducto() {
        return producto;
    }
}
