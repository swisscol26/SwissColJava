package pruebas;

public class CorreoConfiguracionPrueba {

    public static void main(String[] args) {

        String usuario = System.getenv("SWISSCOL_MAIL_USER");
        String password = System.getenv("SWISSCOL_MAIL_PASSWORD");

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalStateException(
                "No se encontró SWISSCOL_MAIL_USER."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "No se encontró SWISSCOL_MAIL_PASSWORD."
            );
        }

        System.out.println(
            "Las dos variables de correo están disponibles."
        );
    }
}