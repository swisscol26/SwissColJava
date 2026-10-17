package services;

import dao.AccesoDatosException;
import dao.ProductoDAO;
import dto.ProductoResultado;
import middlewares.ProductoValidator;
import modelo.Producto;

import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }

    public List<Producto> listarProductos() {
        return productoDAO.listarProductos();
    }

    public ProductoResultado buscarProducto(int productoId) {

        if (productoId <= 0) {
            return ProductoResultado.error(
                400,
                "El ID debe ser un número entero mayor que cero."
            );
        }

        try {

            Producto producto =
                productoDAO.buscarProductoPorId(productoId);

            if (producto == null) {
                return ProductoResultado.error(
                    404,
                    "Producto no encontrado."
                );
            }

            return ProductoResultado.exitoso(
                200,
                "Producto consultado correctamente.",
                producto
            );

        } catch (AccesoDatosException e) {
            return errorInterno("consultar", e);
        }
    }

    public ProductoResultado registrarProducto(
            Producto producto) {

        String error = ProductoValidator.validar(
            producto,
            true
        );

        if (error != null) {
            return ProductoResultado.error(400, error);
        }

        normalizarProducto(producto);

        try {

            if (productoDAO.buscarProductoPorId(
                    producto.getProductId()) != null) {

                return ProductoResultado.error(
                    409,
                    "Ya existe un producto con ese ID."
                );
            }

            if (!productoDAO.existeCategoria(
                    producto.getCategoryId())) {

                return ProductoResultado.error(
                    400,
                    "La categoría indicada no existe."
                );
            }

            if (!productoDAO.insertarProducto(producto)) {
                return ProductoResultado.error(
                    500,
                    "No fue posible registrar el producto."
                );
            }

            return ProductoResultado.exitoso(
                201,
                "Producto registrado correctamente.",
                producto
            );

        } catch (AccesoDatosException e) {

            if (esConflictoIntegridad(e)) {
                return ProductoResultado.error(
                    409,
                    "El producto o la categoría generan un conflicto."
                );
            }

            return errorInterno("registrar", e);
        }
    }

    public ProductoResultado actualizarProducto(
            int productoId,
            Producto producto) {

        if (productoId <= 0) {
            return ProductoResultado.error(
                400,
                "El ID debe ser un número entero mayor que cero."
            );
        }

        String error = ProductoValidator.validar(
            producto,
            false
        );

        if (error != null) {
            return ProductoResultado.error(400, error);
        }

        producto.setProductId(productoId);
        normalizarProducto(producto);

        try {

            if (productoDAO.buscarProductoPorId(productoId)
                    == null) {

                return ProductoResultado.error(
                    404,
                    "Producto no encontrado."
                );
            }

            if (!productoDAO.existeCategoria(
                    producto.getCategoryId())) {

                return ProductoResultado.error(
                    400,
                    "La categoría indicada no existe."
                );
            }

            if (!productoDAO.actualizarProducto(producto)) {
                return ProductoResultado.error(
                    500,
                    "No fue posible actualizar el producto."
                );
            }

            return ProductoResultado.exitoso(
                200,
                "Producto actualizado correctamente.",
                producto
            );

        } catch (AccesoDatosException e) {

            if (esConflictoIntegridad(e)) {
                return ProductoResultado.error(
                    409,
                    "La actualización genera un conflicto de datos."
                );
            }

            return errorInterno("actualizar", e);
        }
    }

    public ProductoResultado eliminarProducto(
            int productoId) {

        if (productoId <= 0) {
            return ProductoResultado.error(
                400,
                "El ID debe ser un número entero mayor que cero."
            );
        }

        try {

            Producto producto =
                productoDAO.buscarProductoPorId(productoId);

            if (producto == null) {
                return ProductoResultado.error(
                    404,
                    "Producto no encontrado."
                );
            }

            if (!productoDAO.eliminarProducto(productoId)) {
                return ProductoResultado.error(
                    500,
                    "No fue posible eliminar el producto."
                );
            }

            return ProductoResultado.exitoso(
                200,
                "Producto eliminado correctamente.",
                producto
            );

        } catch (AccesoDatosException e) {

            if (esConflictoIntegridad(e)) {
                return ProductoResultado.error(
                    409,
                    "El producto no se puede eliminar porque está relacionado con compras o carritos."
                );
            }

            return errorInterno("eliminar", e);
        }
    }

    private void normalizarProducto(Producto producto) {

    producto.setName(
        producto.getName()
            .trim()
            .replaceAll("\\s+", " ")
    );

    producto.setDescription(
        limpiarTextoOpcional(producto.getDescription())
    );

    producto.setImage(
        limpiarTextoOpcional(producto.getImage())
    );

    producto.setTechnicalDetails(
        limpiarTextoOpcional(producto.getTechnicalDetails())
    );

    producto.setReference(
        limpiarTextoOpcional(producto.getReference())
    );
}

    private String limpiarTextoOpcional(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private boolean esConflictoIntegridad(
            AccesoDatosException e) {

        return "23000".equals(e.getSqlState());
    }

    private ProductoResultado errorInterno(
            String operacion,
            AccesoDatosException e) {

        System.err.println(
            "Error al " + operacion + " producto: "
                + e.getMessage()
        );

        return ProductoResultado.error(
            500,
            "Error interno del servidor."
        );
    }
}
