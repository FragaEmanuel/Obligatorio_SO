package com.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recepcionista {
    
    private final PriorityBlockingQueue<Consulta> colaConsultasPorHora; //Cola de consultas ordenadas por hora
    private final PriorityQueue<Consulta> colaConsultasListas;  //Cola de consultas ordenada por prioridad

    
    private String nombre;
    private final Lock lock = new ReentrantLock();

    public Recepcionista(String nombre) {
        this.nombre = nombre;
        this.colaConsultasPorHora = new PriorityBlockingQueue<>();
        this.colaConsultasListas = new PriorityQueue<>(Comparator.comparingInt(Consulta::getPrioridad).reversed());
    }
    
    //agrega consulta
    public void agregarConsulta(Consulta consulta) {    
        colaConsultasPorHora.add(consulta);
    }

    //obtiene una consulta que cumpla la hora, sino retorna nulo
    public Consulta obtenerSiguienteConsultaPorHora() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaConsultasPorHora.isEmpty()) {
            if (colaConsultasPorHora.peek().getTiempoLlegada() == SimulacionCentroMedico.getHora()){
                return colaConsultasPorHora.take();
            } else return null;
        } else {
            return null;
        }
    }

    //Obtiene la siguiente consulta de la cola de consultas listas
    public Consulta obtenerSiguienteConsultaLista() throws InterruptedException {
        // 1. Verificar emergencias (máxima prioridad)
        if (!colaConsultasListas.isEmpty()) {
            for (Consulta consulta : colaConsultasListas) {
                consulta.actualizarPrioridad();
            }
            Consulta consultaPeek = colaConsultasListas.peek();
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
                    colaConsultasListas.remove(consultaPeek);
                    return consultaPeek;
                }
                return null;            //si no hay recursos retorna null
        } else {
            return null;    //si la cola está vacia la cola retorna null
        }
    }

    //agrega a colaConsultasListas las consultas de colaConsultasPorHora que cumplan la con la hora 
    public void agregarConsultaCorrespondiente() throws InterruptedException{
        boolean x = true;
        while (x) {                                                 //va sacando consultas validas hasta que x sea falso
            Consulta consul = obtenerSiguienteConsultaPorHora();
            if (consul == null) {                                   //cuando el metodo para obtener la siguiente consulta devulva null significa que no hay consultas que puedan salir en ese minuto
                x = false;
            } else {
                colaConsultasListas.add(consul);
            }
        }
    }

    public List<Consulta> atenderConsultasCorrespondientesYDevuelveHilos() throws InterruptedException {
        List<Consulta> lanzadas = new ArrayList<>();
        boolean x = true;
        while (x) {        
            SimulacionCentroMedico.ObtenerRecursos.acquire();
            Consulta consul = obtenerSiguienteConsultaLista();
            if (consul == null) {
                x = false;
            } else {
                consul.start();
                lanzadas.add(consul);
            }
            SimulacionCentroMedico.ObtenerRecursos.release();
        }
        return lanzadas;
    }
    
    //Trata de sacar consultas de colaConsultasListas y revisa si hay recursos para que se ejecuten 
    public void atenderConsultasCorrespondientes() throws InterruptedException{
        boolean x = true;
        while (x) {        
            SimulacionCentroMedico.ObtenerRecursos.acquire();                                         //va sacando consultas validas hasta que x sea falso
            Consulta consul = obtenerSiguienteConsultaLista();
            if (consul == null) {                                   //cuando el metodo para obtener la siguiente consulta devulva null significa que no hay consultas que puedan salir en ese minuto
                x = false;
            } else {
            
                consul.start();     //inicia el hilo
                
            }
        }
    }

    public Lock getLock(){
        return lock;
    }
}
