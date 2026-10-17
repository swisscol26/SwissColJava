package middlewares;

import modelo.Producto;

public final class ProductoValidator {

    private static final int MAXIMO_NOMBRE = 45;
    private static final int MAXIMO_DESCRIPCION = 1000;
    private static final int MAXIMO_IMAGEN = 45;
    private static final int MAXIMO_CARACTERISTICAS = 3000;
    private static final int MAXIMO_REFERENCIA = 50;

    private ProductoValidator() {
        // Evita crear objetos de esta clase.
    }

    public static String validar(
            Producto producto,
            boolean validarId) {

        if (producto == null) {
            return "El cuerpo de la solicitud es obligatorio.";
        }

        if (validarId && producto.getProductId() <= 0) {
            return "El ID debe ser un número entero mayor que cero.";
        }

        if (producto.getName() == null
                || producto.getName().isBlank()) {
            return "El nombre es obligatorio.";
        }

        int longitudNombre = producto.getName().trim().length();

        if (longitudNombre < 2
                || longitudNombre > MAXIMO_NOMBRE) {
            return "El nombre debe tener entre 2 y 45 caracteres.";
        }

        if (producto.getDescription() != null
                && producto.getDescription().trim().length()
                    > MAXIMO_DESCRIPCION) {
            return "La descripción admite máximo 1000 caracteres.";
        }

        if (!Double.isFinite(producto.getPrice())
                || producto.getPrice() <= 0) {
            return "El precio debe ser mayor que cero.";
        }

        Double precioOferta = producto.getOfferPrice();

        if (precioOferta != null) {

            if (!Double.isFinite(precioOferta)
                    || precioOferta <= 0) {
                return "El precio de oferta debe ser mayor que cero.";
            }

            if (precioOferta >= producto.getPrice()) {
                return "El precio de oferta debe ser menor que el precio normal.";
            }

            if (java.math.BigDecimal.valueOf(precioOferta)
                    .stripTrailingZeros()
                    .scale() > 2) {
                return "El precio de oferta admite máximo dos decimales.";
            }
        }

        if (producto.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }

        if (producto.getImage() != null
                && producto.getImage().trim().length()
                    > MAXIMO_IMAGEN) {
            return "La imagen admite máximo 45 caracteres.";
        }

        if (producto.getCategoryId() <= 0) {
            return "La categoría es obligatoria.";
        }

        if (producto.getTechnicalDetails() != null
        && producto.getTechnicalDetails().trim().length()
            > MAXIMO_CARACTERISTICAS) {
    return "Las características técnicas admiten máximo 3000 caracteres.";
        }

        if (producto.getReference() != null
                && producto.getReference().trim().length()
                    > MAXIMO_REFERENCIA) {
            return "La referencia admite máximo 50 caracteres.";
        }

        return null;
    }
}
