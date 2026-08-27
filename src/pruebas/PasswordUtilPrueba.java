package pruebas;

import seguridad.PasswordUtil;

public class PasswordUtilPrueba {

    public static void main(String[] args) {

        String password = "ClaveSegura123";

        String hash =
            PasswordUtil.crearHash(password);

        System.out.println("Hash generado:");
        System.out.println(hash);

        System.out.println(
            "Contraseña correcta: "
                + PasswordUtil.verificar(
                    "ClaveSegura123",
                    hash
                )
        );

        System.out.println(
            "Contraseña incorrecta: "
                + PasswordUtil.verificar(
                    "ClaveIncorrecta",
                    hash
                )
        );
    }
}