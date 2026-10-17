package dto;

public class NotificacionResultado {

    private final int notificacionId;
    private final String mensaje;
    private final String tipo;
    private final String fecha;
    private final boolean leida;

    public NotificacionResultado(
            int notificacionId,
            String mensaje,
            String tipo,
            String fecha,
            boolean leida) {

        this.notificacionId = notificacionId;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fecha = fecha;
        this.leida = leida;
    }

    public int getNotificacionId() {
        return notificacionId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }
}