package dao;

import conexion.Conexion;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    /**
     * Busca un usuario por su correo electrónico.
     */
    public Usuario buscarPorCorreo(String email)
            throws SQLException {

        String sql = """
            SELECT user_id,
                   user_name,
                   email,
                   password,
                   user_role,
                   created_at
            FROM users
            WHERE email = ?
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps =
                conexion.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }

        return null;
    }

    /**
     * Registra un usuario y devuelve el ID generado por MySQL.
     */
    public int insertarUsuario(Usuario usuario)
            throws SQLException {

        String sql = """
            INSERT INTO users
                (user_name, email, password, user_role)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps =
                conexion.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setString(1, usuario.getUserName());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getUserRole());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException(
                    "No se pudo registrar el usuario."
                );
            }

            try (ResultSet claves = ps.getGeneratedKeys()) {

                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
            "MySQL no devolvió el ID del usuario."
        );
    }

    /**
     * Convierte una fila de MySQL en un objeto Usuario.
     */
    private Usuario mapearUsuario(ResultSet rs)
            throws SQLException {

        Usuario usuario = new Usuario();

        usuario.setUserId(rs.getInt("user_id"));
        usuario.setUserName(rs.getString("user_name"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password"));
        usuario.setUserRole(rs.getString("user_role"));
        usuario.setCreatedAt(rs.getTimestamp("created_at"));

        return usuario;
    }
}
