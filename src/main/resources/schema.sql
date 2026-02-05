-- =====================================================
-- Script de creación de Base de Datos - SaaS Platform
-- Base de datos: MySQL
-- =====================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS saas_platform
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE saas_platform;

-- =====================================================
-- TABLA: usuarios
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    email_verificado BIT NOT NULL DEFAULT 0,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    ultimo_acceso DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY UK_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: perfiles
-- =====================================================
CREATE TABLE IF NOT EXISTS perfiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100),
    apellidos VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    ciudad VARCHAR(100),
    codigo_postal VARCHAR(10),
    pais VARCHAR(100),
    fecha_nacimiento DATE,
    avatar_url VARCHAR(500),
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_perfiles_usuario (usuario_id),
    CONSTRAINT FK_perfiles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: planes
-- =====================================================
CREATE TABLE IF NOT EXISTS planes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    tipo_plan ENUM('BASIC', 'PREMIUM', 'ENTERPRISE') NOT NULL,
    precio_mensual DECIMAL(10,2) NOT NULL,
    descripcion VARCHAR(500),
    caracteristicas TEXT,
    max_usuarios INT,
    almacenamiento_gb INT,
    soporte_prioritario BIT NOT NULL DEFAULT 0,
    activo BIT NOT NULL DEFAULT 1,
    orden_visualizacion INT DEFAULT 0,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: suscripciones
-- =====================================================
CREATE TABLE IF NOT EXISTS suscripciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    fecha_proximo_cobro DATE NOT NULL,
    estado ENUM('ACTIVA', 'CANCELADA', 'MOROSA', 'SUSPENDIDA', 'EXPIRADA') NOT NULL,
    renovacion_automatica BIT NOT NULL DEFAULT 1,
    precio_actual DECIMAL(10,2),
    fecha_cancelacion DATETIME(6),
    motivo_cancelacion VARCHAR(500),
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    usuario_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_suscripciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT FK_suscripciones_plan FOREIGN KEY (plan_id) REFERENCES planes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: metodos_pago
-- =====================================================
CREATE TABLE IF NOT EXISTS metodos_pago (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo_metodo_pago ENUM('TARJETA_CREDITO', 'PAYPAL', 'TRANSFERENCIA') NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    predeterminado BIT NOT NULL DEFAULT 0,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    usuario_id BIGINT NOT NULL,
    -- Campos para Tarjeta de Crédito
    numero_tarjeta VARCHAR(20),
    nombre_titular VARCHAR(100),
    fecha_expiracion VARCHAR(7),
    ultimos_digitos VARCHAR(4),
    -- Campos para PayPal
    email_paypal VARCHAR(150),
    paypal_id VARCHAR(100),
    -- Campos para Transferencia
    numero_cuenta VARCHAR(30),
    banco VARCHAR(100),
    iban VARCHAR(34),
    swift_bic VARCHAR(11),
    PRIMARY KEY (id),
    CONSTRAINT FK_metodos_pago_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: facturas
-- =====================================================
CREATE TABLE IF NOT EXISTS facturas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero_factura VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE', 'PAGADA', 'VENCIDA', 'CANCELADA') NOT NULL,
    fecha_pago DATETIME(6),
    notas VARCHAR(1000),
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6),
    suscripcion_id BIGINT NOT NULL,
    metodo_pago_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY UK_facturas_numero (numero_factura),
    CONSTRAINT FK_facturas_suscripcion FOREIGN KEY (suscripcion_id) REFERENCES suscripciones (id),
    CONSTRAINT FK_facturas_metodo_pago FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLAS DE AUDITORÍA (Hibernate Envers)
