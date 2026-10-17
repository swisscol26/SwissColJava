# Publicación académica en Render

## Preparación al 17 de octubre de 2026

El Dockerfile construye Java 21 y el panel React en Render. Descarga la tienda pública
`swisscol26/Swisscol-2026` fijada al commit `161bb4c659425bfa4aa7c8fb960dc187ea7a54c5`.
No es necesario instalar Docker en el computador. No se ha probado todavía la
construcción de la imagen en Render ni se ha publicado el sitio.

La carpeta pública de la imagen contiene únicamente index.html, css, js, img,
admin/index.html y assets. Java sirve tienda, panel y API desde el mismo puerto.
Las rutas `/admin/*` devuelven el panel React; su API conserva la autorización
del servidor. Los archivos privados y las rutas fuera de la carpeta pública se rechazan.
Sin SWISSCOL_WEB_ROOT el servidor local conserva su funcionamiento anterior.

## Configuración del servicio

1. Crear una cuenta en Render y conectar GitHub.
2. Seleccionar **New > Web Service**, repositorio **swisscol26/SwissColJava**,
   rama **main**, lenguaje **Docker** y plan **Free**.
3. Dejar Root Directory vacío: el Dockerfile está en la raíz del repositorio.
   Dockerfile Path: `./Dockerfile`. No escribir Build Command ni Docker Command.
4. Antes del despliegue funcional, preparar una base MySQL separada para la demo,
   con el esquema actual, el catálogo y un administrador de prueba. No publicar
   respaldos completos con usuarios, direcciones, pedidos o contraseñas reales.
5. Configurar las variables siguientes en el panel de Render, nunca en Git:

| Variable | Valor |
| --- | --- |
| SWISSCOL_ALLOWED_ORIGINS | Origen HTTPS exacto asignado por Render, sin barra final |
| SWISSCOL_COOKIE_SECURE | true |
| SWISSCOL_DB_URL | URL JDBC de la base remota, con TLS y verificación de identidad |
| SWISSCOL_DB_USER | Usuario de la base de demostración |
| SWISSCOL_DB_PASSWORD | Contraseña privada de ese usuario |

Render proporciona PORT. El Dockerfile fija SWISSCOL_BIND_ADDRESS=0.0.0.0
y SWISSCOL_WEB_ROOT=/app/web. Si MySQL requiere una CA particular, configurar su
certificado de confianza antes de conectar: no desactivar la verificación TLS.

## Pendiente antes de entregar el enlace

- Crear y configurar las cuentas de alojamiento y base de datos.
- Preparar/importar un esquema actualizado y datos de demostración conservando los productos.
- Identificar claramente la web como ejercicio académico y evitar solicitudes de pagos reales.
- Probar la imagen en Render y revisar sus registros de inicio.
- Comprobar HTTPS, catálogo, imágenes, registro, sesión, permisos, pedidos,
  administración, simulación de pago y actualización al recargar `/admin/pedidos`.
- Revisar el tratamiento de contenido de productos antes de abrir registros públicos.

El plan gratuito duerme tras 15 minutos de inactividad y puede tardar cerca de un
minuto en despertar. Las sesiones se pierden al reiniciar Java. La revisión de
reservas no corre mientras duerme y se reanuda al arrancar. No guardar nuevas
imágenes o archivos subidos en el disco efímero de Render.

Para publicar futuros cambios de la tienda, subirlos a su repositorio y actualizar
el commit fijado en Dockerfile; cambiar la tienda sola no actualiza esta imagen.

Fuentes oficiales:
- https://render.com/docs/docker
- https://render.com/docs/web-services
- https://render.com/docs/free
- https://aiven.io/docs/products/mysql/concepts/mysql-free-tier
