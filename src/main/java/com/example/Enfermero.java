package com.example;


public class Enfermero extends Thread {
    String nombre;
//Agregarle la parte de analisis clinico, ademas de curacion que ya esta, cuando este disponible va para el doctor
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
