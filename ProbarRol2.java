import java.io.IOException;

/** Programa sencillo para comprobar el modulo de la Persona 2. */
public class ProbarRol2 {
    public static void main(String[] args) {
        String rutaAps = args.length > 0 ? args[0] : "datos/aps.csv";
        String rutaConexiones = args.length > 1 ? args[1] : "datos/conexiones.txt";

        LectorArchivos lector = new LectorArchivos();
        try {
            LectorArchivos.DatosRed datos = lector.cargarDatos(rutaAps, rutaConexiones);

            System.out.println("=== ACCESS POINTS CARGADOS ===");
            datos.getAccessPoints().values().forEach(System.out::println);

            System.out.println("\n=== CONEXIONES CARGADAS ===");
            datos.getConexiones().forEach(System.out::println);

            System.out.println("\nResumen: " + datos.getAccessPoints().size()
                    + " AP y " + datos.getConexiones().size() + " conexiones validas.");

            if (!lector.getErrores().isEmpty()) {
                System.out.println("\n=== LINEAS OMITIDAS POR ERRORES ===");
                lector.getErrores().forEach(error -> System.out.println("- " + error));
            }
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("No fue posible cargar los datos: " + error.getMessage());
        }
    }
}
