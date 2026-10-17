package config;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServidorConfig {
    private ServidorConfig() {}

    public static String entorno(String clave, String defecto) {
        String valor = System.getenv(clave);
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    public static int puerto() {
        int puerto = Integer.parseInt(entorno("PORT", "8080"));
        if (puerto < 1 || puerto > 65535) throw new IllegalArgumentException("PORT inválido.");
        return puerto;
    }

    public static Set<String> origenes() {
        String valor = entorno("SWISSCOL_ALLOWED_ORIGINS",
            "http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173,http://127.0.0.1:5173");
        return Arrays.stream(valor.split(",")).map(String::trim).map(origen -> {
            URI uri = URI.create(origen);
            if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null || !uri.getRawPath().isEmpty()) {
                throw new IllegalArgumentException("SWISSCOL_ALLOWED_ORIGINS requiere orígenes exactos sin rutas.");
            }
            return origen;
        }).collect(Collectors.toUnmodifiableSet());
    }

    public static String cookieSegura() {
        return Boolean.parseBoolean(entorno("SWISSCOL_COOKIE_SECURE", "false")) ? "; Secure" : "";
    }
}
