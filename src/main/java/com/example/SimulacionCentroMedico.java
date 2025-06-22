package com.example;

import java.util.*;
import java.util.concurrent.Semaphore;

public class SimulacionCentroMedico {
    public static final int DURACION_SIMULACION = 720; // 12 horas (08:00 a 20:00)
    private static int horaSimulada = 0;
    private static final Object lock = new Object();

    public static Semaphore consultaoriodisponibles;
    public static Semaphore haysalaEmergencia = new Semaphore(1);
    public static Semaphore MedicosDisponibles;
    public static Semaphore EnfermerosDisponibles;
    public static Semaphore ObtenerRecursos = new Semaphore(1); // Mutex de reserva de recursos

    public static int cantidadMedicos = 2;
    public static int cantidadEnfermeros = 2;
    public static int cantidadConsultorios = 2;

    public static int consultasAtendidas = 0;
    public static int consultasPerdidas = 0;
    public static Map<TipoConsulta, Integer> atendidasPorTipo = new HashMap<>();
    public static Map<TipoConsulta, Integer> perdidasPorTipo = new HashMap<>();
    public static int atendidasTurno1 = 0;
    public static int atendidasTurno2 = 0;

    public static List<String> rechazosDetalle = new ArrayList<>();
    public static List<Consulta> hilosActivos = Collections.synchronizedList(new ArrayList<>());
    public static int hilosListos = 0;

    public static Object getLock() {
        return lock;
    }

    public static int getHora() {
        return horaSimulada;
    }

    public static void iniciar() throws InterruptedException {
        // Inicialización de semáforos según cantidad
        consultaoriodisponibles = new Semaphore(0);
        MedicosDisponibles = new Semaphore(0);
        EnfermerosDisponibles = new Semaphore(0);

        CentroMedico centro = new CentroMedico(cantidadMedicos, cantidadEnfermeros, cantidadConsultorios);
        Recepcionista recepcionista = centro.getRecepcionista();

        // Cargar archivo de entrada
        String[] contenido = ManejadorArchivosGenerico.leerArchivo("src/main/java/com/example/Entradas/Consultas1.csv");
        for (String linea : contenido) {
            String[] partes = linea.split(";");
            if (partes.length >= 3) {
                TipoConsulta tipo = TipoConsulta.valueOf(partes[0].trim().toUpperCase());
                int id = Integer.parseInt(partes[1]);
                String[] hm = partes[2].split(":");
                int hora = (Integer.parseInt(hm[0]) - 8) * 60 + Integer.parseInt(hm[1]);
                Consulta c = new Consulta(tipo, id, hora);
                recepcionista.agregarConsulta(c);
            }
        }

        // Simulación minuto a minuto
        for (int minuto = 0; minuto < DURACION_SIMULACION; minuto++) {
            int hora = 8 + (minuto / 60);
            int min = minuto % 60;
            System.out.printf("@️ Minuto simulado: %d (%02d:%02d)%n", minuto, hora, min);

            if (minuto == 360) {
                System.out.println("# Cambio de turno (14:00): reinicio de personal");
                MedicosDisponibles = new Semaphore(cantidadMedicos);
                EnfermerosDisponibles = new Semaphore(cantidadEnfermeros);
            }

            // Agregar consultas que llegaron en este minuto
            recepcionista.agregarConsultasDelMinuto(minuto);
            List<Consulta> lanzadas = recepcionista.atenderConsultasDisponibles();
            hilosActivos.addAll(lanzadas);
            System.out.println("* Hilos activos lanzados este minuto: " + lanzadas.size());

            if (minuto % 60 == 0 && minuto > 0) {
                System.out.println("() Completada la hora " + (hora - 1));
            }

            // Sincronización con hilos
            synchronized (lock) {
                lock.notifyAll();  // 🔁 Despierta a todos los hilos activos

                long inicio = System.currentTimeMillis();

                while (true) {
                    hilosActivos.removeIf(h -> !h.isAlive());
                    int vivos = hilosActivos.size();

                    if (vivos == 0 || hilosListos >= vivos) break;

                    long restante = 1000 - (System.currentTimeMillis() - inicio); // ⏱ 1 segundo por minuto simulado
                    if (restante <= 0) break;

                    lock.wait(restante);
                }

                hilosListos = 0;
            }


            horaSimulada++;
        }

        System.out.println("- Esperando que finalicen los hilos activos...");
        for (Consulta c : hilosActivos) {
            c.join(2000);
        }

        String[] salida = new String[] {
                "Consultas atendidas: " + consultasAtendidas,
                "Consultas perdidas: " + consultasPerdidas,
                "Atendidas por tipo: " + atendidasPorTipo,
                "Perdidas por tipo: " + perdidasPorTipo,
                "Consultas turno mañana (8-14): " + atendidasTurno1,
                "Consultas turno tarde (14-20): " + atendidasTurno2
        };

        String archivoSalida = "src/main/java/com/example/Salidas/P2.csv";
        ManejadorArchivosGenerico.escribirArchivo(archivoSalida, salida);

        System.out.println("/// Simulación finalizada. Archivo generado: " + archivoSalida);
        System.out.println("/// Hilos vivos al cerrar: " + hilosActivos.stream().filter(Thread::isAlive).count());
        System.exit(0);
    }
}