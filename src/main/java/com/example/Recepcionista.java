package com.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recepcionista extends Thread {
    
    private final PriorityBlockingQueue<Consulta> colaEmergencias;
    private final PriorityBlockingQueue<Consulta> colaResto;
    

    public Semaphore avisaralRecepcionista = new Semaphore(1);
    private static final Lock lock = new ReentrantLock();
    private final Condition hayConsultas = lock.newCondition();
    
    private String nombre;

    public Recepcionista(String nombre) {
        this.nombre = nombre;
        this.colaEmergencias = new PriorityBlockingQueue<>();
        this.colaResto = new PriorityBlockingQueue<>();
    }

    public void run() {
        while (true) { 
            try {
                SimulacionCentroMedico.genteParaAtender.acquire(); // Espera a que haya gente para atender 
                // Logica para atender consultas y agregarlas a las colas
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                return; // Salir del bucle si se interrumpe
            }
        }
    }
    

    public void agregarConsulta(Consulta consulta) {
        lock.lock();
        switch (consulta.getTipo()) {
            case EMERGENCIA:
                colaEmergencias.put(consulta);
                break;
            case CONTROL:
                colaResto.put(consulta);
                break;
            case CURACION:
                colaResto.put(consulta);
                break;
            case ODONTOLOGIA:
                colaResto.put(consulta);
                break;
        }
        hayConsultas.signalAll();
        lock.unlock();
    }

    
    public Consulta obtenerSiguienteConsulta() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
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
                hayConsultas.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
