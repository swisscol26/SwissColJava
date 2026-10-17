package dao;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutorizacionDAO {

    public boolean esAdministrador(
            int usuarioId) throws SQLException {

        String sql = """
            SELECT user_role
            FROM users
            WHERE user_id = ?
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement consulta =
                conexion.prepareStatement(sql)
        ) {

            consulta.setInt(1, usuarioId);

            try (ResultSet resultado =
                    consulta.executeQuery()) {

                if (!resultado.next()) {
                    return false;
                }

                return "ADMIN".equals(
                    resultado.getString("user_role")
                );
            }
        }
    }
}