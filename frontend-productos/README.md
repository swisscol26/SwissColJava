# CRUD administrativo de productos — Swisscol

Proyecto front-end desarrollado en React para administrar los productos del sistema Swisscol. Permite consultar, registrar, visualizar, actualizar y eliminar productos mediante una API Java conectada con MySQL.

## Tecnologías utilizadas

- React con Vite
- React Router DOM
- Axios
- Bootstrap
- Java HttpServer
- Gson
- MySQL
- Git y GitHub

## Funcionalidades

- Listado y búsqueda de productos.
- Registro de productos.
- Consulta individual de productos.
- Actualización de productos.
- Eliminación con confirmación.
- Validación de formularios.
- Navegación sin recargar la página.
- Mensajes de carga, éxito y error.

## Hooks utilizados

- `useState`: manejo de formularios, tablas, carga y mensajes.
- `useEffect`: consulta de datos al iniciar las pantallas.
- `useNavigate`: navegación entre las páginas.
- `useParams`: lectura del identificador incluido en las rutas.

## Rutas del módulo

- `/admin/productos`: listado de productos.
- `/admin/productos/nuevo`: registro de productos.
- `/admin/productos/:productId`: detalle de un producto.
- `/admin/productos/:productId/editar`: edición de un producto.

## Estructura principal

```text
src/
├── components/
│   └── productos/
├── pages/
│   └── productos/
├── services/
├── assets/
├── App.jsx
└── main.jsx
```

## Requisitos

- Node.js y npm.
- Java JDK.
- MySQL.
- Base de datos `database_swisscol`.
- Variable de entorno `SWISSCOL_DB_PASSWORD`.

## Ejecución

1. Iniciar MySQL.
2. Ejecutar `ProductoApi.java`.
3. Abrir una terminal dentro de `frontend-productos`.
4. Instalar las dependencias:

```bash
npm install
```

5. Iniciar React:

```bash
npm run dev
```

6. Abrir `http://localhost:5173/admin/productos`.

## Verificación

```bash
npm run lint
npm run build
```

## Repositorio

https://github.com/swisscol26/SwissColJava

## Autor

Marlon Pulido — Programa ADSO, SENA.