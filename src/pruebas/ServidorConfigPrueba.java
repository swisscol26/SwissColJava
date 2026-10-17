package pruebas;

import config.ServidorConfig;

/** Ejecutar en procesos separados con las variables del escenario esperado. */
public class ServidorConfigPrueba {
    public static void main(String[] args) {
        if (args.length != 3) throw new IllegalArgumentException("puerto origen cookieSegura");
        if (ServidorConfig.puerto() != Integer.parseInt(args[0])) throw new AssertionError("Puerto");
        if (!ServidorConfig.origenes().contains(args[1])) throw new AssertionError("Origen");
        if (ServidorConfig.origenes().contains("https://intruso.example")) throw new AssertionError("Origen ajeno");
        if (ServidorConfig.cookieSegura().contains("Secure") != Boolean.parseBoolean(args[2])) {
            throw new AssertionError("Cookie");
        }
        System.out.println("Configuración de servidor: correcta.");
    }
}
