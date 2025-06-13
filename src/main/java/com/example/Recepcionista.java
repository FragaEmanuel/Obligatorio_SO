package com.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.example.Consulta.TipoConsulta;

public class Recepcionista {
    
    //private final PriorityBlockingQueue<Consulta> colaEmergencias;
    private final PriorityBlockingQueue<Consulta> colaConsultorio;
    
    private String nombre;
    private final Lock lock = new ReentrantLock();

    public Recepcionista(String nombre) {
        this.nombre = nombre;
        this.colaConsultorio = new PriorityBlockingQueue<>();
    }
    

    public void agregarConsulta(Consulta consulta) {
        colaConsultorio.add(consulta);
    }

    public Consulta obtenerSiguienteConsulta() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaConsultorio.isEmpty()) {
            for (Consulta consulta : colaConsultorio) {
                consulta.actualizarPrioridad();
            }
            if (colaConsultorio.peek().getTiempoLlegada() == SimulacionCentroMedico.getHora()){
                return colaConsultorio.take();
            } else return null;
        } else {
            return null;
        }
    }

    public Consulta obtenerSiguienteConsultaPreventivo() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaConsultorio.isEmpty()) {
            for (Consulta consulta : colaConsultorio) {
                consulta.actualizarPrioridad();
            }
            Consulta consultaPeek = colaConsultorio.peek();
            if (consultaPeek != null && consultaPeek.getTiempoLlegada() == SimulacionCentroMedico.getHora()){
                TipoConsulta tipo = consultaPeek.getTipo();
                if (tipo == TipoConsulta.CONTROL || tipo == TipoConsulta.CARNE || tipo == TipoConsulta.EMERGENCIA){
                    if (SimulacionCentroMedico.medicosdisponibles.availablePermits() > 0 && SimulacionCentroMedico.enfermerosdisponibles.availablePermits() > 0 && SimulacionCentroMedico.consultaoriodisponibles.availablePermits() > 0){
                        return colaConsultorio.take();
                    }
                } else {
                    if (SimulacionCentroMedico.enfermerosdisponibles.availablePermits() > 0 && SimulacionCentroMedico.consultaoriodisponibles.availablePermits() > 0){
                        return colaConsultorio.take();
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }
    
    public void atenderConsultasCorrespondientes() throws InterruptedException{
        boolean x = true;
        while (x) {
            Consulta consul = obtenerSiguienteConsulta();
            if (consul == null) {
                x = false;
            } else {
                SimulacionCentroMedico.ObtenerRecursos.acquire();
                consul.start();
                
                SimulacionCentroMedico.ObtenerRecursos.release();
            }
        }
    }

    public Lock getLock(){
        return lock;
    }
}
