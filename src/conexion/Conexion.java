package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/database_swisscol";
    private static final String USER = "root";
    private static final String PASSWORD = "Bonder981225.";

    public static Connection conectar() {

        try {

            Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexión exitosa.");

            return conexion;

        } catch (SQLException e) {

            System.out.println("Error de conexión: " + e.getMessage());

            return null;
        }

    }

}