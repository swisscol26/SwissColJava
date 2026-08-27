package pruebas;

import dto.AuthResultado;
import dto.LoginSolicitud;
import dto.RegistroSolicitud;
import services.AuthService;

public class AuthServicePrueba {

    public static void main(String[] args) {

        AuthService authService =
            new AuthService();

        RegistroSolicitud registro =
            new RegistroSolicitud();

        registro.setNombre("Usuario de Prueba");
        registro.setCorreo(
            "prueba.auth@swisscol.com"
        );
        registro.setPassword("ClaveSegura123");
        registro.setConfirmarPassword(
            "ClaveSegura123"
        );

        AuthResultado resultadoRegistro =
            authService.registrar(registro);

        mostrarResultado(
            "REGISTRO",
            resultadoRegistro
        );

        LoginSolicitud loginCorrecto =
            new LoginSolicitud();

        loginCorrecto.setCorreo(
            "prueba.auth@swisscol.com"
        );
        loginCorrecto.setPassword(
            "ClaveSegura123"
        );

        mostrarResultado(
            "LOGIN CORRECTO",
            authService.iniciarSesion(loginCorrecto)
        );

        LoginSolicitud loginIncorrecto =
            new LoginSolicitud();

        loginIncorrecto.setCorreo(
            "prueba.auth@swisscol.com"
        );
        loginIncorrecto.setPassword(
            "ClaveIncorrecta"
        );

        mostrarResultado(
            "LOGIN INCORRECTO",
            authService.iniciarSesion(loginIncorrecto)
        );
    }

    private static void mostrarResultado(
            String prueba,
            AuthResultado resultado) {

        System.out.println();
        System.out.println("=== " + prueba + " ===");
        System.out.println(
            "Código: " + resultado.getCodigoHttp()
        );
        System.out.println(
            "Éxito: " + resultado.isExito()
        );
        System.out.println(
            "Mensaje: " + resultado.getMensaje()
        );
    }
}