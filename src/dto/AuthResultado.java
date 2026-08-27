package dto;

public class AuthResultado {

    private final boolean exito;
    private final String mensaje;
    private final Integer usuarioId;
    private final String nombre;
    private final String correo;
    private final String rol;

    private transient final int codigoHttp;

    private AuthResultado(
            boolean exito,
            int codigoHttp,
            String mensaje,
            Integer usuarioId,
            String nombre,
            String correo,
            String rol) {

        this.exito = exito;
        this.codigoHttp = codigoHttp;
        this.mensaje = mensaje;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public static AuthResultado error(
            int codigoHttp,
            String mensaje) {

        return new AuthResultado(
            false,
            codigoHttp,
            mensaje,
            null,
            null,
            null,
            null
        );
    }

    public static AuthResultado exitoso(
            int codigoHttp,
            String mensaje,
            int usuarioId,
            String nombre,
            String correo,
            String rol) {

        return new AuthResultado(
            true,
            codigoHttp,
            mensaje,
            usuarioId,
            nombre,
            correo,
            rol
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

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}