-- =====================================================
CREATE TABLE IF NOT EXISTS revinfo (
    rev INT NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios_aud (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    email VARCHAR(150),
    password VARCHAR(255),
    activo BIT,
    email_verificado BIT,
    fecha_creacion DATETIME(6),
    fecha_modificacion DATETIME(6),
    ultimo_acceso DATETIME(6),
    PRIMARY KEY (rev, id),
    CONSTRAINT FK_usuarios_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS perfiles_aud (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    nombre VARCHAR(100),
    apellidos VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    ciudad VARCHAR(100),
    codigo_postal VARCHAR(10),
    pais VARCHAR(100),
    fecha_nacimiento DATE,
    avatar_url VARCHAR(500),
    fecha_creacion DATETIME(6),
    fecha_modificacion DATETIME(6),
    usuario_id BIGINT,
    PRIMARY KEY (rev, id),
    CONSTRAINT FK_perfiles_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS suscripciones_aud (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    fecha_inicio DATE,
    fecha_fin DATE,
    fecha_proximo_cobro DATE,
    estado ENUM('ACTIVA', 'CANCELADA', 'MOROSA', 'SUSPENDIDA', 'EXPIRADA'),
    renovacion_automatica BIT,
    precio_actual DECIMAL(10,2),
    fecha_cancelacion DATETIME(6),
    motivo_cancelacion VARCHAR(500),
    fecha_creacion DATETIME(6),
    fecha_modificacion DATETIME(6),
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    usuario_id BIGINT,
    PRIMARY KEY (rev, id),
    CONSTRAINT FK_suscripciones_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS metodos_pago_aud (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    tipo_metodo_pago ENUM('TARJETA_CREDITO', 'PAYPAL', 'TRANSFERENCIA'),
    activo BIT,
    predeterminado BIT,
    fecha_creacion DATETIME(6),
    fecha_modificacion DATETIME(6),
    usuario_id BIGINT,
    numero_tarjeta VARCHAR(20),
    nombre_titular VARCHAR(100),
    fecha_expiracion VARCHAR(7),
    ultimos_digitos VARCHAR(4),
    email_paypal VARCHAR(150),
    paypal_id VARCHAR(100),
    numero_cuenta VARCHAR(30),
    banco VARCHAR(100),
    iban VARCHAR(34),
    swift_bic VARCHAR(11),
    PRIMARY KEY (rev, id),
    CONSTRAINT FK_metodos_pago_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS facturas_aud (
    id BIGINT NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    numero_factura VARCHAR(50),
    fecha_emision DATE,
    fecha_vencimiento DATE,
    subtotal DECIMAL(10,2),
    impuestos DECIMAL(10,2),
    total DECIMAL(10,2),
    estado ENUM('PENDIENTE', 'PAGADA', 'VENCIDA', 'CANCELADA'),
    fecha_pago DATETIME(6),
    notas VARCHAR(1000),
    fecha_creacion DATETIME(6),
    fecha_modificacion DATETIME(6),
    suscripcion_id BIGINT,
    metodo_pago_id BIGINT,
    PRIMARY KEY (rev, id),
    CONSTRAINT FK_facturas_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DATOS INICIALES: Planes
-- =====================================================
INSERT INTO planes (nombre, tipo_plan, precio_mensual, descripcion, caracteristicas, max_usuarios, almacenamiento_gb, soporte_prioritario, activo, orden_visualizacion, fecha_creacion)
SELECT 'Plan Básico', 'BASIC', 9.99, 'Ideal para comenzar', 'Acceso básico,1 usuario,5GB almacenamiento', 1, 5, 0, 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE tipo_plan = 'BASIC');

INSERT INTO planes (nombre, tipo_plan, precio_mensual, descripcion, caracteristicas, max_usuarios, almacenamiento_gb, soporte_prioritario, activo, orden_visualizacion, fecha_creacion)
SELECT 'Plan Premium', 'PREMIUM', 29.99, 'Para equipos en crecimiento', 'Todas las funciones básicas,5 usuarios,50GB almacenamiento,Soporte prioritario', 5, 50, 1, 1, 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE tipo_plan = 'PREMIUM');

INSERT INTO planes (nombre, tipo_plan, precio_mensual, descripcion, caracteristicas, max_usuarios, almacenamiento_gb, soporte_prioritario, activo, orden_visualizacion, fecha_creacion)
SELECT 'Plan Enterprise', 'ENTERPRISE', 99.99, 'Solución empresarial completa', 'Todas las funciones premium,Usuarios ilimitados,500GB almacenamiento,Soporte 24/7,API acceso', NULL, 500, 1, 1, 3, NOW()
WHERE NOT EXISTS (SELECT 1 FROM planes WHERE tipo_plan = 'ENTERPRISE');
