import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) {
        String rutaArchivo = "resources/quijote.txt";
        String palabraBuscada = "quijote";
        String secuenciaBuscada = "ción";

        try {
            // 1. CARGA DE DATOS (sin agregar al temporizador)
            System.out.println("Cargando archivo en memoria...");
            String contenidoRaw = Files.readString(Paths.get(rutaArchivo), StandardCharsets.UTF_8);
            String[] palabrasOriginal = contenidoRaw.toLowerCase().split("[^\\p{L}0-9]+");
            System.out.println("Carga lista. Palabras originales: " + palabrasOriginal.length);

            // 2. CREACIÓN DEL TEXTO AMPLIADO (x100)
            System.out.println("Ampliando x100...");
            String[] palabrasAmpliado = crearArregloAmpliado(palabrasOriginal, 100);
            
            // 3. EJECUCIÓN DE LAS PRUEBAS
            System.out.println("\n========== PRUEBAS: TEXTO ORIGINAL ==========");
            ejecutarBateriaDePruebas(palabrasOriginal, palabraBuscada, secuenciaBuscada);

            System.out.println("========== PRUEBAS: TEXTO AMPLIADO (x100) ==========");
            ejecutarBateriaDePruebas(palabrasAmpliado, palabraBuscada, secuenciaBuscada);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo.");
        } catch (InterruptedException e) {
            System.err.println("Hilo interrumpido.");
        }
    }

    private static String[] crearArregloAmpliado(String[] original, int factor) {
        String[] ampliado = new String[original.length * factor];
        for (int i = 0; i < factor; i++) {
            System.arraycopy(original, 0, ampliado, i * original.length, original.length);
        }
        return ampliado;
    }

    /**
     * PRUEBAS
     */
    private static void ejecutarBateriaDePruebas(String[] palabras, String pBuscada, String sBuscada) throws InterruptedException {
        int iteraciones = 5;

        // --- FASE DE VERIFICACIÓN DEL CONTEO ---
        System.out.println("[CONTEOS PARA VERIFICACIÓN]");
        procesarSecuencial(palabras, pBuscada, sBuscada, true); 
        procesarParalelo(palabras, pBuscada, sBuscada, 4, true); 
        System.out.println();

        // --- FASE DE RENDIMIENTO ---
        System.out.println("[RESULTADOS DE TIEMPO PROMEDIO - " + iteraciones + " ITERACIONES]");
        
        // 1. Prueba Secuencial
        long tiempoTotalSecuencial = 0;
        for (int i = 0; i < iteraciones; i++) {
            tiempoTotalSecuencial += procesarSecuencial(palabras, pBuscada, sBuscada, false); 
        }
        double promSecuencial = (tiempoTotalSecuencial / (double) iteraciones) / 1_000_000.0;
        System.out.printf("1 hilo (Secuencial) : %8.4f ms | Aceleración: 1.00x\n", promSecuencial);

        // 2. Prueba Paralela (2 Hilos)
        long tiempoTotal2Hilos = 0;
        for (int i = 0; i < iteraciones; i++) {
            tiempoTotal2Hilos += procesarParalelo(palabras, pBuscada, sBuscada, 2, false);
        }
        double prom2Hilos = (tiempoTotal2Hilos / (double) iteraciones) / 1_000_000.0;
        System.out.printf("2 hilos (Paralelo)  : %8.4f ms | Aceleración: %.2fx\n", prom2Hilos, (promSecuencial/prom2Hilos));

        // 3. Prueba Paralela (4 Hilos)
        long tiempoTotal4Hilos = 0;
        for (int i = 0; i < iteraciones; i++) {
            tiempoTotal4Hilos += procesarParalelo(palabras, pBuscada, sBuscada, 4, false);
        }
        double prom4Hilos = (tiempoTotal4Hilos / (double) iteraciones) / 1_000_000.0;
        System.out.printf("4 hilos (Paralelo)  : %8.4f ms | Aceleración: %.2fx\n", prom4Hilos, (promSecuencial/prom4Hilos));
        System.out.println("-----------------------------------------------------\n");
    }

    private static long procesarSecuencial(String[] palabras, String pBuscada, String sBuscada, boolean imprimirResultados) {
        int totalPalabras = 0, contPalabra = 0, contSecuencia = 0;
        long inicio = System.nanoTime();
        
        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            totalPalabras++;
            if (palabra.equals(pBuscada)) contPalabra++;
            if (palabra.contains(sBuscada)) contSecuencia++;
        }
        
        long fin = System.nanoTime();

        if (imprimirResultados) {
            System.out.println("Conteo Secuencial -> Total: " + totalPalabras + " | Exactas ('" + pBuscada + "'): " + contPalabra + " | Secuencias ('" + sBuscada + "'): " + contSecuencia);
        }

        return (fin - inicio);
    }

    private static long procesarParalelo(String[] palabras, String pBuscada, String sBuscada, int numHilos, boolean imprimirResultados) throws InterruptedException {
        ProcesadorHilo[] hilos = new ProcesadorHilo[numHilos];
        int tamanoPorcion = palabras.length / numHilos;
        long inicio = System.nanoTime();

        for (int i = 0; i < numHilos; i++) {
            int inicioRango = i * tamanoPorcion;
            int finRango = (i == numHilos - 1) ? palabras.length : (i + 1) * tamanoPorcion;
            hilos[i] = new ProcesadorHilo(palabras, inicioRango, finRango, pBuscada, sBuscada);
            hilos[i].start(); 
        }

        int totalPalabras = 0, contPalabra = 0, contSecuencia = 0;
        for (int i = 0; i < numHilos; i++) {
            hilos[i].join(); 
            totalPalabras += hilos[i].getTotalPalabras();
            contPalabra += hilos[i].getContadorPalabraEspecifica();
            contSecuencia += hilos[i].getContadorSecuencia();
        }
        long fin = System.nanoTime();

        if (imprimirResultados) {
            System.out.println("Conteo Paralelo   -> Total: " + totalPalabras + " | Exactas ('" + pBuscada + "'): " + contPalabra + " | Secuencias ('" + sBuscada + "'): " + contSecuencia);
        }

        return (fin - inicio);
    }
}