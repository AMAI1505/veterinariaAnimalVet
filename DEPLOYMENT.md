# Despliegue de AnimalVet

El proyecto se despliega con Docker Compose: MySQL guarda los datos, Spring Boot sirve la API, Nginx publica Angular y Caddy recibe el tráfico público y gestiona el certificado HTTPS.

## Requisitos

- Un servidor Linux público con Docker Engine y Docker Compose.
- Un dominio cuyo registro DNS `A` apunte a la IP del servidor. Si publicas un registro `AAAA`, debe apuntar al mismo servidor.
- Puertos TCP 80 y 443 abiertos en el firewall y en el proveedor del servidor. UDP 443 es opcional y habilita HTTP/3.
- Contraseñas únicas para MySQL y para las cuentas iniciales. En producción, cada contraseña inicial debe tener entre 12 y 100 caracteres, con mayúscula, minúscula, número y símbolo.
- Una clave JWT de al menos 32 bytes aleatorios. Por ejemplo, en el servidor: `openssl rand -hex 32`.

## Instalar en una base nueva

1. Copia `.env.example` a `.env` en el servidor.
2. Reemplaza todos los textos `REPLACE_...`; configura `DOMAIN` con el dominio real. No reutilices la misma contraseña para MySQL, administrador, recepción y veterinario.
3. Deja `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`. Una base recién creada está vacía: Flyway ejecuta `V1__create_initial_schema.sql` y Hibernate valida el esquema resultante.
4. Configura el DNS y espera a que el dominio resuelva a la IP pública del servidor.
5. Desde la raíz del proyecto ejecuta `docker compose up -d --build`.
6. Revisa el arranque con `docker compose ps` y `docker compose logs --tail=100 api gateway`.
7. Abre `https://<tu-dominio>` y comprueba la API en `https://<tu-dominio>/api`.

Caddy solicita y renueva el certificado HTTPS automáticamente cuando el dominio resuelve al servidor y los puertos requeridos son accesibles. No hace falta subir un certificado al repositorio. El volumen `caddy-data` conserva el estado de certificados entre reinicios.

En una instalación vacía, el sistema crea las cuentas `administrador`, `recepcion` y `veterinario.prueba` con las contraseñas indicadas en `INITIAL_ADMIN_PASSWORD`, `INITIAL_RECEPTION_PASSWORD` e `INITIAL_VET_PASSWORD`. Las contraseñas se guardan con hash. En una base que ya tenía usuarios, las variables iniciales no cambian las contraseñas existentes.

## Adoptar una base MySQL que ya tiene datos

Haz esto solo para una base MySQL cuyo esquema ya corresponda a las entidades actuales. La migración `V1` crea un esquema nuevo; al adoptar uno existente, Flyway debe marcar esa versión como ya instalada, sin ejecutar sus `CREATE TABLE`.

1. Haz y prueba una copia de seguridad restaurable antes de continuar.
2. Verifica que el esquema existente contiene las tablas y columnas del modelo actual: `roles`, `usuarios`, `turnos_laborales`, `clientes`, `mascotas`, `citas` y `consultas_medicas`. La aplicación usa Hibernate `validate`; si falta algo, no iniciará ni intentará corregirlo automáticamente.
3. Cambia temporalmente `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` en `.env` y ejecuta `docker compose up -d --build api`.
4. Sigue los logs con `docker compose logs -f api`. Cuando el servicio indique que arrancó y la validación del esquema pase, detén el seguimiento con Ctrl+C; el contenedor seguirá funcionando.
5. Vuelve a dejar `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false` en `.env` y recrea el servicio con `docker compose up -d --force-recreate api`.

No uses el modo de adopción si el esquema está incompleto o no sabes de dónde proviene. Si la validación falla, conserva la copia de seguridad y prepara una migración de actualización específica antes de desplegar.

## Secretos y cuentas

- Mantén `.env` solo en el servidor y limita su lectura al administrador. El archivo está excluido de Git.
- Conserva `ANIMALVET_JWT_SECRET` entre despliegues; cambiarlo invalida las sesiones activas.
- Las variables `INITIAL_*_PASSWORD` son obligatorias en el perfil `prod`. Solo inicializan las cuentas en una base vacía, salvo que uses el mecanismo de restablecimiento.
- Para restablecer la contraseña raíz de forma puntual, añade `ADMIN_PASSWORD_RESET` a `.env`, recrea la API una vez y luego elimina esa variable y vuelve a recrear la API. Si permanece configurada, la aplicación vuelve a establecer esa contraseña en cada arranque.
- Cambiar `DB_PASSWORD` en `.env` no cambia por sí solo la contraseña del usuario dentro de un volumen MySQL ya inicializado. Coordina la rotación en MySQL y en `.env` antes de recrear la API.
- Conserva las copias de seguridad cifradas y fuera del servidor. Ejemplo de copia lógica en Linux: `docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysqldump --single-transaction -u"$MYSQL_USER" "$MYSQL_DATABASE"' > animalvet-backup.sql`.
- Antes de actualizar, respalda la base de datos y prueba la nueva versión con una copia restaurada.

MySQL no publica un puerto al exterior; API, web y base de datos solo se comunican por la red privada de Compose. HTTPS de Caddy protege el tráfico del navegador al servidor. Si más adelante usas una base de datos externa, configura también TLS para esa conexión.

El dominio, la IP del servidor, los secretos y la emisión efectiva del certificado requieren configurar los recursos externos; no se pueden completar desde el repositorio.
