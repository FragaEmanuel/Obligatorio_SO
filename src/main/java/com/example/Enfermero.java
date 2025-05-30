package com.example;


public class Enfermero extends Thread {
    String nombre;

    public Enfermero(String nombre) {
       
        this.nombre = nombre;
        SimulacionCentroMedico.enfermerosdisponibles.release();
    }

    @Override
    public void run() {
        try {
            SimulacionCentroMedico.pacientesparacurar.acquire();
        } catch (Exception e) {
        } 
    }
}
