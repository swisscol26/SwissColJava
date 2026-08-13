import axios from "axios";

// Dirección de la API Java del módulo de productos.
const API_URL = "http://localhost:8080/api/productos";

const productoService = {

  // Obtiene todos los productos.
  listarProductos() {
    return axios.get(API_URL);
  },

  // Busca un producto por su ID.
  buscarProductoPorId(productId) {
    return axios.get(`${API_URL}/${productId}`);
  },

  // Registra un producto nuevo.
  registrarProducto(producto) {
    return axios.post(API_URL, producto);
  },

  // Actualiza un producto existente.
  actualizarProducto(productId, producto) {
    return axios.put(`${API_URL}/${productId}`, producto);
  },

  // Elimina un producto.
  eliminarProducto(productId) {
    return axios.delete(`${API_URL}/${productId}`);
  },
};

export default productoService;