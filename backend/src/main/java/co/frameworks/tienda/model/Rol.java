package co.frameworks.tienda.model;

/**
 * Enumeración que define los roles de autorización en el sistema Tienda.
 * Se persiste en la base de datos PostgreSQL como String (EnumType.STRING)
 * para asegurar legibilidad, consistencia y robustez en migraciones.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}
