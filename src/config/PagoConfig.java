package config;

public final class PagoConfig {

    private static final String NEQUI_NUMERO_POR_DEFECTO =
        "300 800 8180";

    private static final String NEQUI_TITULAR_POR_DEFECTO =
        "Marlon Adenis Pulido Morales";

    public static final int PLAZO_HORAS = 24;

    private PagoConfig() {
    }

    public static String getNequiNumero() {
        return leerVariable(
            "SWISSCOL_NEQUI_NUMBER",
            NEQUI_NUMERO_POR_DEFECTO
        );
    }

    public static String getNequiTitular() {
        return leerVariable(
            "SWISSCOL_NEQUI_HOLDER",
            NEQUI_TITULAR_POR_DEFECTO
        );
    }

    public static String mensajeAprobacion(int pedidoId, java.math.BigDecimal total) {
        java.text.DecimalFormat formato = new java.text.DecimalFormat(
            "#,##0.##", java.text.DecimalFormatSymbols.getInstance(
                java.util.Locale.forLanguageTag("es-CO")));
        return "Pedido #" + pedidoId + " aprobado para pago. Total a consignar: $"
            + formato.format(total) + " COP. Consigna por Nequi al número "
            + getNequiNumero() + ", titular " + getNequiTitular()
            + ". Tienes " + PLAZO_HORAS
            + " horas para realizar el pago y reportar la referencia.";
    }

    private static String leerVariable(
            String nombre,
            String valorPorDefecto) {

        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            return valorPorDefecto;
        }

        return valor.trim();
    }
}
