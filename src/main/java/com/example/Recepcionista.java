package com.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recepcionista {
    
    private final PriorityBlockingQueue<Consulta> colaEmergencias;
    private final PriorityBlockingQueue<Consulta> colaResto;
    
    private String nombre;

    public Recepcionista(String nombre) {
        this.nombre = nombre;
        this.colaEmergencias = new PriorityBlockingQueue<>();
        this.colaResto = new PriorityBlockingQueue<>();
    }
    

    public void agregarConsulta(Consulta consulta) {
        switch (consulta.getTipo()) {
            case EMERGENCIA:
                colaEmergencias.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CONTROL:
                colaResto.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CARNE:
                colaResto.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CURACION:
                colaResto.put(consulta);
                SimulacionCentroMedico.haypacientesCola2.release();
                break;
            case ANALISIS:
                colaResto.put(consulta);
                SimulacionCentroMedico.haypacientesCola2.release();
                break;
            case ODONTOLOGIA:
                break;
        }
    }

    
    public Consulta obtenerSiguienteConsulta() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaEmergencias.isEmpty()) {
            for (Consulta consulta : colaEmergencias) {
                consulta.actualizarPrioridad();
            }
            return colaEmergencias.take();
        }
            
        // 2. Verificar urgencias (prioridad dinámica)
        if (!colaResto.isEmpty()) {
            return colaResto.take();
        }
        // 3. Si no hay consultas, esperar
        return null;
    }

    public static boolean HayConsultas(){
        return !SimulacionCentroMedico.getCentroMedico().getRecepcionista().getColaEmergencias().isEmpty() || 
        !SimulacionCentroMedico.getCentroMedico().getRecepcionista().getColaResto().isEmpty();
    }
}
