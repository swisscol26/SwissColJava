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



   // INSERTAR PRODUCTO
public boolean insertarProducto(Producto producto) {

    String sql = """
        INSERT INTO product
        (product_id, name, description, price, stock, image, category_id)
        VALUES (?, ?, ?, ?, ?, ?, ?)
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

        int filasAfectadas = ps.executeUpdate();

        return filasAfectadas > 0;

    } catch (SQLException e) {

        System.out.println(
            "Error al registrar el producto: " + e.getMessage()
        );

        return false;
    }
}

 // LISTAR PRODUCTOS
public List<Producto> listarProductos() {

    List<Producto> productos = new ArrayList<>();

    String sql = "SELECT * FROM product";

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement ps = conexion.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()
    ) {

        while (rs.next()) {

            Producto producto = new Producto();

            producto.setProductId(rs.getInt("product_id"));
            producto.setName(rs.getString("name"));
            producto.setDescription(rs.getString("description"));
            producto.setPrice(rs.getDouble("price"));
            producto.setStock(rs.getInt("stock"));
            producto.setImage(rs.getString("image"));
            producto.setCategoryId(rs.getInt("category_id"));

            productos.add(producto);
        }

    } catch (SQLException e) {

        System.out.println(
            "Error al consultar los productos: " + e.getMessage()
        );
    }

    return productos;

}

// BUSCAR PRODUCTO POR ID
public Producto buscarProductoPorId(int id) {

    String sql = "SELECT * FROM product WHERE product_id = ?";

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement ps = conexion.prepareStatement(sql)
    ) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                Producto producto = new Producto();

                producto.setProductId(rs.getInt("product_id"));
                producto.setName(rs.getString("name"));
                producto.setDescription(rs.getString("description"));
                producto.setPrice(rs.getDouble("price"));
                producto.setStock(rs.getInt("stock"));
                producto.setImage(rs.getString("image"));
                producto.setCategoryId(rs.getInt("category_id"));

                return producto;
            }
        }

    } catch (SQLException e) {

        System.out.println(
            "Error al buscar el producto: " + e.getMessage()
        );
    }

    return null;
}

    // ACTUALIZAR PRODUCTO
public boolean actualizarProducto(Producto producto) {

    String sql = """
        UPDATE product
        SET name = ?,
            description = ?,
            price = ?,
            stock = ?,
            image = ?,
            category_id = ?
        WHERE product_id = ?
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
        ps.setInt(7, producto.getProductId());

        int filasAfectadas = ps.executeUpdate();

        return filasAfectadas > 0;

    } catch (SQLException e) {

        System.out.println(
            "Error al actualizar el producto: " + e.getMessage()
        );

        return false;
    }
}

   // ELIMINAR PRODUCTO
public boolean eliminarProducto(int id) {

    String sql = "DELETE FROM product WHERE product_id = ?";

    try (
        Connection conexion = Conexion.conectar();
        PreparedStatement ps = conexion.prepareStatement(sql)
    ) {

        ps.setInt(1, id);

        int filasAfectadas = ps.executeUpdate();

        return filasAfectadas > 0;

    } catch (SQLException e) {

        System.out.println(
            "Error al eliminar el producto: " + e.getMessage()
        );

        return false;
    }
}

}