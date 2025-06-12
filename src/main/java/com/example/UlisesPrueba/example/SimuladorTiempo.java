package com.example;

public class SimuladorTiempo extends Thread {
    private final CentroMedico centro;

    public SimuladorTiempo(CentroMedico centro) {
        this.centro = centro;
    }

    @Override
    public void run() {
        while (centro.getHora() <= 720) {
            try {
                Thread.sleep(1000);
                centro.avanzarTiempo();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
