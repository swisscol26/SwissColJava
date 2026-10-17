package seguridad;

public class AccesoException extends RuntimeException {

    private final int codigoHttp;

    public AccesoException(int codigoHttp, String mensaje) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}