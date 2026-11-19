# Despliegue AnimalVet

El proyecto incluye `compose.yaml`, `Dockerfile` y una puerta de entrada Caddy. Caddy obtiene y renueva el certificado HTTPS automáticamente una vez que el dominio real resuelve al servidor.

## Requisitos externos

- Un servidor Linux accesible desde Internet con Docker Engine y Docker Compose.
- Un dominio propio cuyo registro DNS `A` apunte a la IP pública del servidor. Si se publica un registro `AAAA`, también debe apuntar a ese servidor.
- Los puertos TCP 80 y 443 abiertos. UDP 443 es opcional y habilita HTTP/3.
- Valores secretos únicos para la base de datos, el administrador inicial y `ANIMALVET_JWT_SECRET`.

## Preparar y arrancar

1. Copia `.env.example` como `.env` y reemplaza cada valor. No uses el dominio de ejemplo ni conserves los textos `REPLACE_...`.
2. Genera contraseñas aleatorias distintas para MySQL, el administrador, recepción y veterinario. `ANIMALVET_JWT_SECRET` debe contener al menos 32 bytes aleatorios; usa 64 caracteres aleatorios.
3. Configura el DNS del dominio y espera a que resuelva a la IP pública del servidor.
4. En la carpeta del proyecto ejecuta `docker compose up -d --build`.
5. Verifica el sitio en `https://<tu-dominio>` y la API en `https://<tu-dominio>/api`.

La base de datos persiste en el volumen Docker `animalvet-db`; agenda copias de seguridad externas del volumen. No publiques el puerto de MySQL. La API y la web solo son accesibles a través de Caddy.

## Operación segura

- Guarda `.env` fuera del control de versiones y limita su acceso al usuario administrador del servidor.
- Conserva `ANIMALVET_JWT_SECRET` entre despliegues para no invalidar todas las sesiones. Si se filtra, cámbialo; esto invalida los tokens emitidos con la clave anterior.
- Cambia las contraseñas iniciales después del primer ingreso y conserva una copia segura de los secretos.
- Antes de actualizar, respalda la base de datos y prueba el cambio en una copia.
- Las migraciones de esquema todavía usan Hibernate `ddl-auto=update`; para una operación regulada o de alta disponibilidad hay que migrarlas a Flyway/Liquibase antes de automatizar cambios de esquema.

El dominio, la IP del servidor, los secretos y la emisión real del certificado no se pueden completar desde el repositorio: requieren contratar/configurar esos recursos externos.
