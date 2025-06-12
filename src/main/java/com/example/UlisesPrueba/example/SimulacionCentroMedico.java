package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

//import com.example.CentroMedico;


public class SimulacionCentroMedico {


    private static int Hora;

    static Semaphore haysala = new Semaphore(1);
    static Semaphore haysalaEmergencia = new Semaphore(1);
    static Semaphore medicosdisponibles;
    static Semaphore enfermerosdisponibles;
    static Semaphore pacientesparacurar = new Semaphore(0);
    static Semaphore haypacientes = new Semaphore(0);
    static Semaphore genteParaAtender = new Semaphore(0);

    public static int getHora() {
        return Hora;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Número de médicos: ");
        int medicos = scanner.nextInt();
        medicosdisponibles = new Semaphore(medicos);
        
        
        System.out.print("Número de enfermeros: ");
        int enfermeros = scanner.nextInt();
        enfermerosdisponibles = new Semaphore(enfermeros);
       
        System.out.print("¿Sala de emergencias reservada? (true/false): ");
        boolean salaEmergencia = scanner.nextBoolean();
        
        CentroMedico centro = new CentroMedico(medicos, enfermeros, salaEmergencia);
    
        while (Hora <= 720) {
            
            Hora++;
        }


       // CentroMedico centro = new CentroMedico(2,2,true);
        List<String> lineas = ManejadorArchivos.leerLineas("entrada.txt");
        List<Paciente> pacientes = new ArrayList<>();
        int id = 1;

        for (String linea : lineas) {
            String[] partes = linea.split(";");
            int minutos = Integer.parseInt(partes[0]);
            String nombre = partes[1];
            Consulta.TipoConsulta tipo = Consulta.TipoConsulta.valueOf(partes[2].toUpperCase());
            Consulta consulta = new Consulta(tipo, "P" + id++, minutos);
            pacientes.add(new Paciente(nombre, consulta, centro));
        }
        centro.iniciar(pacientes.toArray(new Paciente[0]),medicos);
      //  centro.iniciar(pacientes.toArray(new Paciente[0]), 2);
    }
}
