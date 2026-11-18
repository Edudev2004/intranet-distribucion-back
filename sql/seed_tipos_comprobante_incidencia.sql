-- Datos iniciales de los catálogos de RF-12 (comprobantes) y RF-17 (incidencias).
-- Se puede ejecutar varias veces: solo inserta los nombres que aún no existen.
-- Requiere las tablas de newdb.sql.
--
-- Uso con psql:
--   psql -h <host> -p <puerto> -U <usuario> -d <base> -v ON_ERROR_STOP=1 -f sql/seed_tipos_comprobante_incidencia.sql
-- También se puede pegar completo en el editor SQL de Supabase.

SET client_encoding = 'UTF8';

BEGIN;

-- El backend reconoce estos tipos por su nombre: "Factura", "Boleta" y "Guía..."
INSERT INTO public.tipo_comprobante (nombre)
SELECT nuevo.nombre
FROM (VALUES
    ('Factura'),
    ('Boleta'),
    ('Guía de Remisión')
) AS nuevo(nombre)
WHERE NOT EXISTS (
    SELECT 1
    FROM public.tipo_comprobante actual
    WHERE LOWER(actual.nombre) = LOWER(nuevo.nombre)
);

INSERT INTO public.tipo_incidencia (nombre)
SELECT nuevo.nombre
FROM (VALUES
    ('Rechazo'),
    ('Dirección errada'),
    ('Cliente ausente'),
    ('Local cerrado'),
    ('Mercadería dañada'),
    ('Otro')
) AS nuevo(nombre)
WHERE NOT EXISTS (
    SELECT 1
    FROM public.tipo_incidencia actual
    WHERE LOWER(actual.nombre) = LOWER(nuevo.nombre)
);

COMMIT;
