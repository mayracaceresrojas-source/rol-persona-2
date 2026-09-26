import java.util.Objects;

/**
 * Representa la conexion de un dispositivo a un Access Point.
 */
public class Conexion {
    private final AccessPoint accessPoint;
    private final String direccionMac;

    public Conexion(AccessPoint accessPoint, String direccionMac) {
        if (accessPoint == null) {
            throw new IllegalArgumentException("El Access Point no puede ser nulo.");
        }
        if (direccionMac == null || direccionMac.trim().isEmpty()) {
            throw new IllegalArgumentException("La direccion MAC no puede estar vacia.");
        }
        this.accessPoint = accessPoint;
        this.direccionMac = direccionMac.trim().toUpperCase();
    }

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    public String getAccessPointId() {
        return accessPoint.getId();
    }

    public String getDireccionMac() {
        return direccionMac;
    }

    @Override
    public String toString() {
        return accessPoint.getId() + ";" + direccionMac;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Conexion)) return false;
        Conexion otra = (Conexion) objeto;
        return accessPoint.equals(otra.accessPoint)
                && direccionMac.equals(otra.direccionMac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessPoint, direccionMac);
    }
}
