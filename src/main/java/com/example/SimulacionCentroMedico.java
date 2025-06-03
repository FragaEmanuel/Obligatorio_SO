package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.example.Consulta.TipoConsulta;

public class SimulacionCentroMedico {

    private static int Hora;

    static Semaphore haysala = new Semaphore(1);
    static Semaphore haysalaEmergencia = new Semaphore(1);
    static Semaphore medicosdisponibles;
    static Semaphore enfermerosdisponibles;
    static Semaphore pacientesparacurar = new Semaphore(0);
    static Semaphore genteParaAtender = new Semaphore(0);
    static Semaphore haypacientesCola1 = new Semaphore(0);
    static Semaphore haypacientesCola2 = new Semaphore(0);

    public static int getHora() {
        return Hora;
    }

    public static void iniciar() {

        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Número de médicos: ");
        int medicos = scanner.nextInt();
        medicosdisponibles = new Semaphore(medicos);
        
        
        System.out.print("Número de enfermeros: ");
        int enfermeros = scanner.nextInt();
        enfermerosdisponibles = new Semaphore(enfermeros);
       
        
        CentroMedico centro = new CentroMedico(medicos, enfermeros);
    
        List<Consulta> consultas = new ArrayList<>();
        ManejadorArchivosGenerico manejador = new ManejadorArchivosGenerico();
        String[] contenido = manejador.leerArchivo("Obligatorio_SO/src/main/java/com/example/Consultas1.txt");
        for (String linea : contenido) {
            String[] partes = linea.split(",");
            if (partes.length >= 2) {
                TipoConsulta tipoConsulta = TipoConsulta.valueOf(partes[1].trim().toUpperCase());
                String[] HORA_HM = partes[2].split(":");
                int hora = Integer.parseInt(HORA_HM[0].trim())*60 + Integer.parseInt(HORA_HM[1].trim());
                Consulta consulta = new Consulta(tipoConsulta, partes[0].trim(), hora, Integer.parseInt(partes[3].trim()));
                consultas.add(consulta);
            }
        }
        consultas.sort((c1, c2) -> Integer.compare(c1.getTiempoLlegada(), c2.getTiempoLlegada()));

        LlegadaPacientes llegadaPacientes = new LlegadaPacientes(centro, consultas);
        llegadaPacientes.start();

        while (Hora < 720) {
            try {
                Thread.sleep(1000); // Simula el paso del tiempo en minutos
                Hora++;
            } catch (InterruptedException e) {
                System.out.println("Error al simular el tiempo: " + e.getMessage());
            }
        }
        }

}

