package com.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recepcionista {
    
    private final PriorityBlockingQueue<Consulta> colaEmergencias;
    private final PriorityBlockingQueue<Consulta> colaConsultorio;
    private final PriorityBlockingQueue<Consulta> colaEnfermeria;
    
    private String nombre;

    public Recepcionista(String nombre) {
        this.nombre = nombre;
        this.colaEmergencias = new PriorityBlockingQueue<>();
        this.colaConsultorio = new PriorityBlockingQueue<>();
        this.colaEnfermeria = new PriorityBlockingQueue<>();
    }
    

    public void agregarConsulta(Consulta consulta) {
        switch (consulta.getTipo()) {
            case EMERGENCIA:
                colaEmergencias.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CONTROL:
                colaConsultorio.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CARNE:
                colaConsultorio.put(consulta);
                SimulacionCentroMedico.haypacientesCola1.release();
                break;
            case CURACION:
                colaEnfermeria.put(consulta);
                SimulacionCentroMedico.haypacientesCola2.release();
                break;
            case ANALISIS:
                colaEnfermeria.put(consulta);
                SimulacionCentroMedico.haypacientesCola2.release();
                break;
            case ODONTOLOGIA:
                break;
        }
    }

    
    public Consulta obtenerSiguienteConsultaMedico() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaEmergencias.isEmpty()) {
            for (Consulta consulta : colaEmergencias) {
                consulta.actualizarPrioridad();
            }
            return colaEmergencias.take();
        }
            
        // 2. Verificar urgencias (prioridad dinámica)
        if (!colaConsultorio.isEmpty()) {
            for (Consulta consulta : colaConsultorio) {
                consulta.actualizarPrioridad();
            }
            return colaConsultorio.take();
        }
        // 3. Si no hay consultas, esperar
        return null;
    }

    public Consulta obtenerSiguienteConsultaEnfermeria() throws InterruptedException {
        if (!colaEnfermeria.isEmpty()) {
            for (Consulta consulta : colaEnfermeria) {
                consulta.actualizarPrioridad();
            }
            return colaEnfermeria.take();
        }

        return null;
    }

    public static boolean HayConsultas(){
        return !SimulacionCentroMedico.getCentroMedico().getRecepcionista().getColaEmergencias().isEmpty() || 
        !SimulacionCentroMedico.getCentroMedico().getRecepcionista().getColaResto().isEmpty();
    }
}
