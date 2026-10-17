# Publicación de SwissCol

## Estado al 15 de octubre de 2026

El proyecto sigue funcionando como aplicación local. No se ha publicado ni contratado hosting.
Se verificaron en modo lectura 16 productos de MySQL: todos tienen imagen local coincidente.
Se normalizaron 11 nombres de imágenes a minúsculas para evitar fallos en Linux.
El catálogo y sus precios, existencias y pedidos no se modificaron.

Se crearon respaldos SQL y ZIP de imágenes en `respaldos-locales` en la raíz del workspace.
Son privados, están excluidos de Git y no deben copiarse al directorio público del sitio.
El respaldo se exportó correctamente; su restauración en otro servidor sigue pendiente.

## Conexiones preparadas

- Tienda: `SWISSCOL/js/config.js`. En localhost usa Java en el puerto 8080; en un dominio usa `/api`.
- Panel: `frontend-productos/src/config.js`. Misma regla para `/api`; enlace a la tienda en `/`.
- Servidor: `PORT` (8080 por defecto) y `SWISSCOL_BIND_ADDRESS` (0.0.0.0 por defecto).
- CORS: `SWISSCOL_ALLOWED_ORIGINS`, lista de orígenes exactos separados por comas, sin rutas ni barra final. Si no existe conserva los cuatro orígenes locales anteriores. En producción reemplazarlos por el origen HTTPS real.
- Sesión: `SWISSCOL_COOKIE_SECURE=true` en producción. No activarlo durante pruebas por HTTP local.
- MySQL: `SWISSCOL_DB_URL`, `SWISSCOL_DB_USER` y `SWISSCOL_DB_PASSWORD`. Sin las dos primeras se conserva la configuración local anterior. Para servidor remoto usar TLS y un usuario de aplicación con los permisos necesarios.
- `.env.example` documenta los nombres; Java NO carga archivos `.env` automáticamente. Las variables deben establecerse en el proceso o servicio que inicia Java.

## Arquitectura de publicación prevista

Servir la tienda, el panel y la API en un mismo dominio HTTPS:

| Ruta | Destino |
| --- | --- |
| `/` y archivos de la tienda | Contenido de SWISSCOL |
| `/admin` y `/admin/*` | Panel React compilado; fallback a su index.html |
| `/assets/*` | Archivos generados por la compilación del panel |
| `/api/*` | Proxy a Java, preservando la ruta completa y las cookies |

El servidor web debe terminar HTTPS. El proceso Java y MySQL no necesitan puertos públicos.
Servir exclusivamente los archivos públicos, nunca el workspace completo, código Java, repositorios,
credenciales ni respaldos. No usar Live Server ni el servidor de desarrollo de Vite en producción.
Este diseño permite conservar `SameSite=Lax` y la sesión del mismo dominio entre tienda y panel.
Un hosting que solo sirve archivos estáticos no ejecuta por sí solo la API Java ni MySQL.
La preparación no requiere Docker en el computador del usuario.

## Antes de habilitar ventas

1. Elegir hosting con soporte para Java 21 y MySQL, almacenamiento persistente y HTTPS.
2. Confirmar teléfono/WhatsApp, correo y ciudad reales. WhatsApp confirmado: 3008008180. Correo pendiente; se retiró el correo de ejemplo. La ciudad aún debe confirmarse.
3. Completar preguntas frecuentes, garantía/devoluciones y privacidad con información aprobada por el responsable. Se incorporaron preguntas frecuentes y orientación de garantía con fuente oficial de Victorinox. La política de devoluciones y privacidad sigue pendiente.
4. Implementar suscripciones con consentimiento y persistencia, o retirar el formulario de promociones antes del lanzamiento. Se retiró el formulario que no guardaba correos y se reemplazó por contacto mediante WhatsApp.
5. Revisar seguridad del acceso y registro: límites de intentos, tratamiento de entradas y registros de errores. Las pruebas actuales no constituyen una auditoría de seguridad completa.
6. Restaurar el respaldo en una base nueva, sin sobrescribir la local; comprobar tablas, relaciones, pedidos, existencias y número de productos. Los scripts SQL antiguos no sustituyen el respaldo del esquema actual.
7. Configurar datos de Nequi y correo de aviso; probarlos con el responsable.
8. Probar en HTTPS registro, login/logout, permisos de administrador, catálogo, imágenes, carrito, pedido, envío, reporte y confirmación de pago, notificaciones y vencimiento de reservas.
9. Verificar presentación en móvil y escritorio, copias automáticas y restauración.

La sesión actual está en memoria: reiniciar Java cierra sesiones. Usar inicialmente una sola instancia de API; varias instancias requieren resolver sesiones compartidas y coordinación de tareas.

## Validaciones de esta revisión

- Siete pruebas Node aprobadas: configuración de URL y flujo de pagos/notificaciones.
- Panel: lint y compilación Vite aprobados.
- Compilación completa de Java 21 aprobada en carpeta temporal.
- Pruebas Java aprobadas: sesiones (5), productos (10), pedidos (5), configuración de pagos HTTP (16).
- Configuración de puerto/origen/cookie verificada en procesos separados para local y un dominio HTTPS de prueba.
- Consulta de solo lectura del catálogo: 16 productos y cero imágenes faltantes por coincidencia exacta.
- No se ejecutó una compra real ni se modificaron pedidos en esta revisión. Falta la prueba integral en el hosting elegido.
