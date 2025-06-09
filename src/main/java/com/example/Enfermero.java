package com.example;

import sun.jvm.hotspot.interpreter.OffsetClosure;


public class Enfermero extends Thread {
    String nombre;
    CentroMedico centroMedico;
    boolean ocupado = false;
    Consulta consultaatendida = null;
    int duracionconsultaactual;
    int contadorduracion;

    public Enfermero(String nombre, CentroMedico centroMedico) {
       
        this.nombre = nombre;
        this.centroMedico = centroMedico;
        SimulacionCentroMedico.enfermerosdisponibles.release();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (SimulacionCentroMedico.getLock()) {
                try {
                    SimulacionCentroMedico.getLock().wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
    
                if (ocupado) {
                    if (contadorduracion < duracionconsultaactual)
                    contadorduracion++;
                    else {
                        ocupado = false;
                        consultaatendida = null;
                        duracionconsultaactual = 0;
                        contadorduracion = 0;
                        SimulacionCentroMedico.enfermerosdisponibles.release();
                    }
                } else {
                    SimulacionCentroMedico.haysala.acquire();
                    SimulacionCentroMedico.haypacientesCola2.acquire();

                    try {
                        centroMedico.getRecepcionista().getLock().lock();
                        try {
                        consultaatendida = centroMedico.getRecepcionista().obtenerSiguienteConsultaMedico();
                        } finally {
                            centroMedico.getRecepcionista().getLock().unlock();
                        }
                        int horaAtendido = SimulacionCentroMedico.getHora();
                        ocupado = true;
                        duracionconsultaactual = consultaatendida.getDuracionConsulta();

                    } catch (InterruptedException e) {
                        SimulacionCentroMedico.haypacientesCola2.release();
                    }
                }
            }
        }   
    }
}
