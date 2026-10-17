package dao;

public class AccesoDatosException extends RuntimeException {

    private final String sqlState;
    private final int codigoError;

    public AccesoDatosException(
            String mensaje,
            Exception causa,
            String sqlState,
            int codigoError) {

        super(mensaje, causa);
        this.sqlState = sqlState;
        this.codigoError = codigoError;
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getCodigoError() {
        return codigoError;
    }
}
