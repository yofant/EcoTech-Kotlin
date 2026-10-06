-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 03-05-2026 a las 18:44:00
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `ecotech`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `activos`
--

CREATE TABLE `activos` (
  `id_activo` int(11) NOT NULL,
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
  `id_empresa` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `activos`
--

INSERT INTO `activos` (`id_activo`, `codigo_qr`, `nombre_activo`, `descripcion`, `marca`, `modelo`, `numero_serie`, `fecha_adquisicion`, `valor_compra`, `vida_util_anios`, `id_categoria`, `id_estado`, `id_ubicacion`, `id_empresa`) VALUES
(3, 'Preuba', 'Laptop dell', 'Lapotp delll en buen estado', 'Dell', 'Laptitude ', 'dsfgsdg4542145', '2026-05-01', 50000.00, 2, 1, 1, 5, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id_categoria` int(11) NOT NULL,
  `nombre_categoria` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id_categoria`, `nombre_categoria`, `descripcion`) VALUES
(1, 'Laptops', 'Laptops');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empresas`
--

CREATE TABLE `empresas` (
  `id_empresa` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `nit` varchar(30) NOT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo_contacto` varchar(100) DEFAULT NULL,
  `fecha_registro` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `empresas`
--

INSERT INTO `empresas` (`id_empresa`, `nombre`, `nit`, `direccion`, `telefono`, `correo_contacto`, `fecha_registro`) VALUES
(1, 'Tecnomundo', '12345264', 'Calle 45 # 32 - 84 sur', '2354157', 'tecnomundo@gmail.com', '2026-05-01'),
(2, 'Telecomunicaciones S.A.S', '12255', 'Calle 185 # 65b - 10', '328578', 'Telecomunicacionescol@gmail.com', '2026-05-01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estados`
--

CREATE TABLE `estados` (
  `id_estado` int(11) NOT NULL,
  `nombre_estado` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estados`
--

INSERT INTO `estados` (`id_estado`, `nombre_estado`, `descripcion`) VALUES
(1, 'Baja', 'Equipo de baja'),
(2, 'Buen estado', 'Equipos en buen estado fisico, sin daños graves en sus partes bitales'),
(3, 'Posibpe reparacion', 'Equipos recibidos en un estdo regular pero con opciones de modificacion par apoder dar un segunda oportunidad'),
(4, 'Excelente', 'Se debe de formatear por temas de seguridad e la imformacion, garantizando al usuario y una correcta eliminacion de su informacion.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historial_activos`
--

CREATE TABLE `historial_activos` (
  `id_historial` int(11) NOT NULL,
  `id_activo` int(11) NOT NULL,
  `fecha_movimiento` datetime DEFAULT current_timestamp(),
  `estado_anterior` int(11) DEFAULT NULL,
  `nuevo_estado` int(11) DEFAULT NULL,
  `ubicacion_anterior` int(11) DEFAULT NULL,
  `nueva_ubicacion` int(11) DEFAULT NULL,
  `observaciones` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `historial_activos`
--

INSERT INTO `historial_activos` (`id_historial`, `id_activo`, `fecha_movimiento`, `estado_anterior`, `nuevo_estado`, `ubicacion_anterior`, `nueva_ubicacion`, `observaciones`) VALUES
(1, 3, '2026-05-01 23:38:47', 1, 2, 5, 5, 'Se realiza mantenimiento y cambio de algunas teclas');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `mantenimientos`
--

CREATE TABLE `mantenimientos` (
  `id_mantenimiento` int(11) NOT NULL,
  `id_activo` int(11) NOT NULL,
  `tipo` enum('Preventivo','Correctivo') NOT NULL,
  `fecha_mantenimiento` date NOT NULL,
  `descripcion` text DEFAULT NULL,
  `responsable` varchar(100) DEFAULT NULL,
  `costo` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reportes`
--

CREATE TABLE `reportes` (
  `id_reporte` int(11) NOT NULL,
  `titulo` varchar(100) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_generacion` datetime DEFAULT current_timestamp(),
  `generado_por` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `reportes`
--

INSERT INTO `reportes` (`id_reporte`, `titulo`, `descripcion`, `fecha_generacion`, `generado_por`) VALUES
(1, 'PRUEBA', 'Pueba de funcionaiento de crud de reportes', '2026-05-01 20:37:52', 13);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ubicaciones`
--

CREATE TABLE `ubicaciones` (
  `id_ubicacion` int(11) NOT NULL,
  `nombre_ubicacion` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `id_empresa` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `ubicaciones`
--

INSERT INTO `ubicaciones` (`id_ubicacion`, `nombre_ubicacion`, `descripcion`, `id_empresa`) VALUES
(5, 'Sanrtander', 'Se reciben 5 equipos en mal estado fisico para posible reparacion ', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre` varchar(30) NOT NULL,
  `primer_apellido` varchar(30) NOT NULL,
  `segundo_apellido` varchar(30) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

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

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `activos`
--
ALTER TABLE `activos`
  ADD PRIMARY KEY (`id_activo`),
  ADD UNIQUE KEY `codigo_qr` (`codigo_qr`),
  ADD KEY `id_categoria` (`id_categoria`),
  ADD KEY `id_estado` (`id_estado`),
  ADD KEY `id_ubicacion` (`id_ubicacion`),
  ADD KEY `id_empresa` (`id_empresa`);

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id_categoria`);

--
-- Indices de la tabla `empresas`
--
ALTER TABLE `empresas`
  ADD PRIMARY KEY (`id_empresa`),
  ADD UNIQUE KEY `nit` (`nit`);

--
-- Indices de la tabla `estados`
--
ALTER TABLE `estados`
  ADD PRIMARY KEY (`id_estado`);

--
-- Indices de la tabla `historial_activos`
--
ALTER TABLE `historial_activos`
  ADD PRIMARY KEY (`id_historial`),
  ADD KEY `id_activo` (`id_activo`),
  ADD KEY `estado_anterior` (`estado_anterior`),
  ADD KEY `nuevo_estado` (`nuevo_estado`),
  ADD KEY `ubicacion_anterior` (`ubicacion_anterior`),
  ADD KEY `nueva_ubicacion` (`nueva_ubicacion`);

--
-- Indices de la tabla `mantenimientos`
--
ALTER TABLE `mantenimientos`
  ADD PRIMARY KEY (`id_mantenimiento`),
  ADD KEY `id_activo` (`id_activo`);

--
-- Indices de la tabla `reportes`
--
ALTER TABLE `reportes`
  ADD PRIMARY KEY (`id_reporte`),
  ADD KEY `generado_por` (`generado_por`);

--
-- Indices de la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  ADD PRIMARY KEY (`id_ubicacion`),
  ADD KEY `id_empresa` (`id_empresa`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `correo` (`correo`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `activos`
--
ALTER TABLE `activos`
  MODIFY `id_activo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `empresas`
--
ALTER TABLE `empresas`
  MODIFY `id_empresa` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `estados`
--
ALTER TABLE `estados`
  MODIFY `id_estado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `historial_activos`
--
ALTER TABLE `historial_activos`
  MODIFY `id_historial` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `mantenimientos`
--
ALTER TABLE `mantenimientos`
  MODIFY `id_mantenimiento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `reportes`
--
ALTER TABLE `reportes`
  MODIFY `id_reporte` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  MODIFY `id_ubicacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `activos`
--
ALTER TABLE `activos`
  ADD CONSTRAINT `activos_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id_categoria`),
  ADD CONSTRAINT `activos_ibfk_2` FOREIGN KEY (`id_estado`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `activos_ibfk_3` FOREIGN KEY (`id_ubicacion`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `activos_ibfk_4` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`id_empresa`);

--
-- Filtros para la tabla `historial_activos`
--
ALTER TABLE `historial_activos`
  ADD CONSTRAINT `historial_activos_ibfk_1` FOREIGN KEY (`id_activo`) REFERENCES `activos` (`id_activo`),
  ADD CONSTRAINT `historial_activos_ibfk_2` FOREIGN KEY (`estado_anterior`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `historial_activos_ibfk_3` FOREIGN KEY (`nuevo_estado`) REFERENCES `estados` (`id_estado`),
  ADD CONSTRAINT `historial_activos_ibfk_4` FOREIGN KEY (`ubicacion_anterior`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `historial_activos_ibfk_5` FOREIGN KEY (`nueva_ubicacion`) REFERENCES `ubicaciones` (`id_ubicacion`);

--
-- Filtros para la tabla `mantenimientos`
--
ALTER TABLE `mantenimientos`
  ADD CONSTRAINT `mantenimientos_ibfk_1` FOREIGN KEY (`id_activo`) REFERENCES `activos` (`id_activo`);

--
-- Filtros para la tabla `reportes`
--
ALTER TABLE `reportes`
  ADD CONSTRAINT `reportes_ibfk_1` FOREIGN KEY (`generado_por`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  ADD CONSTRAINT `ubicaciones_ibfk_1` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`id_empresa`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
