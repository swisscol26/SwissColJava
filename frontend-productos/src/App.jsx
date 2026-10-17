import {
    Navigate,
    Route,
    Routes
} from "react-router-dom";

import AccesoAdministrador from "./components/AccesoAdministrador";

import EditarProductoPage from "./pages/productos/EditarProductoPage";
import NuevoProductoPage from "./pages/productos/NuevoProductoPage";
import ProductoDetallePage from "./pages/productos/ProductoDetallePage";
import ProductosPage from "./pages/productos/ProductosPage";
import PedidosPage from "./pages/pedidos/PedidosPage";
import EstadisticasPage from "./pages/Estadisticas/EstadisticasPage";

function App() {
    return (
        <AccesoAdministrador>
            <Routes>
                <Route
                    path="/"
                    element={
                        <Navigate to="/admin/productos" replace />
                    }
                />

                <Route
                    path="/admin/productos"
                    element={<ProductosPage />}
                />

                <Route
                    path="/admin/productos/nuevo"
                    element={<NuevoProductoPage />}
                />

                <Route
                    path="/admin/productos/:productId/editar"
                    element={<EditarProductoPage />}
                />

                <Route
                    path="/admin/productos/:productId"
                    element={<ProductoDetallePage />}
                />

                <Route
                    path="/admin/estadisticas"
                    element={<EstadisticasPage />}
                />

                <Route
                    path="/admin/pedidos"
                    element={<PedidosPage />}
/>

                <Route
                    path="*"
                    element={
                        <Navigate to="/admin/productos" replace />
                    }
                />
            </Routes>
        </AccesoAdministrador>
    );
}

export default App;
