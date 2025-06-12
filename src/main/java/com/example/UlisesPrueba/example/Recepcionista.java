package com.example;

import java.util.concurrent.BlockingQueue;

public class Recepcionista extends Thread {
    private final BlockingQueue<Consulta> colaGeneral;
    private final BlockingQueue<Consulta> colaEmergencias;

    public Recepcionista(BlockingQueue<Consulta> colaGeneral, BlockingQueue<Consulta> colaEmergencias) {
        this.colaGeneral = colaGeneral;
        this.colaEmergencias = colaEmergencias;
    }

    public void agregarConsulta(Consulta consulta) {
        if (consulta.getTipo() == Consulta.TipoConsulta.EMERGENCIA) {
            colaEmergencias.add(consulta);
        } else {
            colaGeneral.add(consulta);
        }
    }
}
