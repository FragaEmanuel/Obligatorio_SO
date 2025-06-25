package com.example;

import java.util.*;
import java.util.concurrent.Semaphore;

public class SimulacionCentroMedico {

    public static final int DURACION_SIMULACION = 720; // 12 horas de simulación (08:00 a 20:00)
    private static int horaSimulada = 0;
    private static final Object lock = new Object(); // Para sincronización minuto a minuto

    // Semáforos de recursos diferenciados
    public static Semaphore consultaoriodisponibles;
    public static Semaphore haysalaEmergencia = new Semaphore(1); // 1 sala de emergencia por letra
    public static Semaphore MedicosDisponibles;
    public static Semaphore OdontologosDisponibles;
    public static Semaphore EnfermerosFijos;       // Solo para CURACION y ANALISIS
    public static Semaphore EnfermerosRotativos;   // Para médicos y odontólogos
    public static Semaphore ObtenerRecursos = new Semaphore(1); // Mutex para reservar recursos

    // Configuración (puede adaptarse para mínimo o extendido)
    public static int cantidadMedicos = /*2;*/ 2;//1; // 1 o 2 depende el caso
    public static int cantidadOdontologos = /*1;*/ 1;//0; //0 o 1 dependiendo el caso
    public static int cantidadEnfermerosFijos = /*1;*/ 1; //0 o 1 dependiendo el caso
    public static int cantidadEnfermerosRotativos = 1; // Siempre 1, en el caso 1 cumple ambos roles del enfermero, en el caso 2 cumple solo su rol original
    public static int cantidadConsultorios =2; // siempre 2 por letra

    // Estadísticas
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
        // Inicialización de semáforos en 0, se llenan en CentroMedico
        consultaoriodisponibles = new Semaphore(0);
        MedicosDisponibles = new Semaphore(0);
        OdontologosDisponibles = new Semaphore(0);
        EnfermerosFijos = new Semaphore(0);
        EnfermerosRotativos = new Semaphore(0);

        // Crear centro médico y recepcionista
        CentroMedico centro = new CentroMedico(cantidadMedicos, cantidadOdontologos, cantidadEnfermerosFijos, cantidadEnfermerosRotativos, cantidadConsultorios);
        Recepcionista recepcionista = centro.getRecepcionista();


        // Cargar archivo de entrada
        String[] contenido = ManejadorArchivosGenerico.leerArchivo("src/main/java/com/example/Entradas/Consultas1.csv");
        for (String linea : contenido) {
            String[] partes = linea.split(";");
            if (partes.length >= 3) {
                TipoConsulta tipo = TipoConsulta.valueOf(partes[0].trim().toUpperCase());
                int id = Integer.parseInt(partes[1]);
                String[] hm = partes[2].split(":");

                int horaRaw = Integer.parseInt(hm[0]);
                int minutos = Integer.parseInt(hm[1]);

                if (horaRaw < 8 || horaRaw > 19 || minutos < 0 || minutos >= 60) {
                    System.out.println("Línea ignorada: horario fuera de rango (8:00 a 20:00): " + linea);
                    continue;
                }

                int hora = (horaRaw - 8) * 60 + minutos;
                Consulta c = new Consulta(tipo, id, hora);
                System.out.println("...... Consulta cargada: ID " + id + ", tipo " + tipo + ", minuto " + hora);
                recepcionista.agregarConsulta(c);
            }
        }


        // Bucle principal de simulación minuto a minuto
        for (int minuto = 0; minuto < DURACION_SIMULACION; minuto++) {
            int hora = 8 + (minuto / 60);
            int min = minuto % 60;
            System.out.printf("@️ Minuto simulado: %d (%02d:%02d)%n", minuto, hora, min);

            // CAMBIO DE TURNO a las 14:00 (minuto 360)
            if (minuto == 360) {
                System.out.println("# Cambio de turno (14:00): reinicio de personal");
                MedicosDisponibles = new Semaphore(cantidadMedicos);
                OdontologosDisponibles = new Semaphore(cantidadOdontologos);
                EnfermerosFijos = new Semaphore(cantidadEnfermerosFijos);
                EnfermerosRotativos = new Semaphore(cantidadEnfermerosRotativos);
            }

            // Cargar y lanzar consultas del minuto actual
            recepcionista.agregarConsultasDelMinuto(minuto);
            List<Consulta> lanzadas = recepcionista.atenderConsultasDisponibles();
            hilosActivos.addAll(lanzadas);
            System.out.println("* Hilos activos lanzados este minuto: " + lanzadas.size());

            if (minuto % 60 == 0 && minuto > 0) {
                System.out.println("() Completada la hora " + (hora - 1));
            }

            // Sincronización con hilos
            synchronized (lock) {
                lock.notifyAll(); // Despierta hilos activos
                long inicio = System.currentTimeMillis();

                while (true) {
                    hilosActivos.removeIf(h -> !h.isAlive());
                    int vivos = hilosActivos.size();
                    if (vivos == 0 || hilosListos >= vivos)
                        break;

                    long restante = 1000 - (System.currentTimeMillis() - inicio);
                    if (restante <= 0) break;

                    lock.wait(restante);
                }

                hilosListos = 0;
            }
// 🚨 Reintentar lanzar consultas si ya no hay hilos activos y aún hay pendientes
            if (SimulacionCentroMedico.hilosActivos.isEmpty()) {
                List<Consulta> nuevas = recepcionista.atenderConsultasDisponibles();
                hilosActivos.addAll(nuevas);
                if (!nuevas.isEmpty()) {
                    System.out.println("+++ Relanzadas consultas tras liberar recursos: " + nuevas.size());
                }
            }

            horaSimulada++;
        }

        // Esperar que terminen los hilos
        System.out.println("- Esperando que finalicen los hilos activos...");
        for (Consulta c : hilosActivos) {
            c.join(2000);
        }
        List<Consulta> noLanzadas = recepcionista.obtenerConsultasNoLanzadas();
        for (Consulta c : noLanzadas) {
            c.marcarPerdida("no se pudieron asignar recursos durante toda la simulación");
        }


        // Guardar resultados
        String[] salida = new String[] {
                "Consultas atendidas: " + consultasAtendidas,
                "Consultas perdidas: " + consultasPerdidas,
                "Atendidas por tipo: " + atendidasPorTipo,
                "Perdidas por tipo: " + perdidasPorTipo,
                "Consultas turno mañana (8-14): " + atendidasTurno1,
                "Consultas turno tarde (14-20): " + atendidasTurno2,
                "----------------------------------------------------------------------------------------------"
        };

        String archivoSalida = "src/main/java/com/example/Salidas/ResultadosFinal.csv";
        ManejadorArchivosGenerico.escribirArchivo(archivoSalida, salida);

        System.out.println("/// Simulación finalizada. Archivo generado: " + archivoSalida);
        System.out.println("/// Hilos vivos al cerrar: " + hilosActivos.stream().filter(Thread::isAlive).count());

        System.exit(0);
    }
}