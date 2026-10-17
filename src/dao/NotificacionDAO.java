package dao;

import conexion.Conexion;
import dto.NotificacionResultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public List<NotificacionResultado> listarPorUsuario(
            int usuarioId) throws SQLException {

        String sql = """
            SELECT not_id, message, type, date, is_read
            FROM notifications
            WHERE users_id = ?
            ORDER BY date DESC, not_id DESC
            """;

        List<NotificacionResultado> notificaciones =
            new ArrayList<>();

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement consulta =
                conexion.prepareStatement(sql)
        ) {
            consulta.setInt(1, usuarioId);

            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    notificaciones.add(
                        new NotificacionResultado(
                            filas.getInt("not_id"),
                            filas.getString("message"),
                            filas.getString("type"),
                            filas.getDate("date").toString(),
                            filas.getBoolean("is_read")
                        )
                    );
                }
            }
        }

        return notificaciones;
    }

    public boolean marcarComoLeida(
            int notificacionId,
            int usuarioId) throws SQLException {

        String sql = """
            UPDATE notifications
            SET is_read = TRUE
            WHERE not_id = ?
            AND users_id = ?
            AND is_read = FALSE
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement actualizacion =
                conexion.prepareStatement(sql)
        ) {
            actualizacion.setInt(1, notificacionId);
            actualizacion.setInt(2, usuarioId);

            if (actualizacion.executeUpdate() > 0) {
                return true;
            }

            // Si ya estaba leída, la operación también es válida.
            String consultar = """
                SELECT 1
                FROM notifications
                WHERE not_id = ?
                AND users_id = ?
                """;

            try (PreparedStatement consulta =
                    conexion.prepareStatement(consultar)) {

                consulta.setInt(1, notificacionId);
                consulta.setInt(2, usuarioId);

                try (ResultSet filas = consulta.executeQuery()) {
                    return filas.next();
                }
            }
        }
    }

    public void crearParaPedido(
            int pedidoId,
            String mensaje,
            String tipo) throws SQLException {

        if (pedidoId <= 0) {
            throw new IllegalArgumentException(
                "El número del pedido no es válido."
            );
        }

        String mensajeLimpio = mensaje == null
            ? ""
            : mensaje.trim();

        String tipoLimpio = tipo == null
            ? ""
            : tipo.trim().toUpperCase();

        if (mensajeLimpio.isEmpty()
                || mensajeLimpio.length() > 500) {
            throw new IllegalArgumentException(
                "La notificación debe tener entre 1 y 500 caracteres."
            );
        }

        if (tipoLimpio.isEmpty()
                || tipoLimpio.length() > 30) {
            throw new IllegalArgumentException(
                "El tipo de notificación no es válido."
            );
        }

        String sql = """
            INSERT INTO notifications
                (message, type, date, is_read, users_id)
            SELECT ?, ?, ?, FALSE, o.users_id
            FROM orders o
            WHERE o.order_id = ?
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement insercion =
                conexion.prepareStatement(sql)
        ) {
            insercion.setString(1, mensajeLimpio);
            insercion.setString(2, tipoLimpio);
            insercion.setDate(
                3,
                java.sql.Date.valueOf(
                    java.time.LocalDate.now(
                        java.time.ZoneId.of("America/Bogota")
                    )
                )
            );
            insercion.setInt(4, pedidoId);

            if (insercion.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                    "No existe el pedido indicado para crear la notificación."
                );
            }
        }
    }

}