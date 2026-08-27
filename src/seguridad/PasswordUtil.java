package seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_SAL = 16;
    private static final int TAMANO_HASH_BITS = 256;

    private static final SecureRandom RANDOM =
        new SecureRandom();

    private PasswordUtil() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Genera un hash con una sal aleatoria.
     */
    public static String crearHash(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                "La contraseña es obligatoria."
            );
        }

        byte[] sal = new byte[TAMANO_SAL];
        RANDOM.nextBytes(sal);

        byte[] hash = generarHash(
            password.toCharArray(),
            sal,
            ITERACIONES,
            TAMANO_HASH_BITS
        );

        return ITERACIONES
            + ":"
            + Base64.getEncoder().encodeToString(sal)
            + ":"
            + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Compara una contraseña recibida con el hash almacenado.
     */
    public static boolean verificar(
            String password,
            String hashGuardado) {

        if (password == null || hashGuardado == null) {
            return false;
        }

        try {

            String[] partes = hashGuardado.split(":", -1);

            if (partes.length != 3) {
                return false;
            }

            int iteraciones =
                Integer.parseInt(partes[0]);

            byte[] sal =
                Base64.getDecoder().decode(partes[1]);

            byte[] hashEsperado =
                Base64.getDecoder().decode(partes[2]);

            if (iteraciones <= 0
                    || sal.length == 0
                    || hashEsperado.length == 0) {
                return false;
            }

            byte[] hashCalculado = generarHash(
                password.toCharArray(),
                sal,
                iteraciones,
                hashEsperado.length * 8
            );

            return MessageDigest.isEqual(
                hashEsperado,
                hashCalculado
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] generarHash(
            char[] password,
            byte[] sal,
            int iteraciones,
            int tamanoBits) {

        PBEKeySpec especificacion = new PBEKeySpec(
            password,
            sal,
            iteraciones,
            tamanoBits
        );

        try {

            SecretKeyFactory fabrica =
                SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
                );

            return fabrica
                .generateSecret(especificacion)
                .getEncoded();

        } catch (GeneralSecurityException e) {

            throw new IllegalStateException(
                "No fue posible proteger la contraseña.",
                e
            );

        } finally {
            especificacion.clearPassword();
        }
    }
}