import { API_BASE } from "../config";
import axios from "axios";

const apiProductos = axios.create({
    baseURL:
        `${API_BASE}/productos`,
    withCredentials: true
});

const productoService = {

    listarProductos() {
        return apiProductos.get("");
    },

    buscarProductoPorId(productId) {
        return apiProductos.get(`/${productId}`);
    },

    registrarProducto(producto) {
        return apiProductos.post("", producto);
    },

    actualizarProducto(productId, producto) {
        return apiProductos.put(`/${productId}`, producto);
    },

    eliminarProducto(productId) {
        return apiProductos.delete(`/${productId}`);
    }
};

export default productoService;