import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

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