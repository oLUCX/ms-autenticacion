package com.bodeganube.autenticacion.model;

/**
 * Roles definidos en el caso BodegaNube: el operario de bodega gestiona picking y despacho,
 * el comercio solo consulta sus propias ordenes (Tenant Isolation, RF-06).
 */
public enum Rol {
    OPERARIO,
    COMERCIO
}
