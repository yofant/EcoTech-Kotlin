SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `activos`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `activos` (
  `id_activo` int(11) NOT NULL AUTO_INCREMENT,
  `codigo_qr` varchar(100) NOT NULL,
  `nombre_activo` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `marca` varchar(50) DEFAULT NULL,
  `modelo` varchar(50) DEFAULT NULL,
  `numero_serie` varchar(100) DEFAULT NULL,
  `fecha_adquisicion` date DEFAULT NULL,
  `valor_compra` decimal(12,2) DEFAULT NULL,
  `vida_util_anios` int(11) DEFAULT NULL,
  `id_categoria` int(11) DEFAULT NULL,
  `id_estado` int(11) DEFAULT NULL,
  `id_ubicacion` int(11) DEFAULT NULL,
  `id_empresa` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_activo`),
  UNIQUE KEY `codigo_qr` (`codigo_qr`),
  KEY `id_categoria` (`id_categoria`),
  KEY `id_estado` (`id_estado`),
  KEY `id_ubicacion` (`id_ubicacion`),
  KEY `id_empresa` (`id_empresa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `activos` (`id_activo`, `codigo_qr`, `nombre_activo`, `descripcion`, `marca`, `modelo`, `numero_serie`, `fecha_adquisicion`, `valor_compra`, `vida_util_anios`, `id_categoria`, `id_estado`, `id_ubicacion`, `id_empresa`) VALUES
(3, 'Preuba', 'Laptop dell', 'Lapotp delll en buen estado', 'Dell', 'Laptitude ', 'dsfgsdg4542145', '2026-05-01', 50000.00, 2, 1, 1, 5, 1);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `categorias`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `categorias` (
  `id_categoria` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_categoria` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `categorias` (`id_categoria`, `nombre_categoria`, `descripcion`) VALUES
(1, 'Laptops', 'Laptops');

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `estados`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `estados` (
  `id_estado` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL,
  PRIMARY KEY (`id_estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `estados` (`id_estado`, `nombre_estado`, `descripcion`) VALUES
(1, 'Baja', 'Equipo de baja'),
(2, 'Buen estado', 'Equipos en buen estado fisico, sin daños graves en sus partes bitales'),
(3, 'Posibpe reparacion', 'Equipos recibidos en un estdo regular pero con opciones de modificacion par apoder dar un segunda oportunidad'),
(4, 'Excelente', 'Se debe de formatear por temas de seguridad e la imformacion, garantizando al usuario y una correcta eliminacion de su informacion.');

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `historial_activos`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `historial_activos` (
  `id_historial` int(11) NOT NULL AUTO_INCREMENT,
  `id_activo` int(11) NOT NULL,
  `fecha_movimiento` datetime DEFAULT current_timestamp(),
  `estado_anterior` int(11) DEFAULT NULL,
  `nuevo_estado` int(11) DEFAULT NULL,
  `ubicacion_anterior` int(11) DEFAULT NULL,
  `nueva_ubicacion` int(11) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  PRIMARY KEY (`id_historial`),
  KEY `id_activo` (`id_activo`),
  KEY `estado_anterior` (`estado_anterior`),
  KEY `nuevo_estado` (`nuevo_estado`),
  KEY `ubicacion_anterior` (`ubicacion_anterior`),
  KEY `nueva_ubicacion` (`nueva_ubicacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `historial_activos` (`id_historial`, `id_activo`, `fecha_movimiento`, `estado_anterior`, `nuevo_estado`, `ubicacion_anterior`, `nueva_ubicacion`, `observaciones`) VALUES
(1, 3, '2026-05-01 23:38:47', 1, 2, 5, 5, 'Se realiza mantenimiento y cambio de algunas teclas');

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `mantenimientos`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mantenimientos` (
  `id_mantenimiento` int(11) NOT NULL AUTO_INCREMENT,
  `id_activo` int(11) NOT NULL,
  `tipo` enum('Preventivo','Correctivo') NOT NULL,
  `fecha_mantenimiento` date NOT NULL,
  `descripcion` text DEFAULT NULL,
  `responsable` varchar(100) DEFAULT NULL,
  `costo` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id_mantenimiento`),
  KEY `id_activo` (`id_activo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `reportes`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `reportes` (
  `id_reporte` int(11) NOT NULL AUTO_INCREMENT,
  `titulo` varchar(100) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_generacion` datetime DEFAULT current_timestamp(),
  `generado_por` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_reporte`),
  KEY `generado_por` (`generado_por`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `reportes` (`id_reporte`, `titulo`, `descripcion`, `fecha_generacion`, `generado_por`) VALUES
(1, 'PRUEBA', 'Pueba de funcionaiento de crud de reportes', '2026-05-01 20:37:52', 13);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `ubicaciones`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ubicaciones` (
  `id_ubicacion` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_ubicacion` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `id_empresa` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_ubicacion`),
  KEY `id_empresa` (`id_empresa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `ubicaciones` (`id_ubicacion`, `nombre_ubicacion`, `descripcion`, `id_empresa`) VALUES
(5, 'Sanrtander', 'Se reciben 5 equipos en mal estado fisico para posible reparacion ', 1);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `usuarios`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  `primer_apellido` varchar(30) NOT NULL,
  `segundo_apellido` varchar(30) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `usuarios` (`id_usuario`, `nombre`, `primer_apellido`, `segundo_apellido`, `correo`, `contrasena`, `rol`) VALUES
(5, 'Preuba3', 'Preuba3', 'Preuba3', 'Preuba3@gmail.com', '$2y$10$PCQXAr.cUyybgcOK6KR70OnQMV8oGgNGerH.vMay2oZwWJKpvdae.', 'admin'),
(6, 'Preuba4', 'Preuba4', 'Preuba4', 'Preuba4@gmail.com', '$2y$10$l/rhTKodKCZlFUnVbVwvFuyKDdRGBI8SnDYFFCgucctQjI1Jteggu', 'cliente'),
(7, 'Pablo', 'Hernandez', 'Morales', 'pablo.hernandez@gmail.com', '$2y$10$n35TrpjromG/1VFVSQZj5OLB46n8MSt.KGEkWtrXYVSuAB.RocES6', 'admin'),
(8, 'Prueba5', 'Prueba5', 'Prueba5', 'Prueba5@gmail.com', '$2y$10$KgnLpDQAUxKwwpZ.smhL8.fRMRZKnTnn3tktdYQunwItD30RjBKSi', 'operador'),
(10, 'Prueba6', 'Prueba6', 'Prueba6', 'Prueba6@gmail.com', '$2y$10$mcggPciI.MT8eE6R9.Esl.WoynNitBKuYy/ADR3XxM9DA7eq2bzSK', 'cliente'),
(11, 'Prueba7', 'Prueba7', 'Prueba7', 'Prueba7@gmail.com', '$2y$10$kZlkJwa3e8Z6HZokdReauePCsEVStzwtgNZrYMBxglSJBd9//FhpW', 'cliente'),
(12, 'Prueba8', 'Prueba8', 'Prueba8', 'Prueba8@gmail.com', '$2y$10$nbGcltCHrMkDBM8glQliwO43a3sbcgUwqAzf2CLO3y7gvSqH80/1W', 'admin'),
(13, 'Yofan', 'Tellez', 'Garzon', 'yojantellez8@gmail.com', '$2y$10$fFcsqf94DOfsnNq4QdRBIua/A0ZxvAFOgWQVmxwHnaFS9AJrjHGCm', 'admin'),
(14, 'Crud20260501202100', 'Desde', 'Admin', 'crud_admin_20260501202100@ecotech.test', '$2y$10$6M63bBRXTDbaxwE8s4yXj.TqkmraychficMB1sxJmIKY9clvCK3.a', 'cliente'),
(16, 'Cristian', 'Munca', 'Arizona', 'cristian.munca@gmail.com', '$2y$10$LLvoFm51.cwU2Od6XNXNyeea9Ych5GNRslQgzelS36u.rhjAloKjC', 'admin'),
(17, 'Karoline', 'Miranda', 'Sanchez', 'karoline.sanchez@gmail.com', '$2y$10$VDTl/aBuLgIKw3C7PE5xeuIpRLrwtVuohM2lmNf/qTygPP5dDK6Im', 'tecnico'),
(18, 'Guillermo', 'Blanco', 'Garzon', 'guillermo.blanco@gmial.com', '$2y$10$MPB4aRSRCj18X6wga.P8EuLpEdlh0RcShKnlx2HwSsLe5X85QvgYC', 'tecnico'),
(19, 'Pablo', 'Eran', 'Hermanos', 'pablo.eran@gamil.com', '$2y$10$EBbdBdZiEhyzBdvryhBiBO5B9fvY12.bFxHo6Kv65av42y2GyMjES', 'operador'),
(20, 'Yaneth', 'Mendez', 'Garcia', 'yaneth.mendez@gmial.com', '$2y$10$YnXxBlR2qWpiafMFZD/B1O5MVJcyOltG/tFvSeKcaQKM/KXLMk9dy', 'operador');

-- --------------------------------------------------------
-- Claves Foráneas / Relaciones
-- --------------------------------------------------------
ALTER TABLE `activos`
  ADD CONSTRAINT `activos_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id_categoria`),
  ADD CONSTRAINT `activos_ibfk_2` FOREIGN KEY (`id_estado`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `activos_ibfk_3` FOREIGN KEY (`id_ubicacion`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `activos_ibfk_4` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`id_empresa`);

ALTER TABLE `historial_activos`
  ADD CONSTRAINT `historial_activos_ibfk_1` FOREIGN KEY (`id_activo`) REFERENCES `activos` (`id_activo`),
  ADD CONSTRAINT `historial_activos_ibfk_2` FOREIGN KEY (`estado_anterior`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `historial_activos_ibfk_3` FOREIGN KEY (`nuevo_estado`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `historial_activos_ibfk_4` FOREIGN KEY (`ubicacion_anterior`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `historial_activos_ibfk_5` FOREIGN KEY (`nueva_ubicacion`) REFERENCES `ubicaciones` (`id_ubicacion`);

ALTER TABLE `mantenimientos`
  ADD CONSTRAINT `mantenimientos_ibfk_1` FOREIGN KEY (`id_activo`) REFERENCES `activos` (`id_activo`);

ALTER TABLE `reportes`
  ADD CONSTRAINT `reportes_ibfk_1` FOREIGN KEY (`generado_por`) REFERENCES `usuarios` (`id_usuario`);

ALTER TABLE `ubicaciones`
  ADD CONSTRAINT `ubicaciones_ibfk_1` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`id_empresa`);

SET FOREIGN_KEY_CHECKS = 1;
