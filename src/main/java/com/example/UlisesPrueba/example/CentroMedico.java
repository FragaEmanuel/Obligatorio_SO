package com.example;

import java.util.concurrent.*;

public class CentroMedico {
    private final BlockingQueue<Consulta> colaGeneral = new PriorityBlockingQueue<>();
    private final BlockingQueue<Consulta> colaEmergencias = new PriorityBlockingQueue<>();
    private final BlockingQueue<Consulta> colaAtencion = new ArrayBlockingQueue<>(100);
    private final Recepcionista recepcionista = new Recepcionista(colaGeneral, colaEmergencias);
    private int hora = 0;
    public CentroMedico(int medicos, int enfermeros, boolean tieneSalaEmergencia) {
        // Guardá o usá los valores como necesites
    }

    public void iniciar(Paciente[] pacientes, int medicos) {
        recepcionista.start();
        new Planificador(colaGeneral, colaEmergencias, this).start();
        for (int i = 1; i <= medicos; i++) new com.example.Doctor(i, this).start();
        new SimuladorTiempo(this).start();
        for (Paciente p : pacientes) p.start();
    }

    public void agregarConsulta(Consulta consulta) {
        recepcionista.agregarConsulta(consulta);
    }

    public synchronized void asignarConsulta(Consulta consulta) {
        colaAtencion.add(consulta);
    }

    public Consulta obtenerConsultaParaAtender() throws InterruptedException {
        return colaAtencion.take();
    }

    public synchronized int getHora() {
        return hora;
    }

    public synchronized void avanzarTiempo() {
        hora++;
    }
}
