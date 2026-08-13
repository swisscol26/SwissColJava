import { useEffect, useState } from "react";
import {
  useNavigate,
  useParams,
} from "react-router-dom";

import ProductoForm from "../../components/productos/ProductoForm";
import productoService from "../../services/productoService";

/**
 * Pantalla para consultar y actualizar un producto.
 */
function EditarProductoPage() {
  const navigate = useNavigate();
  const { productId } = useParams();

  const [producto, setProducto] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");

  /**
   * Carga el producto seleccionado al abrir la pantalla.
   */
  useEffect(() => {
    const cargarProducto = async () => {
      try {
        setCargando(true);
        setError("");

        const respuesta =
          await productoService.buscarProductoPorId(
            productId
          );

        setProducto(respuesta.data);
      } catch (err) {
        console.error(
          "Error al consultar producto:",
          err
        );

        setError(
          err.response?.data?.mensaje ||
            "No fue posible consultar el producto."
        );
      } finally {
        setCargando(false);
      }
    };

    cargarProducto();
  }, [productId]);

  /**
   * Envía los cambios a la API Java.
   */
  const actualizarProducto = async (
    productoActualizado
  ) => {
    try {
      setGuardando(true);
      setError("");

      await productoService.actualizarProducto(
        productId,
        {
          ...productoActualizado,
          productId: Number(productId),
        }
      );

      navigate("/admin/productos");
    } catch (err) {
      console.error(
        "Error al actualizar producto:",
        err
      );

      setError(
        err.response?.data?.mensaje ||
          "No fue posible actualizar el producto."
      );
    } finally {
      setGuardando(false);
    }
  };

  return (
    <main className="container py-5">
      <div className="mb-4">
        <h1 className="mb-1">Editar producto</h1>
        <p className="text-secondary mb-0">
          Modifique los datos del producto seleccionado.
        </p>
      </div>

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}

      {cargando && (
        <div className="text-center py-5">
          <div
            className="spinner-border text-danger"
            role="status"
          >
            <span className="visually-hidden">
              Cargando producto...
            </span>
          </div>
          <p className="mt-3">
            Cargando producto...
          </p>
        </div>
      )}

      {!cargando && producto && (
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <ProductoForm
              productoInicial={producto}
              onGuardar={actualizarProducto}
              onCancelar={() =>
                navigate("/admin/productos")
              }
              guardando={guardando}
              esEdicion
            />
          </div>
        </div>
      )}

      {!cargando && !producto && (
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() =>
            navigate("/admin/productos")
          }
        >
          Volver al listado
        </button>
      )}
    </main>
  );
}

export default EditarProductoPage;