package services;

import dao.NotificacionDAO;
import dto.NotificacionResultado;
import seguridad.AccesoException;
import seguridad.GestorSesiones;
import seguridad.Sesion;

import java.sql.SQLException;
import java.util.List;

public class NotificacionService {

    private final NotificacionDAO notificacionDAO =
        new NotificacionDAO();

    public List<NotificacionResultado> listar(
            String token) throws SQLException {

        Sesion sesion = exigirSesion(token);

        return notificacionDAO.listarPorUsuario(
            sesion.getUsuarioId()
        );
    }

    public void marcarComoLeida(
            String token,
            int notificacionId) throws SQLException {

        Sesion sesion = exigirSesion(token);

        if (notificacionId <= 0) {
            throw new IllegalArgumentException(
                "El número de notificación debe ser mayor que cero."
            );
        }

        boolean encontrada = notificacionDAO.marcarComoLeida(
            notificacionId,
            sesion.getUsuarioId()
        );

        if (!encontrada) {
            throw new AccesoException(
                404,
                "Notificación no encontrada."
            );
        }
    }

    private Sesion exigirSesion(String token) {

        Sesion sesion = GestorSesiones.obtenerSesion(token);

        if (sesion == null) {
            throw new AccesoException(
                401,
                "Debes iniciar sesión para consultar tus notificaciones."
            );
        }

        return sesion;
    }

    public void crearParaPedido(
            int pedidoId,
            String mensaje,
            String tipo) throws SQLException {

        notificacionDAO.crearParaPedido(
            pedidoId,
            mensaje,
            tipo
        );
    }

}
