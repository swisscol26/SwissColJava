# Recuperación de contraseña y compras autenticadas

La tienda incluye «Olvidé mi contraseña». El usuario recibe un código de 256 bits, lo pega en «Ya tengo un código» y define su contraseña. Dura 15 minutos, se guarda solo su SHA-256 y es de un uso. El cambio compara el hash anterior al actualizar `users`, revoca sesiones y no inicia sesión automáticamente. No se altera el esquema.

Las solicitudes de correos existentes e inexistentes obtienen la misma respuesta; el trabajo de correo ocurre en una cola limitada. Límite: una solicitud por correo cada 10 minutos y 30 solicitudes globales/minuto. El código no aparece en respuestas ni logs. Un fallo de envío se registra sin datos personales; no debe interpretarse el mensaje genérico como confirmación de entrega.

Los códigos y límites residen en memoria, igual que las sesiones actuales. Un reinicio invalida los códigos: pedir uno nuevo. Esta implementación corresponde a una instancia; antes de escalar se debe trasladar tokens, límites y sesiones a un almacenamiento compartido con expiración y consumo atómico.

## Correo

### Opción con Gmail verificado: Brevo por HTTPS

Para el caso actual, sin dominio propio, se añadió Brevo. Crear una cuenta, activar el servicio transaccional y verificar el Gmail como remitente en Brevo. En Render configurar `SWISSCOL_MAIL_USER` con el Gmail verificado y `SWISSCOL_BREVO_API_KEY` con la clave API de Brevo (no una clave SMTP). La clave se guarda únicamente en Render. Brevo tiene prioridad si esa variable existe; si está vacía se considera una configuración incompleta.

Brevo documenta que reemplaza temporalmente el dominio del remitente cuando se usa Gmail para cumplir los requisitos de entrega. Esto no autentica el dominio gmail.com ni garantiza la aprobación de una cuenta o la llegada a todas las bandejas. Antes de dar la función por activada comprobar el servicio transaccional y una entrega real autorizada.

Fuentes: https://developers.brevo.com/docs/send-a-transactional-email y https://help.brevo.com/hc/en-us/articles/14925263522578-Comply-with-Gmail-Yahoo-and-Microsoft-s-requirements-for-email-senders.

### Opciones anteriores

Gmail existente: `SWISSCOL_MAIL_USER` y `SWISSCOL_MAIL_PASSWORD` (contraseña de aplicación, privada). La dirección configurada recibe los avisos de pedidos; los códigos se envían al correo de cada cuenta.

Render Free bloquea SMTP 25/465/587. Para ese entorno se añadió Resend por HTTPS, activado al configurar:

- `SWISSCOL_RESEND_API_KEY`: clave privada, solo en Render.
- `SWISSCOL_MAIL_FROM`: remitente de un dominio verificado en Resend, por ejemplo `SwissCol <cuentas@tu-dominio.com>`.
- `SWISSCOL_MAIL_USER`: buzón que recibe los avisos de pedidos, conservando la configuración existente.

Una dirección Gmail receptora no sustituye un dominio remitente verificado. No configurar un remitente de prueba para recuperar cuentas de clientes arbitrarios. Si hay una clave Resend configurada pero incompleta, el sistema no intenta silenciosamente Gmail. Nunca guardar credenciales en Git ni en el frontend.

Referencias consultadas: https://render.com/docs/free#other-limitations y https://resend.com/docs/api-reference/emails/send-email.

## Publicación y verificación

Publicar los cambios de `SWISSCOL` y actualizar el SHA fijado en la etapa `tienda` del Dockerfile antes de desplegar el backend. Sin actualizar ese SHA, Render seguirá sirviendo la tienda anterior. Configurar el proveedor antes de probar la entrega de correo. No se enviaron mensajes reales durante las pruebas locales.

El visitante puede consultar el catálogo y preparar un carrito. Antes de abrir envío y antes de enviar el pedido se verifica `/api/auth/sesion`, sin confiar en localStorage. La API conserva su rechazo de pedidos sin sesión. Si expira una sesión, el carrito se conserva y se solicita autenticación.

Prueba local sin datos reales: compilar Java y ejecutar `pruebas.RecuperacionPrueba`. En la web verificar: visitante → Finalizar compra → login; registro → login → formulario de envío; sesión cerrada/vencida → pedido rechazado; recuperar cuenta propia → correo → cambiar clave → clave anterior rechazada. Usar una cuenta de prueba autorizada para el envío real.
