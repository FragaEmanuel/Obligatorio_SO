package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class SimulacionCentroMedico {

    //Atributos comunes

    private static int Hora;
    private static final Object lock = new Object();
    public static List<Consulta> hilosConsultas = new ArrayList<>();
    public static int hilosListos;
    public static int hilosEsperados;

    //Semaforos

    static Semaphore consultaoriodisponibles;
    static Semaphore haysalaEmergencia = new Semaphore(1);
    static Semaphore medicosdisponibles;
    static Semaphore enfermerosdisponibles;
    static Semaphore ObtenerRecursos = new Semaphore(1);

    //Registro

    public static int consultasAtendidas = 0;
    public static int consultasPerdidas = 0;
    // Puedes usar mapas para estadísticas por tipo
    public static Map<TipoConsulta, Integer> atendidasPorTipo = new HashMap<>();
    public static Map<TipoConsulta, Integer> perdidasPorTipo = new HashMap<>();
        

    public static int getHora() {
        return Hora;
    }

    public static Object getLock() {
        return lock;
    }

    public static void iniciar() throws InterruptedException {

        Scanner scanner = new Scanner(System.in);

        
        System.out.print("Número de consultorios: ");
        //int consultorio = scanner.nextInt();
        int consultorio = 1;
        consultaoriodisponibles = new Semaphore(consultorio);
        
        System.out.print("Número de médicos: ");
        //int medicos = scanner.nextInt();
        int medicos = 1;
        medicosdisponibles = new Semaphore(medicos);
        
        
        System.out.print("Número de enfermeros: ");
        //int enfermeros = scanner.nextInt();
        int enfermeros = 1;
        enfermerosdisponibles = new Semaphore(enfermeros);

        System.out.print("Nombre de Archivo: ");
        //String nombrearchivo = scanner.next();
        String nombrearchivo = "P.csv";
        String ubicacionEntrada = "src/main/java/com/example/Entradas/" + nombrearchivo;
        
        
        CentroMedico centro = new CentroMedico(medicos, enfermeros, consultorio);
    
        List<Consulta> consultas = new ArrayList<>();
        String[] contenido = ManejadorArchivosGenerico.leerArchivo(ubicacionEntrada);
        for (String linea : contenido) {
            String[] partes = linea.split(";");
            if (partes.length >= 2) {
                TipoConsulta tipoConsulta = TipoConsulta.valueOf(partes[0].trim().toUpperCase());
                String[] HORA_HM = partes[2].split(":");
                int hora = (Integer.parseInt(HORA_HM[0].trim())-8)*60 + Integer.parseInt(HORA_HM[1].trim());
                Consulta consulta = new Consulta(tipoConsulta, Integer.parseInt(partes[1]), hora);
                consultas.add(consulta);
            }
        }
        for (Consulta consulta : consultas) {
            centro.getRecepcionista().agregarConsulta(consulta);
        }

        for (int i = 0; i < 720; i++) {
            
            centro.getRecepcionista().agregarConsultaCorrespondiente();
            //centro.getRecepcionista().atenderConsultasCorrespondientes();
            hilosConsultas.addAll(centro.getRecepcionista().atenderConsultasCorrespondientesYDevuelveHilos());
           
            synchronized (lock) {
                lock.notifyAll(); // Avanza todos los hilos un minuto
                while (true) {
                    // Limpia hilos terminados en cada iteración del while
                    SimulacionCentroMedico.hilosConsultas.removeIf(h -> !h.isAlive());
                    hilosEsperados = SimulacionCentroMedico.hilosConsultas.size();
                    if (hilosListos >= hilosEsperados || hilosEsperados == 0) {
                        break;
                    }
                    lock.wait();
                }
                hilosListos = 0;
            }

                Hora++;
            }

        String[] salida = new String[4];
        salida[0] = "Consultas atendidas: " + consultasAtendidas;
        salida[1] = "Consultas perdidas: " + consultasPerdidas;
        salida[2] = "Atendidas por tipo: " + atendidasPorTipo;
        salida[3] = "Perdidas por tipo: " + perdidasPorTipo;

        ManejadorArchivosGenerico.escribirArchivo("src/main/java/com/example/Salidas/" + nombrearchivo, salida);
        System.out.println("Simulacion Terminada. Los datos se guardaron en " + nombrearchivo);
    }
}

