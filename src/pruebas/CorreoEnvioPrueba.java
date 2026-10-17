package pruebas;

import services.CorreoService;

public class CorreoEnvioPrueba {

    public static void main(String[] args) throws Exception {

        CorreoService correoService = new CorreoService();

        correoService.enviarAvisoEmpresa(
            "SWISSCOL - Prueba de correo",
            """
            Hola, Marlon.

            Este es un correo de prueba enviado desde Java.

            La conexión de correo de SWISSCOL está funcionando.
            Este mensaje no corresponde a un pedido real.
            """
        );

        System.out.println(
            "Gmail aceptó el envío. Revisa tu bandeja de entrada."
        );
    }
}