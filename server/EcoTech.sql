CREATE DATABASE IF NOT EXISTS EcoTech;
USE EcoTech;

CREATE TABLE IF NOT EXISTS Ciudades (
    ciudad_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    departamento VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Usuarios (
    usuario_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT 1,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_Usuarios_Rol CHECK (rol IN ('Auditor', 'Operador', 'Tecnico', 'Administrador'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Donantes (
    donante_id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    ciudad_id INT,
    direccion VARCHAR(250) NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_Donantes_tipo CHECK (tipo IN ('Empresa', 'Persona')),
    FOREIGN KEY (ciudad_id) REFERENCES Ciudades(ciudad_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Beneficiarios (
    beneficiario_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    ciudad_id INT,
    direccion VARCHAR(250) NOT NULL,
    estrato INT CHECK (estrato >= 1 AND estrato <= 3),
    necesidad TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ciudad_id) REFERENCES Ciudades(ciudad_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS TiposEquipo (
    tipo_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(250)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Equipos (
    equipo_id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_id INT NOT NULL,
    donante_id INT,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    serial VARCHAR(100) UNIQUE,
    estado_ingreso VARCHAR(50),
    estado_actual VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    fecha_recepcion DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT,
    CONSTRAINT CK_Equipos_estado CHECK (estado_ingreso IN ('Para piezas', 'Malo', 'Regular', 'Bueno')),
    CONSTRAINT CK_Equipos_estado_actual CHECK (estado_actual IN ('Descartado', 'Entregado', 'Repotenciado', 'En reparacion', 'En diagnostico', 'Recibido')),
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(usuario_id),
    FOREIGN KEY (tipo_id) REFERENCES TiposEquipo(tipo_id),
    FOREIGN KEY (donante_id) REFERENCES Donantes(donante_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Diagnosticos (
    diagnostico_id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_id INT NOT NULL,
    tecnico_id INT NOT NULL,
    descripcion VARCHAR(250),
    requiere_repara BOOLEAN DEFAULT 1,
    costo_estimado DECIMAL(18, 2),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipo_id) REFERENCES Equipos(equipo_id),
    FOREIGN KEY (tecnico_id) REFERENCES Usuarios(usuario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Reparaciones (
    reparacion_id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_id INT NOT NULL,
    tecnico_id INT NOT NULL,
    descripcion VARCHAR(250),
    repuestos_usados VARCHAR(100),
    costo_real DECIMAL(18, 2),
    estado VARCHAR(50),
    fecha_inicio DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_fin DATETIME,
    CONSTRAINT CK_Reparaciones_estado CHECK (estado IN ('Cancelada', 'Completada', 'En proceso')),
    FOREIGN KEY (equipo_id) REFERENCES Equipos(equipo_id),
    FOREIGN KEY (tecnico_id) REFERENCES Usuarios(usuario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Entregas (
    entrega_id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_id INT NOT NULL,
    beneficiario_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_entrega DATETIME DEFAULT CURRENT_TIMESTAMP,
    condiciones VARCHAR(100),
    observaciones VARCHAR(100),
    FOREIGN KEY (equipo_id) REFERENCES Equipos(equipo_id),
    FOREIGN KEY (beneficiario_id) REFERENCES Beneficiarios(beneficiario_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(usuario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Auditoria (
    auditoria_id INT AUTO_INCREMENT PRIMARY KEY,
    tabla_afectada VARCHAR(100) NOT NULL,
    operacion VARCHAR(15) NOT NULL,
    registro_id INT,
    usuario_sql VARCHAR(100) DEFAULT (USER()),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    detalle VARCHAR(100),
    CONSTRAINT CK_Auditoria_operacion CHECK (operacion IN ('DELETE', 'UPDATE', 'INSERT'))
) ENGINE=InnoDB;
