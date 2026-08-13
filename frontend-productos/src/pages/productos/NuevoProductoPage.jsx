import { useState } from "react";
import { useNavigate } from "react-router-dom";

import ProductoForm from "../../components/productos/ProductoForm";
import productoService from "../../services/productoService";

/**
 * Pantalla para registrar un producto nuevo.
 */
function NuevoProductoPage() {
  const navigate = useNavigate();

  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");

  const registrarProducto = async (producto) => {
    try {
      setGuardando(true);
      setError("");

      await productoService.registrarProducto(producto);

      navigate("/admin/productos");
    } catch (err) {
      console.error("Error al registrar producto:", err);

      setError(
        err.response?.data?.mensaje ||
          "No fue posible registrar el producto."
      );
    } finally {
      setGuardando(false);
    }
  };

  return (
    <main className="container py-5">
      <div className="mb-4">
        <h1 className="mb-1">Registrar producto</h1>
        <p className="text-secondary mb-0">
          Complete la información del nuevo producto.
        </p>
      </div>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      <div className="card shadow-sm">
        <div className="card-body p-4">
          <ProductoForm
            onGuardar={registrarProducto}
            onCancelar={() =>
              navigate("/admin/productos")
            }
            guardando={guardando}
          />
        </div>
      </div>
    </main>
  );
}

export default NuevoProductoPage;