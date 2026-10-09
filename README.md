# Motor de Procesamiento Textual Multihilo (Java Threads)

![Java](https://img.shields.io/badge/Java-27_OpenJDK-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Make](https://img.shields.io/badge/Make-Build_Automation-0089A7?style=for-the-badge&logo=gnu&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

## RESUMEN (ABSTRACT)

**Problema:** El análisis secuencial de grandes volúmenes de texto subutiliza los procesadores multinúcleo modernos, resultando en tiempos de ejecución lineales que se vuelven ineficientes al escalar la cantidad de datos.

**Propuesta:** Se implementa un motor de búsqueda y conteo de frecuencias léxicas en Java. El sistema evade herramientas de alto nivel como `ExecutorService` o `parallelStream` para gestionar la concurrencia a muy bajo nivel utilizando la clase nativa `Thread`, aislando la lectura de disco y dividiendo matemáticamente la carga de trabajo en memoria.

**Tecnologías:** Java (StandardCharsets, Regex Unicode), concurrencia nativa y automatización por `Makefile`.

**Resultados:** El sistema logró procesar más de **38 millones de palabras** en apenas **~85 milisegundos**, demostrando una aceleración de **3.63x** al paralelizar la carga en 4 hilos frente al enfoque lineal tradicional.

---

## ARQUITECTURA DEL SISTEMA

El proyecto está diseñado para garantizar la integridad de los datos evitando *Race Conditions* y separar estrictamente el tiempo de preparación del tiempo de CPU.

```mermaid
%%{init: {'theme':'base', 'themeVariables': { 'fontSize':'14px'}}}%%
graph LR
    subgraph FASE_1[" "]
        direction TB
        A1["<b>Fase 1: Preparación (I/O)</b><br/><br/>① Lectura NIO UTF-8<br/>② Regex Unicode \p{L}<br/>③ Multiplicador de Arrays"]
    end
    
    subgraph FASE_2[" "]
        direction TB
        B1["<b>Fase 2: Motor de Inferencia</b><br/><br/>① Benchmark Secuencial (Base)<br/>② Particionamiento Matemático<br/>③ Instanciación de Hilos"]
    end

    subgraph FASE_3[" "]
        direction TB
        C1["<b>Fase 3: Sincronización</b><br/><br/>① Ejecución en Núcleos Físicos<br/>② Bloqueo Thread.join()<br/>③ Sumatoria de Resultados"]
    end
    
    FASE_1 ~~~ FASE_2
    FASE_2 ~~~ FASE_3
    
    style A1 fill:#eef5fb,stroke:#3b82f6,stroke-width:2px,text-align:left
    style B1 fill:#f0fdf4,stroke:#10b981,stroke-width:2px,text-align:left
    style C1 fill:#fffbeb,stroke:#f59e0b,stroke-width:2px,text-align:left
    style FASE_1 fill:none,stroke:none
    style FASE_2 fill:none,stroke:none
    style FASE_3 fill:none,stroke:none
```

---

## Análisis de Rendimiento y Resultados

El experimento constó de dos fases: el procesamiento del texto original Don Quijote de la Mancha y una prueba de estrés utilizando una versión del texto ampliada 100 veces. Los tiempos reflejan el promedio de 5 iteraciones consecutivas.

### 1. Pruebas: Texto Original (386,793 palabras)

Se validó la integridad algorítmica asegurando que el conteo de la palabra exacta (`quijote`: 2245) y la secuencia (`ción`: 950) fuera matemáticamente idéntico en todos los métodos.

| Método de Procesamiento | Hilos | Tiempo Promedio (ms) | Aceleración (Speedup) |
|---|---|---|---|
| Secuencial (Baseline) | 1 | 4.8500 ms | 1.00x |
| Paralelo | 2 | 2.6681 ms | 1.82x |
| Paralelo | 4 | 1.4155 ms | 3.43x |

### 2. Pruebas: Texto Ampliado x100 (38,679,300 palabras)

En el escenario de carga masiva, la eficiencia del paralelismo absorbe por completo el overhead de la creación de hilos, demostrando una escalabilidad casi lineal.

| Método de Procesamiento | Hilos | Tiempo Promedio (ms) | Aceleración (Speedup) |
|---|---|---|---|
| Secuencial (Baseline) | 1 | 309.6917 ms | 1.00x |
| Paralelo | 2 | 164.0379 ms | 1.89x |
| Paralelo | 4 | 85.3444 ms | 3.63x |

---

## Compilación y Ejecución

El proyecto incluye un `Makefile` para abstraer la compilación manual de Java y la limpieza de binarios.

```bash
# 1. Clonar el repositorio
git clone https://github.com/elsebasdev1/secuential_and_parallel_processing
cd secuential_and_parallel_processing

# 2. Compilar el código y ejecutar las pruebas
make
```

---

## 📂 Estructura del Repositorio

```
├── resources/
│   └── quijote.txt         # Dataset de prueba (Obra completa en formato crudo)
├── src/
│   ├── App.java            # Orquestador de pruebas y lógica de particionamiento
│   └── ProcesadorHilo.java # Definición del Obrero (extends Thread)
└── Makefile                 # Automatización del proceso de compilación
```