package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Planificador extends Thread {
    private final BlockingQueue<Consulta> colaGeneral;
    private final BlockingQueue<Consulta> colaEmergencias;
    private final CentroMedico centro;

    public Planificador(BlockingQueue<Consulta> colaGeneral, BlockingQueue<Consulta> colaEmergencias, CentroMedico centro) {
        this.colaGeneral = colaGeneral;
        this.colaEmergencias = colaEmergencias;
        this.centro = centro;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Consulta siguiente = obtenerSiguienteConsulta();
                if (siguiente != null) centro.asignarConsulta(siguiente);
                sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private Consulta obtenerSiguienteConsulta() {
        List<Consulta> lista = new ArrayList<>();
        colaEmergencias.drainTo(lista);
        for (Consulta c : lista) c.actualizarPrioridad(centro.getHora());
        if (!lista.isEmpty()) return Collections.max(lista);

        colaGeneral.drainTo(lista);
        for (Consulta c : lista) c.actualizarPrioridad(centro.getHora());
        return lista.isEmpty() ? null : Collections.max(lista);
    }
}
