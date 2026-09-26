import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee aps.csv y conexiones.txt, valida sus lineas y convierte los datos
 * en objetos AccessPoint y Conexion.
 */
public class LectorArchivos {
    private final List<String> errores = new ArrayList<>();

    /** Lee los AP y los guarda por identificador, conservando el orden. */
    public Map<String, AccessPoint> leerAccessPoints(String rutaArchivo) throws IOException {
        errores.clear();
        Map<String, AccessPoint> accessPoints = new LinkedHashMap<>();
        Path ruta = Paths.get(rutaArchivo);
        validarArchivo(ruta);

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                linea = limpiarLinea(linea);
                if (ignorarLinea(linea)) continue;

                String[] partes = linea.split(";", -1);
                if (partes.length != 2) {
                    registrarError(ruta, numeroLinea,
                            "se esperaban 2 campos: id;ubicacion", linea);
                    continue;
                }

                String id = partes[0].trim().toUpperCase();
                String ubicacion = partes[1].trim();
                if (id.isEmpty() || ubicacion.isEmpty()) {
                    registrarError(ruta, numeroLinea, "hay campos vacios", linea);
                    continue;
                }
                if (esEncabezadoAp(id, ubicacion)) continue;
                if (accessPoints.containsKey(id)) {
                    registrarError(ruta, numeroLinea,
                            "identificador de AP repetido: " + id, linea);
                    continue;
                }

                accessPoints.put(id, new AccessPoint(id, ubicacion));
            }
        }
        return accessPoints;
    }

    /**
     * Lee conexiones y verifica que cada identificador exista en el mapa de AP.
     * No elimina MAC repetidas, pues ese conteo corresponde al rol 3.
     */
    public List<Conexion> leerConexiones(String rutaArchivo,
                                         Map<String, AccessPoint> accessPoints)
            throws IOException {
        if (accessPoints == null) {
            throw new IllegalArgumentException("El mapa de Access Points no puede ser nulo.");
        }

        List<Conexion> conexiones = new ArrayList<>();
        Path ruta = Paths.get(rutaArchivo);
        validarArchivo(ruta);

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                linea = limpiarLinea(linea);
                if (ignorarLinea(linea)) continue;

                String[] partes = linea.split(";", -1);
                if (partes.length != 2) {
                    registrarError(ruta, numeroLinea,
                            "se esperaban 2 campos: idAP;direccionMAC", linea);
                    continue;
                }

                String idAp = partes[0].trim().toUpperCase();
                String mac = partes[1].trim().toUpperCase();
                if (idAp.isEmpty() || mac.isEmpty()) {
                    registrarError(ruta, numeroLinea, "hay campos vacios", linea);
                    continue;
                }
                if (esEncabezadoConexion(idAp, mac)) continue;
                if (!esMacValida(mac)) {
                    registrarError(ruta, numeroLinea,
                            "direccion MAC no valida", linea);
                    continue;
                }

                AccessPoint ap = accessPoints.get(idAp);
                if (ap == null) {
                    registrarError(ruta, numeroLinea,
                            "el Access Point " + idAp + " no existe en aps.csv", linea);
                    continue;
                }

                conexiones.add(new Conexion(ap, mac));
            }
        }
        return conexiones;
    }

    /** Metodo practico que carga ambos archivos en una sola llamada. */
    public DatosRed cargarDatos(String rutaAps, String rutaConexiones) throws IOException {
        Map<String, AccessPoint> aps = leerAccessPoints(rutaAps);
        List<String> erroresAps = new ArrayList<>(errores);
        errores.clear();
        List<Conexion> conexiones = leerConexiones(rutaConexiones, aps);
        List<String> todosLosErrores = new ArrayList<>(erroresAps);
        todosLosErrores.addAll(errores);
        errores.clear();
        errores.addAll(todosLosErrores);
        return new DatosRed(aps, conexiones);
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    private void validarArchivo(Path ruta) throws IOException {
        if (!Files.exists(ruta)) {
            throw new IOException("No se encontro el archivo: " + ruta.toAbsolutePath());
        }
        if (!Files.isRegularFile(ruta) || !Files.isReadable(ruta)) {
            throw new IOException("No se puede leer el archivo: " + ruta.toAbsolutePath());
        }
    }

    private String limpiarLinea(String linea) {
        if (linea == null) return "";
        return linea.replace("\uFEFF", "").trim();
    }

    private boolean ignorarLinea(String linea) {
        return linea.isEmpty() || linea.startsWith("#");
    }

    private boolean esEncabezadoAp(String id, String ubicacion) {
        return (id.equalsIgnoreCase("ID") || id.equalsIgnoreCase("ID_AP")
                || id.equalsIgnoreCase("ACCESS_POINT"))
                && ubicacion.equalsIgnoreCase("UBICACION");
    }

    private boolean esEncabezadoConexion(String id, String mac) {
        return (id.equalsIgnoreCase("ID") || id.equalsIgnoreCase("ID_AP")
                || id.equalsIgnoreCase("ACCESS_POINT"))
                && (mac.equalsIgnoreCase("MAC") || mac.equalsIgnoreCase("DIRECCION_MAC"));
    }

    private boolean esMacValida(String mac) {
        // Formato habitual 00:1A:2B:3C:4D:5E. Tambien admite MAC001 del ejemplo academico.
        return mac.matches("(?i)^[0-9A-F]{2}([:-][0-9A-F]{2}){5}$")
                || mac.matches("(?i)^MAC[0-9A-Z_-]+$");
    }

    private void registrarError(Path ruta, int numeroLinea,
                                 String motivo, String contenido) {
        errores.add(ruta.getFileName() + ", linea " + numeroLinea + ": "
                + motivo + " -> [" + contenido + "]");
    }

    /** Contenedor inmutable para entregar al rol 3 los datos ya cargados. */
    public static class DatosRed {
        private final Map<String, AccessPoint> accessPoints;
        private final List<Conexion> conexiones;

        public DatosRed(Map<String, AccessPoint> accessPoints,
                        List<Conexion> conexiones) {
            this.accessPoints = Collections.unmodifiableMap(
                    new LinkedHashMap<>(accessPoints));
            this.conexiones = Collections.unmodifiableList(
                    new ArrayList<>(conexiones));
        }

        public Map<String, AccessPoint> getAccessPoints() {
            return accessPoints;
        }

        public List<Conexion> getConexiones() {
            return conexiones;
        }
    }
}
