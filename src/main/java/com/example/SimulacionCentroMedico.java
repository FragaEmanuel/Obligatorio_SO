package com.example;

import java.io.File;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SimulacionCentroMedico {
    public static final int DURACION_SIMULACION = 10; // Cambiar a 5 para pruebas rápidas
    private static int Hora;
    private static final Object lock = new Object();
    public static List<Consulta> hilosConsultas = new ArrayList<>();
    public static int hilosListos;
    public static int hilosEsperados;

    static Semaphore consultaoriodisponibles;
    static Semaphore haysalaEmergencia = new Semaphore(1);
    static Semaphore medicosdisponibles;
    static Semaphore enfermerosdisponibles;
    static Semaphore ObtenerRecursos = new Semaphore(1);

    public static int consultasAtendidas = 0;
    public static int consultasPerdidas = 0;
    public static Map<TipoConsulta, Integer> atendidasPorTipo = new HashMap<>();
    public static Map<TipoConsulta, Integer> perdidasPorTipo = new HashMap<>();

    public static int getHora() {
        return Hora;
    }

    public static Object getLock() {
        return lock;
    }

    public static void iniciar() throws InterruptedException {
        int consultorio = 1;
        int medicos = 1;
        int enfermeros = 1;
        String nombrearchivo = "Consultas1.csv";
        String nombrearchivosalida = "P2.csv";
        String ubicacionEntrada = "src/main/java/com/example/Entradas/" + nombrearchivo;

        consultaoriodisponibles = new Semaphore(consultorio);
        medicosdisponibles = new Semaphore(medicos);
        enfermerosdisponibles = new Semaphore(enfermeros);

        CentroMedico centro = new CentroMedico(medicos, enfermeros, consultorio);
        List<Consulta> consultas = new ArrayList<>();
        String[] contenido = ManejadorArchivosGenerico.leerArchivo(ubicacionEntrada);
        for (String linea : contenido) {
            String[] partes = linea.split(";");
            if (partes.length >= 3) {
                TipoConsulta tipoConsulta = TipoConsulta.valueOf(partes[0].trim().toUpperCase());
                String[] HORA_HM = partes[2].split(":");
                int hora = (Integer.parseInt(HORA_HM[0].trim()) - 8) * 60 + Integer.parseInt(HORA_HM[1].trim());
                Consulta consulta = new Consulta(tipoConsulta, Integer.parseInt(partes[1]), hora);
                consultas.add(consulta);
            }
        }
        for (Consulta consulta : consultas) {
            centro.getRecepcionista().agregarConsulta(consulta);
        }

        for (int i = 0; i < DURACION_SIMULACION; i++) {
            int hora = 8 + (i / 60);
            int minuto = i % 60;
            String reloj = String.format("%02d:%02d", hora, minuto);
            System.out.println("@️ Minuto simulado: " + i + " (" + reloj + ")");

            centro.getRecepcionista().agregarConsultaCorrespondiente();
            List<Consulta> nuevos = centro.getRecepcionista().atenderConsultasCorrespondientesYDevuelveHilos();
            hilosConsultas.addAll(nuevos);
            System.out.println("* Hilos activos lanzados este minuto: " + nuevos.size());

            if (i % 60 == 0 && i > 0) {
                System.out.println("() Completada la hora " + (hora - 1));
            }

            synchronized (lock) {
                lock.notifyAll();

                long esperaInicio = System.currentTimeMillis();
                while (true) {
                    hilosConsultas.removeIf(h -> !h.isAlive());
                    hilosEsperados = hilosConsultas.size();

                    if (hilosListos >= hilosEsperados || hilosEsperados == 0) break;

                    lock.wait(1000);
                    if (System.currentTimeMillis() - esperaInicio > 3000) {
                        System.out.println("/ Timeout esperando hilos. Continuando de todos modos.");
                        break;
                    }
                }

                hilosListos = 0;
            }

            Hora++;
        }

        System.out.println("- Esperando que finalicen los hilos activos...");
        for (Consulta c : hilosConsultas) {
            try {
                c.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String[] salida = new String[4];
        salida[0] = "Consultas atendidas: " + consultasAtendidas;
        salida[1] = "Consultas perdidas: " + consultasPerdidas;
        salida[2] = "Atendidas por tipo: " + atendidasPorTipo;
        salida[3] = "Perdidas por tipo: " + perdidasPorTipo;

        String salidaPath = "src/main/java/com/example/Salidas/" + nombrearchivosalida;
        ManejadorArchivosGenerico.escribirArchivo(salidaPath, salida);

        System.out.println("/// Simulación finalizada. Archivo generado: " + salidaPath);
        System.out.println("/// Hilos vivos al cerrar: " + hilosConsultas.stream().filter(Thread::isAlive).count());
        System.exit(0);
    }
}