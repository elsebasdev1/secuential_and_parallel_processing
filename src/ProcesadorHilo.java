public class ProcesadorHilo extends Thread {
    
    // 1. Referencias compartidas (Lectura)
    private String[] palabras;
    private String palabraBuscada;
    private String secuenciaBuscada;
    
    // 2. Límites de trabajo exclusivos para este hilo
    private int inicio;
    private int fin;
    
    // 3. Resultados
    private int totalPalabras = 0;
    private int contadorPalabraEspecifica = 0;
    private int contadorSecuencia = 0;

    // CONSTRUCTOR
    public ProcesadorHilo(String[] palabras, int inicio, int fin, String palabraBuscada, String secuenciaBuscada) {
        this.palabras = palabras;
        this.inicio = inicio;
        this.fin = fin;
        this.palabraBuscada = palabraBuscada;
        this.secuenciaBuscada = secuenciaBuscada;
    }

    @Override
    public void run() {
        // El bucle SOLO recorre desde su índice de inicio hasta su fin
        for (int i = inicio; i < fin; i++) {
            String palabra = palabras[i];
            
            if (palabra.isEmpty()) continue;
            
            totalPalabras++;
            
            if (palabra.equals(palabraBuscada)) {
                contadorPalabraEspecifica++;
            }
            
            if (palabra.contains(secuenciaBuscada)) {
                contadorSecuencia++;
            }
        }
    }

    // GETTERS
    public int getTotalPalabras() { return totalPalabras; }
    public int getContadorPalabraEspecifica() { return contadorPalabraEspecifica; }
    public int getContadorSecuencia() { return contadorSecuencia; }
}