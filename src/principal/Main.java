package principal;

import java.util.List;
import java.util.Scanner;


import dao.ProductoDAO;
import modelo.Producto;

public class Main {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);
        ProductoDAO productoDAO = new ProductoDAO();

        int opcion;

        do {

            System.out.println("==============================");
            System.out.println("          SWISSCOL");
            System.out.println("    Gestión de Productos");
            System.out.println("==============================");
            System.out.println();
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Buscar producto");
            System.out.println("4. Actualizar producto");
            System.out.println("5. Eliminar producto");
            System.out.println("6. Salir");
            System.out.println();
            System.out.print("Seleccione una opción: ");

            opcion = teclado.nextInt();

            switch (opcion) {

                case 1: {

                    teclado.nextLine();

                    System.out.print("ID del producto: ");
                    int productId = teclado.nextInt();
                    teclado.nextLine();

                    System.out.print("Nombre: ");
                    String name = teclado.nextLine();

                    System.out.print("Descripción: ");
                    String description = teclado.nextLine();

                    System.out.print("Precio: ");
                    double price = teclado.nextDouble();

                    System.out.print("Stock: ");
                    int stock = teclado.nextInt();
                    teclado.nextLine();

                    System.out.print("Nombre o ruta de la imagen: ");
                    String image = teclado.nextLine();

                    System.out.print("ID de la categoría: ");
                    int categoryId = teclado.nextInt();

                    Producto producto = new Producto(
                        productId,
                        name,
                        description,
                        price,
                        stock,
                        image,
                        categoryId
                    );

                    boolean registrado =
                        productoDAO.insertarProducto(producto);

                    if (registrado) {
                        System.out.println(
                            "Producto registrado correctamente."
                        );
                    } else {
                        System.out.println(
                            "No fue posible registrar el producto."
                        );
                    }

                    break;
                }

                case 2: {

                    List<Producto> productos =
                        productoDAO.listarProductos();

                    if (productos.isEmpty()) {

                        System.out.println(
                            "No hay productos registrados."
                        );

                    } else {

                        System.out.println();
                        System.out.println(
                            "===== LISTA DE PRODUCTOS ====="
                        );

                        for (Producto producto : productos) {

                            System.out.println(
                                "-----------------------------"
                            );
                            System.out.println(
                                "ID: " + producto.getProductId()
                            );
                            System.out.println(
                                "Nombre: " + producto.getName()
                            );
                            System.out.println(
                                "Descripción: "
                                    + producto.getDescription()
                            );
                            System.out.println(
                                "Precio: $" + producto.getPrice()
                            );
                            System.out.println(
                                "Stock: " + producto.getStock()
                            );
                            System.out.println(
                                "Imagen: " + producto.getImage()
                            );
                            System.out.println(
                                "Categoría: "
                                    + producto.getCategoryId()
                            );
                        }
                    }

                    break;
                }

                case 3: {
                    

                    System.out.print("Ingrese el ID del producto: ");
                    int idBuscado = teclado.nextInt();

                    Producto productoEncontrado =
                        productoDAO.buscarProductoPorId(idBuscado);

                    if (productoEncontrado == null) {

                        System.out.println(
                            "No se encontró ningún producto con el ID "
                                + idBuscado + "."
                        );

                    } else {

                        System.out.println();
                        System.out.println("===== PRODUCTO ENCONTRADO =====");
                        System.out.println(
                            "ID: " + productoEncontrado.getProductId()
                        );
                        System.out.println(
                            "Nombre: " + productoEncontrado.getName()
                        );
                        System.out.println(
                            "Descripción: "
                                + productoEncontrado.getDescription()
                        );
                        System.out.println(
                            "Precio: $" + productoEncontrado.getPrice()
                        );
                        System.out.println(
                            "Stock: " + productoEncontrado.getStock()
                        );
                        System.out.println(
                            "Imagen: " + productoEncontrado.getImage()
                        );
                        System.out.println(
                            "Categoría: "
                                + productoEncontrado.getCategoryId()
                        );
                    }

                    break;
                }
                                

                case 4: {

                    System.out.print("Ingrese el ID del producto a actualizar: ");
                    int idActualizar = teclado.nextInt();
                    teclado.nextLine();

                    Producto productoExistente =
                        productoDAO.buscarProductoPorId(idActualizar);

                    if (productoExistente == null) {

                        System.out.println(
                            "No existe un producto con el ID " + idActualizar + "."
                        );

                    } else {

                        System.out.println(
                            "Producto encontrado: " + productoExistente.getName()
                        );

                        System.out.print("Nuevo nombre: ");
                        String nuevoNombre = teclado.nextLine();

                        System.out.print("Nueva descripción: ");
                        String nuevaDescripcion = teclado.nextLine();

                        System.out.print("Nuevo precio: ");
                        double nuevoPrecio = teclado.nextDouble();

                        System.out.print("Nuevo stock: ");
                        int nuevoStock = teclado.nextInt();
                        teclado.nextLine();

                        System.out.print("Nueva imagen: ");
                        String nuevaImagen = teclado.nextLine();

                        System.out.print("Nuevo ID de categoría: ");
                        int nuevaCategoria = teclado.nextInt();

                        Producto productoActualizado = new Producto(
                            idActualizar,
                            nuevoNombre,
                            nuevaDescripcion,
                            nuevoPrecio,
                            nuevoStock,
                            nuevaImagen,
                            nuevaCategoria
                        );

                        boolean actualizado =
                            productoDAO.actualizarProducto(productoActualizado);

                        if (actualizado) {
                            System.out.println(
                                "Producto actualizado correctamente."
                            );
                        } else {
                            System.out.println(
                                "No fue posible actualizar el producto."
                            );
                        }
                    }

                    break;
                }

                case 5: {

                    System.out.print("Ingrese el ID del producto a eliminar: ");
                    int idEliminar ;
                    

                    if (teclado.hasNextInt()) {

                        idEliminar = teclado.nextInt();

                    } else {

                        System.out.println(
                            "El ID debe ser un número entero válido."
                        );

                        teclado.nextLine();
                        break;
                    }

                    Producto productoEliminar =
                        productoDAO.buscarProductoPorId(idEliminar);

                    if (productoEliminar == null) {

                        System.out.println(
                            "No existe un producto con el ID " + idEliminar + "."
                        );

                    } else {

                        System.out.println(
                            "Producto encontrado: " + productoEliminar.getName()
                        );

                        System.out.print(
                            "¿Está seguro de eliminarlo? (1 = Sí, 2 = No): "
                        );

                        int confirmacion = teclado.nextInt();

                        if (confirmacion == 1) {

                            boolean eliminado =
                                productoDAO.eliminarProducto(idEliminar);

                            if (eliminado) {
                                System.out.println(
                                    "Producto eliminado correctamente."
                                );
                            } else {
                                System.out.println(
                                    "No fue posible eliminar el producto."
                                );
                            }

                        } else {

                            System.out.println("Eliminación cancelada.");
                        }
                    }

                    break;
                }
                case 6: {
                    System.out.println("Programa finalizado.");
                    break;
                }

                default: {
                    System.out.println("Opción inválida.");
                }
            }

            System.out.println();

        } while (opcion != 6);

        teclado.close();
    }
}