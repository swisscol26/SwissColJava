package pruebas;

import dao.ProductoDAO;
import dto.ProductoResultado;
import modelo.Producto;
import services.ProductoService;

public class ProductoServicePrueba {

    private static int comprobaciones;

    public static void main(String[] args) {

        ProductoService service = new ProductoService();
        ProductoDAO productoDAO = new ProductoDAO();

        int productoId = obtenerIdDisponible(productoDAO);
        boolean productoCreado = false;

        try {

            Producto invalido = producto(
                productoId,
                "Producto inválido",
                -1,
                1
            );

            comprobarCodigo(
                service.registrarProducto(invalido),
                400,
                false
            );

            Producto nuevo = producto(
                productoId,
                "Producto integración",
                125000,
                1
            );

            ProductoResultado registro =
                service.registrarProducto(nuevo);

            comprobarCodigo(registro, 201, true);
            productoCreado = true;

            comprobarCodigo(
                service.registrarProducto(nuevo),
                409,
                false
            );

            ProductoResultado consulta =
                service.buscarProducto(productoId);

            comprobarCodigo(consulta, 200, true);
            comprobar(
                consulta.getProducto() != null
                    && consulta.getProducto().getProductId()
                        == productoId,
                "La consulta debe devolver el producto creado"
            );

            Producto actualizado = producto(
                productoId,
                "Producto actualizado",
                175000,
                2
            );

            ProductoResultado actualizacion =
                service.actualizarProducto(
                    productoId,
                    actualizado
                );

            comprobarCodigo(actualizacion, 200, true);
            comprobar(
                "Producto actualizado".equals(
                    actualizacion.getProducto().getName()
                ),
                "La actualización debe devolver el nuevo nombre"
            );

            ProductoResultado eliminacion =
                service.eliminarProducto(productoId);

            comprobarCodigo(eliminacion, 200, true);
            productoCreado = false;

            comprobarCodigo(
                service.buscarProducto(productoId),
                404,
                false
            );

            comprobarCodigo(
                service.eliminarProducto(productoId),
                404,
                false
            );

            System.out.println(
                "ProductoService: " + comprobaciones
                    + " comprobaciones aprobadas."
            );

        } finally {

            if (productoCreado
                    && productoDAO.buscarProductoPorId(productoId)
                        != null) {

                productoDAO.eliminarProducto(productoId);
            }
        }
    }

    private static int obtenerIdDisponible(
            ProductoDAO productoDAO) {

        int candidato = 900_000_000
            + (int) ((System.currentTimeMillis() / 1000)
                % 90_000_000);

        while (productoDAO.buscarProductoPorId(candidato)
                != null) {
            candidato++;
        }

        return candidato;
    }

    private static Producto producto(
            int productoId,
            String nombre,
            double precio,
            int categoriaId) {

        return new Producto(
            productoId,
            nombre,
            "Producto creado por la prueba de integración",
            precio,
            12,
            "integracion.jpg",
            categoriaId
        );
    }

    private static void comprobarCodigo(
            ProductoResultado resultado,
            int codigoEsperado,
            boolean exitoEsperado) {

        comprobar(
            resultado.getCodigoHttp() == codigoEsperado,
            "Se esperaba código " + codigoEsperado
                + " y se obtuvo "
                + resultado.getCodigoHttp()
        );

        comprobar(
            resultado.isExito() == exitoEsperado,
            "El estado de éxito no coincide"
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
