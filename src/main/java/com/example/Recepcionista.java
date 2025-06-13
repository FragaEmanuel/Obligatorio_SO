package com.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    //version más segura de obtenersiguienteconsulta para que no se acumulen consultas fuera de la cola
    public Consulta obtenerSiguienteConsultaPreventivo() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaConsultorio.isEmpty()) {
            for (Consulta consulta : colaConsultorio) {
                consulta.actualizarPrioridad();
            }
            Consulta consultaPeek = colaConsultorio.peek();
            if (consultaPeek != null && consultaPeek.getTiempoLlegada() == SimulacionCentroMedico.getHora()){
                boolean recursosDisponibles = false;        //revisa si hay recursos
                switch (consultaPeek.getTipo()) {
                    case EMERGENCIA:
                        recursosDisponibles = 
                            SimulacionCentroMedico.haysalaEmergencia.availablePermits() > 0 &&
                            SimulacionCentroMedico.medicosdisponibles.availablePermits() > 0 &&
                            SimulacionCentroMedico.enfermerosdisponibles.availablePermits() > 0;
                        break;
                    case CONTROL:
                    case CARNE:
                        recursosDisponibles = 
                            SimulacionCentroMedico.consultaoriodisponibles.availablePermits() > 0 &&
                            SimulacionCentroMedico.medicosdisponibles.availablePermits() > 0 &&
                            SimulacionCentroMedico.enfermerosdisponibles.availablePermits() > 0;
                        break;
                    case CURACION:
                    case ANALISIS:
                        recursosDisponibles = 
                            SimulacionCentroMedico.consultaoriodisponibles.availablePermits() > 0 &&
                            SimulacionCentroMedico.enfermerosdisponibles.availablePermits() > 0;
                        break;
                    case ODONTOLOGIA:
                        // Ajusta según recursos para odontología
                        recursosDisponibles = true;
                        break;
                }
                if (recursosDisponibles) {              //si hay recursos disponibles para dicha consulta la saca de la cola y la devuelve.
                    colaConsultorio.remove(consultaPeek);
                    return consultaPeek;
                }
                return null;            //si no hay recursos retorna null
            } else {
                return null;            //si la hora no coincide retorna null
            }
        } else {
            return null;    //si la cola está vacia la cola retorna null
        }
    }
    
    public void atenderConsultasCorrespondientes() throws InterruptedException{
        boolean x = true;
        while (x) {                                                 //va sacando consultas validas hasta que x sea falso
            Consulta consul = obtenerSiguienteConsulta();
            if (consul == null) {                                   //cuando el metodo para obtener la siguiente consulta devulva null significa que no hay consultas que puedan salir en ese minuto
                x = false;
            } else {
                SimulacionCentroMedico.ObtenerRecursos.acquire();       //no se si esto es util, casi seguro que no
                consul.start();     //inicia el hilo
                
                SimulacionCentroMedico.ObtenerRecursos.release();
            }
        }
    }

    public Lock getLock(){
        return lock;
    }
}
