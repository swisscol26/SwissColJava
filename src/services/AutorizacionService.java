package services;

import dao.AutorizacionDAO;
import seguridad.GestorSesiones;
import seguridad.Sesion;

import java.sql.SQLException;

public class AutorizacionService {

    private final AutorizacionDAO autorizacionDAO =
        new AutorizacionDAO();

    public int comprobarAdministrador(
            String token) throws SQLException {

        Sesion sesion = GestorSesiones.obtenerSesion(token);

        if (sesion == null) {
            return 401;
        }

        if (!autorizacionDAO.esAdministrador(
                sesion.getUsuarioId())) {
            return 403;
        }

        return 200;
    }
}