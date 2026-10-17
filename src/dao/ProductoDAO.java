package dao;

import conexion.Conexion;
import modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean insertarProducto(Producto producto) {

        String sql = """
        INSERT INTO product
        (product_id, name, description, price, stock,
        image, category_id, technical_details, reference, offer_price)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, producto.getProductId());
            ps.setString(2, producto.getName());
            ps.setString(3, producto.getDescription());
            ps.setDouble(4, producto.getPrice());
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getImage());
            ps.setInt(7, producto.getCategoryId());
            ps.setString(8, producto.getTechnicalDetails());
            ps.setString(9, producto.getReference());

            if (producto.getOfferPrice() == null) {
                ps.setNull(10, java.sql.Types.DECIMAL);
            } else {
                ps.setBigDecimal(
                    10,
                    java.math.BigDecimal.valueOf(producto.getOfferPrice())
                );
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw crearExcepcion(
                "No fue posible registrar el producto.",
                e
            );
        }
    }

    public List<Producto> listarProductos() {

        List<Producto> productos = new ArrayList<>();

        String sql = """
    SELECT product_id, name, description, price,
            stock,
            (stock - reserved_stock) AS available_stock,
            image, category_id, technical_details,
            reference, offer_price
        FROM product
        ORDER BY product_id
        """;
        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

            return productos;

        } catch (SQLException e) {
            throw crearExcepcion(
                "No fue posible consultar los productos.",
                e
            );
        }
    }

    public Producto buscarProductoPorId(int id) {

        String sql = """
        SELECT product_id, name, description, price,
            stock,
            (stock - reserved_stock) AS available_stock,
            image, category_id, technical_details,
            reference, offer_price
        FROM product
        WHERE product_id = ?
        """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearProducto(rs) : null;
            }

        } catch (SQLException e) {
            throw crearExcepcion(
                "No fue posible buscar el producto.",
                e
            );
        }
    }

    public boolean actualizarProducto(Producto producto) {

        String sql = """
        UPDATE product
        SET name = ?,
            description = ?,
            price = ?,
            stock = ?,
            image = ?,
            category_id = ?,
            technical_details = ?,
            reference = ?,
            offer_price = ?
        WHERE product_id = ?
            AND reserved_stock <= ?
        """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setString(1, producto.getName());
            ps.setString(2, producto.getDescription());
            ps.setDouble(3, producto.getPrice());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImage());
            ps.setInt(6, producto.getCategoryId());
            ps.setString(7, producto.getTechnicalDetails());
            ps.setString(8, producto.getReference());


            if (producto.getOfferPrice() == null) {
                ps.setNull(9, java.sql.Types.DECIMAL);
            } else {
                ps.setBigDecimal(
                    9,
                    java.math.BigDecimal.valueOf(producto.getOfferPrice())
                );
            }

            ps.setInt(10, producto.getProductId());
            ps.setInt(11, producto.getStock());
                        return ps.executeUpdate() > 0;


            } catch (SQLException e) {
                throw crearExcepcion(
                    "No fue posible actualizar el producto.",
                    e
                );
            }
        }

    public boolean eliminarProducto(int id) {

        String sql = "DELETE FROM product WHERE product_id = ?";

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw crearExcepcion(
                "No fue posible eliminar el producto.",
                e
            );
        }
    }

    public boolean existeCategoria(int categoriaId) {

        String sql = """
            SELECT 1
            FROM category
            WHERE category_id = ?
            """;

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw crearExcepcion(
                "No fue posible validar la categoría.",
                e
            );
        }
    }

    private Producto mapearProducto(ResultSet rs)
            throws SQLException {

        Producto producto = new Producto();

        producto.setProductId(rs.getInt("product_id"));
        producto.setName(rs.getString("name"));
        producto.setDescription(rs.getString("description"));
        producto.setPrice(rs.getDouble("price"));
        producto.setStock(rs.getInt("stock"));
        producto.setAvailableStock(rs.getInt("available_stock"));
        producto.setImage(rs.getString("image"));
        producto.setCategoryId(rs.getInt("category_id"));
        producto.setTechnicalDetails(
            rs.getString("technical_details")
        );
        producto.setReference(rs.getString("reference"));

        java.math.BigDecimal precioOferta =
            rs.getBigDecimal("offer_price");

        producto.setOfferPrice(
            precioOferta == null ? null : precioOferta.doubleValue()
        );

        return producto;
    }

    private AccesoDatosException crearExcepcion(
            String mensaje,
            SQLException causa) {

        return new AccesoDatosException(
            mensaje,
            causa,
            causa.getSQLState(),
            causa.getErrorCode()
        );
    }
}