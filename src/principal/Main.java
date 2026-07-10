package principal;

import dao.ProductoDAO;
import modelo.Producto;

public class Main {

    public static void main(String[] args) {

        Producto producto = new Producto();

        producto.setProductId(1);

        producto.setName("Victorinox Huntsman");
        producto.setDescription("Navaja multifunción de 15 usos");
        producto.setPrice(150000);
        producto.setStock(20);
        producto.setImage("huntsman.png");
        producto.setCategoryId(1);

        ProductoDAO dao = new ProductoDAO();

        // INSERTAR
        dao.insertarProducto(producto);

        // ACTUALIZAR
        producto.setName("Victorinox Deluxe");
        producto.setPrice(180000);

        dao.actualizarProducto(producto);

        // LISTAR
        System.out.println("\n===== LISTA DE PRODUCTOS =====");
        dao.listarProductos();

        // ELIMINAR
        dao.eliminarProducto(producto.getProductId());

        // LISTAR NUEVAMENTE
        System.out.println("\n===== PRODUCTOS DESPUÉS DE ELIMINAR =====");
        dao.listarProductos();

    }

}