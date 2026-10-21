import { API_BASE } from "../config";
import axios from "axios";

const apiPedidos = axios.create({
    baseURL:
        `${API_BASE}/pedidos`,
    withCredentials: true
});

const pedidoService = {

    cancelarSolicitud(pedidoId) {
        return apiPedidos.post("/cancelar", { pedidoId });
    },
    listarPendientes() {
        return apiPedidos.get("/pendientes");
    },

    confirmarPedido(pedidoId) {
        return apiPedidos.post("/confirmar", {
            pedidoId
        });
    },

    consultarEstadisticas() {
        return apiPedidos.get("/estadisticas");
    },

    actualizarCostoEnvio(pedidoId, costoEnvio) {
        return apiPedidos.post("/costo-envio", {
            pedidoId,
            costoEnvio
        });
    },

    aprobarParaPago(pedidoId) {
        return apiPedidos.post("/aprobar", {
            pedidoId
        });
    }
};

export default pedidoService;