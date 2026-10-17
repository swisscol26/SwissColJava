package pruebas;

import middlewares.ProductoValidator;
import modelo.Producto;

public class ProductoValidatorPrueba {

    private static int comprobaciones;

    public static void main(String[] args) {

        Producto valido = productoValido(900001);

        comprobar(
            ProductoValidator.validar(valido, true) == null,
            "Acepta un producto válido"
        );

        Producto sinId = productoValido(0);
        comprobarContiene(
            ProductoValidator.validar(sinId, true),
            "ID"
        );

        Producto sinNombre = productoValido(900002);
        sinNombre.setName("  ");
        comprobarContiene(
            ProductoValidator.validar(sinNombre, true),
            "nombre"
        );

        Producto precioCero = productoValido(900003);
        precioCero.setPrice(0);
        comprobarContiene(
            ProductoValidator.validar(precioCero, true),
            "precio"
        );

        Producto stockNegativo = productoValido(900004);
        stockNegativo.setStock(-1);
        comprobarContiene(
            ProductoValidator.validar(stockNegativo, true),
            "stock"
        );

        Producto categoriaInvalida = productoValido(900005);
        categoriaInvalida.setCategoryId(0);
        comprobarContiene(
            ProductoValidator.validar(categoriaInvalida, true),
            "categoría"
        );

        Producto nombreLargo = productoValido(900006);
        nombreLargo.setName("N".repeat(46));
        comprobarContiene(
            ProductoValidator.validar(nombreLargo, true),
            "45"
        );

        Producto descripcionLarga = productoValido(900007);
        descripcionLarga.setDescription("D".repeat(1001));
        comprobarContiene(
            ProductoValidator.validar(descripcionLarga, true),
            "1000"
        );

        Producto imagenLarga = productoValido(900008);
        imagenLarga.setImage("I".repeat(46));
        comprobarContiene(
            ProductoValidator.validar(imagenLarga, true),
            "45"
        );

        Producto actualizacionSinId = productoValido(0);
        comprobar(
            ProductoValidator.validar(
                actualizacionSinId,
                false
            ) == null,
            "La actualización toma el ID desde la ruta"
        );

        System.out.println(
            "ProductoValidator: " + comprobaciones
                + " comprobaciones aprobadas."
        );
    }

    private static Producto productoValido(int productoId) {
        return new Producto(
            productoId,
            "Producto de prueba",
            "Descripción controlada",
            150000,
            10,
            "producto-prueba.jpg",
            1
        );
    }

    private static void comprobarContiene(
            String valor,
            String esperado) {

        comprobar(
            valor != null
                && valor.toLowerCase().contains(
                    esperado.toLowerCase()
                ),
            "El mensaje debe contener: " + esperado
        );
    }

    private static void comprobar(
            boolean condicion,
            String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }

        comprobaciones++;
    }
}
