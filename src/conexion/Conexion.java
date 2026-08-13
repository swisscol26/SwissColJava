package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/database_swisscol"
            + "?useSSL=false"
            + "&serverTimezone=America/Bogota"
            + "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD =
        System.getenv("SWISSCOL_DB_PASSWORD");

    private Conexion() {
        // Evita crear objetos de esta clase.
    }

    public static Connection conectar() throws SQLException {
    if (PASSWORD == null || PASSWORD.isBlank()) {
        throw new SQLException(
                "No se encontró la variable SWISSCOL_DB_PASSWORD."
        );
    }

    return DriverManager.getConnection(URL, USER, PASSWORD);
}
}