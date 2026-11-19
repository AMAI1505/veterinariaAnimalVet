CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    descripcion VARCHAR(100),
    puede_ver BIT(1) NOT NULL,
    puede_crear BIT(1) NOT NULL,
    puede_editar BIT(1) NOT NULL,
    puede_eliminar BIT(1) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    nombre_completo VARCHAR(100),
    telefono VARCHAR(20),
    especialidad VARCHAR(60),
    tipo_personal VARCHAR(30),
    password VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    enabled BIT(1) NOT NULL,
    intentos_fallidos INT,
    fecha_desbloqueo DATETIME(6),
    token_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE turnos_laborales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    veterinario_id BIGINT NOT NULL,
    dia_semana VARCHAR(12) NOT NULL,
    hora_inicio TIME(6) NOT NULL,
    hora_fin TIME(6) NOT NULL,
    hora_descanso_inicio TIME(6),
    duracion_descanso_minutos INT,
    CONSTRAINT pk_turnos_laborales PRIMARY KEY (id),
    CONSTRAINT uk_turno_vet_dia UNIQUE (veterinario_id, dia_semana),
    CONSTRAINT fk_turnos_veterinario FOREIGN KEY (veterinario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE clientes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dni VARCHAR(8) NOT NULL,
    nombre VARCHAR(70) NOT NULL,
    telefono VARCHAR(9) NOT NULL,
    correo VARCHAR(100),
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uk_clientes_dni UNIQUE (dni)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mascotas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(40) NOT NULL,
    especie VARCHAR(30) NOT NULL,
    sexo VARCHAR(10),
    esterilizado BIT(1),
    raza VARCHAR(40) NOT NULL,
    peso VARCHAR(6) NOT NULL,
    color VARCHAR(30) NOT NULL,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT pk_mascotas PRIMARY KEY (id),
    CONSTRAINT fk_mascotas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE citas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha_hora DATETIME(6) NOT NULL,
    mascota_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    veterinario_id BIGINT,
    nombre_profesional VARCHAR(100),
    motivo VARCHAR(100) NOT NULL,
    estado VARCHAR(255) NOT NULL,
    CONSTRAINT pk_citas PRIMARY KEY (id),
    CONSTRAINT fk_citas_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas (id),
    CONSTRAINT fk_citas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_citas_veterinario FOREIGN KEY (veterinario_id) REFERENCES usuarios (id),
    INDEX idx_citas_fecha_hora (fecha_hora),
    INDEX idx_citas_veterinario_fecha (veterinario_id, fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE consultas_medicas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cita_id BIGINT NOT NULL,
    mascota_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    veterinario_id BIGINT,
    nombre_veterinario VARCHAR(100),
    peso_kg DECIMAL(7,2),
    exploracion_clinica VARCHAR(4000),
    diagnostico VARCHAR(4000) NOT NULL,
    tratamiento VARCHAR(4000) NOT NULL,
    indicaciones_tutor VARCHAR(4000),
    proximo_control DATE,
    fecha_atencion DATETIME(6) NOT NULL,
    CONSTRAINT pk_consultas_medicas PRIMARY KEY (id),
    CONSTRAINT uk_consulta_cita UNIQUE (cita_id),
    CONSTRAINT fk_consultas_cita FOREIGN KEY (cita_id) REFERENCES citas (id),
    CONSTRAINT fk_consultas_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas (id),
    CONSTRAINT fk_consultas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_consultas_veterinario FOREIGN KEY (veterinario_id) REFERENCES usuarios (id),
    INDEX idx_consultas_mascota_fecha (mascota_id, fecha_atencion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
