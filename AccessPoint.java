import java.util.Objects;

/**
 * Representa un punto de acceso (Access Point) de la red inalambrica.
 */
public class AccessPoint {
    private final String id;
    private final String ubicacion;

    public AccessPoint(String id, String ubicacion) {
        this.id = validarTexto(id, "identificador").toUpperCase();
        this.ubicacion = validarTexto(ubicacion, "ubicacion");
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacio.");
        }
        return valor.trim();
    }

    public String getId() {
        return id;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    @Override
    public String toString() {
        return id + ";" + ubicacion;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof AccessPoint)) return false;
        AccessPoint otro = (AccessPoint) objeto;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
