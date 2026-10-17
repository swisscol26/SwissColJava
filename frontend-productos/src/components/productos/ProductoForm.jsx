import { useState } from "react";

const valoresVacios = {
  productId: "",
  name: "",
  reference: "",
  description: "",
  technicalDetails: "",
  price: "",
  offerPrice: "",
  stock: "",
  image: "",
  categoryId: "",
};

/**
 * Formulario reutilizable para registrar y editar productos.
 */
function ProductoForm({
  productoInicial = valoresVacios,
  onGuardar,
  onCancelar,
  guardando = false,
  esEdicion = false,
}) {
  const [formulario, setFormulario] = useState({
    productId: productoInicial.productId ?? "",
    name: productoInicial.name ?? "",
    reference: productoInicial.reference ?? "",
    description: productoInicial.description ?? "",
    technicalDetails: productoInicial.technicalDetails ?? "",
    price: productoInicial.price ?? "",
    offerPrice: productoInicial.offerPrice ?? "",
    stock: productoInicial.stock ?? "",
    image: productoInicial.image ?? "",
    categoryId: productoInicial.categoryId ?? "",
  });

  const [errores, setErrores] = useState({});

  const manejarCambio = (evento) => {
    const { name, value } = evento.target;

    setFormulario((valoresAnteriores) => ({
      ...valoresAnteriores,
      [name]: value,
    }));

    setErrores((erroresAnteriores) => ({
      ...erroresAnteriores,
      [name]: "",
    }));
  };

  const validarFormulario = () => {
    const nuevosErrores = {};

    if (
      !Number.isInteger(Number(formulario.productId)) ||
      Number(formulario.productId) <= 0
    ) {
      nuevosErrores.productId =
        "El ID debe ser un número entero mayor que cero.";
    }

    if (!formulario.name.trim()) {
      nuevosErrores.name = "El nombre es obligatorio.";
    } else if (formulario.name.trim().length > 45) {
      nuevosErrores.name =
        "El nombre no puede superar los 45 caracteres.";
    }

    if (
      formulario.price === "" ||
      Number(formulario.price) <= 0
    ) {
      nuevosErrores.price =
        "El precio debe ser mayor que cero.";
    }

    if (
      !Number.isInteger(Number(formulario.stock)) ||
      Number(formulario.stock) < 0
    ) {
      nuevosErrores.stock =
        "El stock debe ser un número entero positivo.";
    }

    if (
      formulario.image.trim().length > 45
    ) {
      nuevosErrores.image =
        "La imagen no puede superar los 45 caracteres.";
    }

    if (
      !Number.isInteger(Number(formulario.categoryId)) ||
      Number(formulario.categoryId) <= 0
    ) {
      nuevosErrores.categoryId =
        "La categoría debe ser un número entero mayor que cero.";
    }

    if (formulario.technicalDetails.trim().length > 3000) {
  nuevosErrores.technicalDetails =
    "Las características técnicas admiten máximo 3000 caracteres.";
    }

    if (formulario.reference.trim().length > 50) {
  nuevosErrores.reference =
    "La referencia admite máximo 50 caracteres.";
    }

    if (formulario.offerPrice !== "") {
      const oferta = Number(formulario.offerPrice);
      const precioNormal = Number(formulario.price);

      if (!Number.isFinite(oferta) || oferta <= 0) {
        nuevosErrores.offerPrice =
          "El precio de oferta debe ser mayor que cero.";
      } else if (oferta >= precioNormal) {
        nuevosErrores.offerPrice =
          "El precio de oferta debe ser menor que el precio normal.";
      } else if (
        Math.abs(oferta * 100 - Math.round(oferta * 100)) > 0.000001
      ) {
        nuevosErrores.offerPrice =
          "El precio de oferta admite máximo dos decimales.";
      }
    }

    setErrores(nuevosErrores);

    return Object.keys(nuevosErrores).length === 0;
  };

  const manejarEnvio = (evento) => {
    evento.preventDefault();

    if (!validarFormulario()) {
      return;
    }

    const producto = {
      productId: Number(formulario.productId),
      name: formulario.name.trim(),
      reference: formulario.reference.trim(),
      description: formulario.description.trim(),
      technicalDetails: formulario.technicalDetails.trim(),
      price: Number(formulario.price),
      offerPrice: formulario.offerPrice === ""
      ? null
      : Number(formulario.offerPrice),
      stock: Number(formulario.stock),
      image: formulario.image.trim(),
      categoryId: Number(formulario.categoryId),
    };

    onGuardar(producto);
  };

  return (
    <form onSubmit={manejarEnvio} noValidate>
      <div className="row g-3">
        <div className="col-md-4">
          <label
            htmlFor="productId"
            className="form-label"
          >
            ID del producto
          </label>
          <input
            id="productId"
            name="productId"
            type="number"
            min="1"
            className={`form-control ${
              errores.productId ? "is-invalid" : ""
            }`}
            value={formulario.productId}
            onChange={manejarCambio}
            disabled={esEdicion}
          />
          <div className="invalid-feedback">
            {errores.productId}
          </div>
        </div>

        <div className="col-md-8">
          <label
            htmlFor="name"
            className="form-label"
          >
            Nombre
          </label>
          <input
            id="name"
            name="name"
            type="text"
            maxLength="45"
            className={`form-control ${
              errores.name ? "is-invalid" : ""
            }`}
            value={formulario.name}
            onChange={manejarCambio}
          />
          <div className="invalid-feedback">
            {errores.name}
          </div>
        </div>

            <div className="col-12">
      <label htmlFor="reference" className="form-label">
        Referencia
      </label>

      <input
        id="reference"
        name="reference"
        type="text"
        maxLength={50}
        className={`form-control ${
          errores.reference ? "is-invalid" : ""
        }`}
        value={formulario.reference}
        onChange={manejarCambio}
      />

      <div className="invalid-feedback">
        {errores.reference}
      </div>

      <div className="form-text">
        Código de referencia del producto. Opcional y único.
      </div>
    </div>

        <div className="col-12">
          <label
            htmlFor="description"
            className="form-label"
          >
            Descripción
          </label>
          <textarea
            id="description"
            name="description"
            className="form-control"
            rows="3"
            maxLength="1000"
            value={formulario.description}
            onChange={manejarCambio}
          />
        </div>

        <div className="col-12">
          <label
            htmlFor="technicalDetails"
            className="form-label"
          >
            Características técnicas
          </label>

          <textarea
          id="technicalDetails"
          name="technicalDetails"
          className={`form-control ${
            errores.technicalDetails ? "is-invalid" : ""
          }`}
          rows="5"
          maxLength={3000}
          value={formulario.technicalDetails}
          onChange={manejarCambio}
          aria-describedby="ayudaCaracteristicas errorCaracteristicas"
          aria-invalid={Boolean(errores.technicalDetails)}
        />

        <div
          id="errorCaracteristicas"
          className="invalid-feedback"
        >
          {errores.technicalDetails}
        </div>

        <div id="ayudaCaracteristicas" className="form-text">
          Campo opcional. Escribe material, dimensiones, peso
          y funciones, una característica por línea.
          Máximo 3000 caracteres.
          </div>
        </div>

        <div className="col-md-4">
          <label
            htmlFor="price"
            className="form-label"
          >
            Precio
          </label>
          <input
            id="price"
            name="price"
            type="number"
            min="0.01"
            step="0.01"
            className={`form-control ${
              errores.price ? "is-invalid" : ""
            }`}
            value={formulario.price}
            onChange={manejarCambio}
          />
          <div className="invalid-feedback">
            {errores.price}
          </div>
        </div>

        <div className="col-md-4">
        <label htmlFor="offerPrice" className="form-label">
          Precio de oferta
        </label>

        <input
          id="offerPrice"
          name="offerPrice"
          type="number"
          min="0.01"
          step="0.01"
          className={`form-control ${
            errores.offerPrice ? "is-invalid" : ""
          }`}
          value={formulario.offerPrice}
          onChange={manejarCambio}
          placeholder="Sin oferta"
          aria-describedby="errorOferta ayudaOferta"
          aria-invalid={Boolean(errores.offerPrice)}
        />

        <div id="errorOferta" className="invalid-feedback">
          {errores.offerPrice}
        </div>

        <div id="ayudaOferta" className="form-text">
          Opcional. Déjalo vacío para quitar la oferta.
          Debe ser menor que el precio normal.
        </div>
      </div>

        <div className="col-md-4">
          <label
            htmlFor="stock"
            className="form-label"
          >
            Stock
          </label>
          <input
            id="stock"
            name="stock"
            type="number"
            min="0"
            className={`form-control ${
              errores.stock ? "is-invalid" : ""
            }`}
            value={formulario.stock}
            onChange={manejarCambio}
          />
          <div className="invalid-feedback">
            {errores.stock}
          </div>
        </div>

        <div className="col-md-4">
          <label
            htmlFor="categoryId"
            className="form-label"
          >
            ID de categoría
          </label>
          <input
            id="categoryId"
            name="categoryId"
            type="number"
            min="1"
            className={`form-control ${
              errores.categoryId ? "is-invalid" : ""
            }`}
            value={formulario.categoryId}
            onChange={manejarCambio}
          />
          <div className="invalid-feedback">
            {errores.categoryId}
          </div>
        </div>

        <div className="col-12">
          <label
            htmlFor="image"
            className="form-label"
          >
            Nombre o ruta de la imagen
          </label>
          <input
            id="image"
            name="image"
            type="text"
            maxLength="45"
            className={`form-control ${
              errores.image ? "is-invalid" : ""
            }`}
            value={formulario.image}
            onChange={manejarCambio}
            placeholder="Ejemplo: producto.jpg"
          />
          <div className="invalid-feedback">
            {errores.image}
          </div>
        </div>

        <div className="col-12 d-flex gap-2 justify-content-end mt-4">
          <button
            type="button"
            className="btn btn-outline-secondary"
            onClick={onCancelar}
            disabled={guardando}
          >
            Cancelar
          </button>

          <button
            type="submit"
            className="btn btn-danger"
            disabled={guardando}
          >
            {guardando
              ? "Guardando..."
              : esEdicion
                ? "Guardar cambios"
                : "Registrar producto"}
          </button>
        </div>
      </div>
    </form>
  );
}

export default ProductoForm;
