-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.rol (
  id_rol smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  descripcion character varying,
  CONSTRAINT rol_pkey PRIMARY KEY (id_rol)
);
CREATE TABLE public.modulo (
  id_modulo smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT modulo_pkey PRIMARY KEY (id_modulo)
);
CREATE TABLE public.accion (
  id_accion smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT accion_pkey PRIMARY KEY (id_accion)
);
CREATE TABLE public.permiso (
  id_permiso smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_modulo smallint NOT NULL,
  id_accion smallint NOT NULL,
  CONSTRAINT permiso_pkey PRIMARY KEY (id_permiso),
  CONSTRAINT permiso_id_modulo_fkey FOREIGN KEY (id_modulo) REFERENCES public.modulo(id_modulo),
  CONSTRAINT permiso_id_accion_fkey FOREIGN KEY (id_accion) REFERENCES public.accion(id_accion)
);
CREATE TABLE public.rol_permiso (
  id_rol smallint NOT NULL,
  id_permiso smallint NOT NULL,
  CONSTRAINT rol_permiso_pkey PRIMARY KEY (id_rol, id_permiso),
  CONSTRAINT rol_permiso_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol),
  CONSTRAINT rol_permiso_id_permiso_fkey FOREIGN KEY (id_permiso) REFERENCES public.permiso(id_permiso)
);
CREATE TABLE public.sede (
  id_sede integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  direccion character varying NOT NULL,
  id_responsable integer,
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT sede_pkey PRIMARY KEY (id_sede),
  CONSTRAINT fk_sede_responsable FOREIGN KEY (id_responsable) REFERENCES public.usuario(id_usuario)
);
CREATE TABLE public.usuario (
  id_usuario integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombres character varying NOT NULL,
  apellidos character varying NOT NULL,
  username character varying NOT NULL UNIQUE,
  correo character varying NOT NULL,
  password_hash character varying NOT NULL,
  id_rol smallint NOT NULL,
  id_sede integer NOT NULL,
  activo boolean NOT NULL DEFAULT true,
  intentos_fallidos smallint NOT NULL DEFAULT 0,
  bloqueado boolean NOT NULL DEFAULT false,
  fecha_bloqueo timestamp with time zone,
  ultimo_acceso timestamp with time zone,
  creado_en timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario),
  CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol),
  CONSTRAINT usuario_id_sede_fkey FOREIGN KEY (id_sede) REFERENCES public.sede(id_sede)
);
CREATE TABLE public.tipo_almacen (
  id_tipo_almacen smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT tipo_almacen_pkey PRIMARY KEY (id_tipo_almacen)
);
CREATE TABLE public.almacen (
  id_almacen integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_sede integer NOT NULL,
  nombre character varying NOT NULL,
  id_tipo_almacen smallint NOT NULL,
  capacidad_maxima numeric NOT NULL CHECK (capacidad_maxima > 0::numeric),
  ubicacion character varying,
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT almacen_pkey PRIMARY KEY (id_almacen),
  CONSTRAINT almacen_id_sede_fkey FOREIGN KEY (id_sede) REFERENCES public.sede(id_sede),
  CONSTRAINT almacen_id_tipo_almacen_fkey FOREIGN KEY (id_tipo_almacen) REFERENCES public.tipo_almacen(id_tipo_almacen)
);
CREATE TABLE public.categoria (
  id_categoria integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  descripcion character varying,
  CONSTRAINT categoria_pkey PRIMARY KEY (id_categoria)
);
CREATE TABLE public.unidad_medida (
  id_unidad smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  codigo character varying NOT NULL UNIQUE,
  nombre character varying NOT NULL,
  CONSTRAINT unidad_medida_pkey PRIMARY KEY (id_unidad)
);
CREATE TABLE public.producto (
  id_producto integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  codigo character varying NOT NULL UNIQUE,
  nombre character varying NOT NULL,
  id_categoria integer NOT NULL,
  id_unidad smallint NOT NULL,
  stock_minimo numeric NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0::numeric),
  peso_kg numeric NOT NULL DEFAULT 0 CHECK (peso_kg >= 0::numeric),
  precio_unitario numeric NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0::numeric),
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT producto_pkey PRIMARY KEY (id_producto),
  CONSTRAINT producto_id_categoria_fkey FOREIGN KEY (id_categoria) REFERENCES public.categoria(id_categoria),
  CONSTRAINT producto_id_unidad_fkey FOREIGN KEY (id_unidad) REFERENCES public.unidad_medida(id_unidad)
);
CREATE TABLE public.inventario (
  id_almacen integer NOT NULL,
  id_producto integer NOT NULL,
  stock_actual numeric NOT NULL DEFAULT 0 CHECK (stock_actual >= 0::numeric),
  CONSTRAINT inventario_pkey PRIMARY KEY (id_almacen, id_producto),
  CONSTRAINT inventario_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacen(id_almacen),
  CONSTRAINT inventario_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto)
);
CREATE TABLE public.tipo_movimiento (
  id_tipo_movimiento smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  naturaleza character NOT NULL CHECK (naturaleza = ANY (ARRAY['E'::bpchar, 'S'::bpchar])),
  CONSTRAINT tipo_movimiento_pkey PRIMARY KEY (id_tipo_movimiento)
);
CREATE TABLE public.movimiento_inventario (
  id_movimiento bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_almacen integer NOT NULL,
  id_tipo_movimiento smallint NOT NULL,
  id_usuario integer NOT NULL,
  id_pedido bigint,
  fecha timestamp with time zone NOT NULL DEFAULT now(),
  observacion character varying,
  CONSTRAINT movimiento_inventario_pkey PRIMARY KEY (id_movimiento),
  CONSTRAINT fk_mov_pedido FOREIGN KEY (id_pedido) REFERENCES public.pedido(id_pedido),
  CONSTRAINT movimiento_inventario_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacen(id_almacen),
  CONSTRAINT movimiento_inventario_id_tipo_movimiento_fkey FOREIGN KEY (id_tipo_movimiento) REFERENCES public.tipo_movimiento(id_tipo_movimiento),
  CONSTRAINT movimiento_inventario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE TABLE public.detalle_movimiento (
  id_movimiento bigint NOT NULL,
  id_producto integer NOT NULL,
  cantidad numeric NOT NULL CHECK (cantidad > 0::numeric),
  CONSTRAINT detalle_movimiento_pkey PRIMARY KEY (id_movimiento, id_producto),
  CONSTRAINT detalle_movimiento_id_movimiento_fkey FOREIGN KEY (id_movimiento) REFERENCES public.movimiento_inventario(id_movimiento),
  CONSTRAINT detalle_movimiento_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto)
);
CREATE TABLE public.zona (
  id_zona integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  descripcion character varying,
  CONSTRAINT zona_pkey PRIMARY KEY (id_zona)
);
CREATE TABLE public.tipo_documento (
  id_tipo_documento smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT tipo_documento_pkey PRIMARY KEY (id_tipo_documento)
);
CREATE TABLE public.cliente (
  id_cliente integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_tipo_documento smallint NOT NULL,
  numero_documento character varying NOT NULL,
  nombre_razon_social character varying NOT NULL,
  direccion character varying NOT NULL,
  telefono character varying,
  correo character varying,
  id_zona integer,
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT cliente_pkey PRIMARY KEY (id_cliente),
  CONSTRAINT cliente_id_tipo_documento_fkey FOREIGN KEY (id_tipo_documento) REFERENCES public.tipo_documento(id_tipo_documento),
  CONSTRAINT cliente_id_zona_fkey FOREIGN KEY (id_zona) REFERENCES public.zona(id_zona)
);
CREATE TABLE public.estado_pedido (
  id_estado_pedido smallint NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT estado_pedido_pkey PRIMARY KEY (id_estado_pedido)
);
CREATE TABLE public.pedido (
  id_pedido bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_cliente integer NOT NULL,
  id_almacen integer NOT NULL,
  id_usuario_registra integer NOT NULL,
  id_estado_pedido smallint NOT NULL DEFAULT 1,
  fecha_pedido timestamp with time zone NOT NULL DEFAULT now(),
  fecha_entrega_solicitada date,
  observacion character varying,
  CONSTRAINT pedido_pkey PRIMARY KEY (id_pedido),
  CONSTRAINT pedido_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente),
  CONSTRAINT pedido_id_almacen_fkey FOREIGN KEY (id_almacen) REFERENCES public.almacen(id_almacen),
  CONSTRAINT pedido_id_usuario_registra_fkey FOREIGN KEY (id_usuario_registra) REFERENCES public.usuario(id_usuario),
  CONSTRAINT pedido_id_estado_pedido_fkey FOREIGN KEY (id_estado_pedido) REFERENCES public.estado_pedido(id_estado_pedido)
);
CREATE TABLE public.detalle_pedido (
  id_pedido bigint NOT NULL,
  id_producto integer NOT NULL,
  cantidad numeric NOT NULL CHECK (cantidad > 0::numeric),
  precio_unitario numeric NOT NULL CHECK (precio_unitario >= 0::numeric),
  CONSTRAINT detalle_pedido_pkey PRIMARY KEY (id_pedido, id_producto),
  CONSTRAINT detalle_pedido_id_pedido_fkey FOREIGN KEY (id_pedido) REFERENCES public.pedido(id_pedido),
  CONSTRAINT detalle_pedido_id_producto_fkey FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto)
);
CREATE TABLE public.aprobacion_pedido (
  id_aprobacion bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_pedido bigint NOT NULL UNIQUE,
  id_usuario integer NOT NULL,
  decision character varying NOT NULL CHECK (decision::text = ANY (ARRAY['APROBADO'::character varying, 'RECHAZADO'::character varying]::text[])),
  motivo character varying,
  fecha timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT aprobacion_pedido_pkey PRIMARY KEY (id_aprobacion),
  CONSTRAINT aprobacion_pedido_id_pedido_fkey FOREIGN KEY (id_pedido) REFERENCES public.pedido(id_pedido),
  CONSTRAINT aprobacion_pedido_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE TABLE public.tipo_comprobante (
  id_tipo_comprobante smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT tipo_comprobante_pkey PRIMARY KEY (id_tipo_comprobante)
);
CREATE TABLE public.comprobante (
  id_comprobante bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_pedido bigint NOT NULL,
  id_tipo_comprobante smallint NOT NULL,
  serie character varying NOT NULL,
  numero integer NOT NULL,
  fecha_emision timestamp with time zone NOT NULL DEFAULT now(),
  id_usuario integer NOT NULL,
  CONSTRAINT comprobante_pkey PRIMARY KEY (id_comprobante),
  CONSTRAINT comprobante_id_pedido_fkey FOREIGN KEY (id_pedido) REFERENCES public.pedido(id_pedido),
  CONSTRAINT comprobante_id_tipo_comprobante_fkey FOREIGN KEY (id_tipo_comprobante) REFERENCES public.tipo_comprobante(id_tipo_comprobante),
  CONSTRAINT comprobante_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE TABLE public.estado_vehiculo (
  id_estado_vehiculo smallint NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT estado_vehiculo_pkey PRIMARY KEY (id_estado_vehiculo)
);
CREATE TABLE public.vehiculo (
  id_vehiculo integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  placa character varying NOT NULL UNIQUE,
  marca character varying,
  modelo character varying,
  capacidad_kg numeric NOT NULL CHECK (capacidad_kg > 0::numeric),
  id_estado_vehiculo smallint NOT NULL DEFAULT 1,
  CONSTRAINT vehiculo_pkey PRIMARY KEY (id_vehiculo),
  CONSTRAINT vehiculo_id_estado_vehiculo_fkey FOREIGN KEY (id_estado_vehiculo) REFERENCES public.estado_vehiculo(id_estado_vehiculo)
);
CREATE TABLE public.conductor (
  id_conductor integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_usuario integer UNIQUE,
  nombres character varying NOT NULL,
  apellidos character varying NOT NULL,
  dni character NOT NULL UNIQUE,
  nro_licencia character varying NOT NULL UNIQUE,
  categoria_licencia character varying NOT NULL,
  fecha_venc_licencia date NOT NULL,
  telefono character varying,
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT conductor_pkey PRIMARY KEY (id_conductor),
  CONSTRAINT conductor_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE TABLE public.tipo_mantenimiento (
  id_tipo_mantenimiento smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT tipo_mantenimiento_pkey PRIMARY KEY (id_tipo_mantenimiento)
);
CREATE TABLE public.mantenimiento_vehiculo (
  id_mantenimiento integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_vehiculo integer NOT NULL,
  id_tipo_mantenimiento smallint NOT NULL,
  fecha_inicio date NOT NULL,
  fecha_fin date NOT NULL,
  descripcion character varying,
  costo numeric CHECK (costo >= 0::numeric),
  CONSTRAINT mantenimiento_vehiculo_pkey PRIMARY KEY (id_mantenimiento),
  CONSTRAINT mantenimiento_vehiculo_id_vehiculo_fkey FOREIGN KEY (id_vehiculo) REFERENCES public.vehiculo(id_vehiculo),
  CONSTRAINT mantenimiento_vehiculo_id_tipo_mantenimiento_fkey FOREIGN KEY (id_tipo_mantenimiento) REFERENCES public.tipo_mantenimiento(id_tipo_mantenimiento)
);
CREATE TABLE public.ruta (
  id_ruta integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL,
  id_zona integer NOT NULL,
  activo boolean NOT NULL DEFAULT true,
  CONSTRAINT ruta_pkey PRIMARY KEY (id_ruta),
  CONSTRAINT ruta_id_zona_fkey FOREIGN KEY (id_zona) REFERENCES public.zona(id_zona)
);
CREATE TABLE public.estado_programacion (
  id_estado_programacion smallint NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT estado_programacion_pkey PRIMARY KEY (id_estado_programacion)
);
CREATE TABLE public.programacion_ruta (
  id_programacion integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_ruta integer NOT NULL,
  id_vehiculo integer NOT NULL,
  id_conductor integer NOT NULL,
  inicio_programado timestamp with time zone NOT NULL,
  fin_programado timestamp with time zone NOT NULL,
  inicio_real timestamp with time zone,
  fin_real timestamp with time zone,
  id_estado_programacion smallint NOT NULL DEFAULT 1,
  CONSTRAINT programacion_ruta_pkey PRIMARY KEY (id_programacion),
  CONSTRAINT programacion_ruta_id_ruta_fkey FOREIGN KEY (id_ruta) REFERENCES public.ruta(id_ruta),
  CONSTRAINT programacion_ruta_id_vehiculo_fkey FOREIGN KEY (id_vehiculo) REFERENCES public.vehiculo(id_vehiculo),
  CONSTRAINT programacion_ruta_id_conductor_fkey FOREIGN KEY (id_conductor) REFERENCES public.conductor(id_conductor),
  CONSTRAINT programacion_ruta_id_estado_programacion_fkey FOREIGN KEY (id_estado_programacion) REFERENCES public.estado_programacion(id_estado_programacion)
);
CREATE TABLE public.estado_entrega (
  id_estado_entrega smallint NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT estado_entrega_pkey PRIMARY KEY (id_estado_entrega)
);
CREATE TABLE public.entrega (
  id_entrega bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_programacion integer NOT NULL,
  id_pedido bigint NOT NULL,
  id_estado_entrega smallint NOT NULL DEFAULT 1,
  orden_visita smallint,
  hora_estimada timestamp with time zone,
  hora_entrega_real timestamp with time zone,
  CONSTRAINT entrega_pkey PRIMARY KEY (id_entrega),
  CONSTRAINT entrega_id_programacion_fkey FOREIGN KEY (id_programacion) REFERENCES public.programacion_ruta(id_programacion),
  CONSTRAINT entrega_id_pedido_fkey FOREIGN KEY (id_pedido) REFERENCES public.pedido(id_pedido),
  CONSTRAINT entrega_id_estado_entrega_fkey FOREIGN KEY (id_estado_entrega) REFERENCES public.estado_entrega(id_estado_entrega)
);
CREATE TABLE public.tipo_incidencia (
  id_tipo_incidencia smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
  nombre character varying NOT NULL UNIQUE,
  CONSTRAINT tipo_incidencia_pkey PRIMARY KEY (id_tipo_incidencia)
);
CREATE TABLE public.incidencia (
  id_incidencia bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_entrega bigint NOT NULL,
  id_tipo_incidencia smallint NOT NULL,
  comentario character varying,
  fecha_hora timestamp with time zone NOT NULL DEFAULT now(),
  id_usuario integer NOT NULL,
  CONSTRAINT incidencia_pkey PRIMARY KEY (id_incidencia),
  CONSTRAINT incidencia_id_entrega_fkey FOREIGN KEY (id_entrega) REFERENCES public.entrega(id_entrega),
  CONSTRAINT incidencia_id_tipo_incidencia_fkey FOREIGN KEY (id_tipo_incidencia) REFERENCES public.tipo_incidencia(id_tipo_incidencia),
  CONSTRAINT incidencia_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);