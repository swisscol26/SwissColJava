import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import EditarProductoPage from "./pages/productos/EditarProductoPage";
import NuevoProductoPage from "./pages/productos/NuevoProductoPage";
import ProductoDetallePage from "./pages/productos/ProductoDetallePage";
import ProductosPage from "./pages/productos/ProductosPage";

/**
 * Configuración de rutas del módulo administrativo de productos.
 */
function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <Navigate
            to="/admin/productos"
            replace
          />
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
        path="*"
        element={
          <Navigate
            to="/admin/productos"
            replace
          />
        }
      />
    </Routes>
  );
}

export default App;