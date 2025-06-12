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
        while (true) {
            try {
                SimulacionCentroMedico.enfermerosdisponibles.acquire();  // Espera turno
                // Atender paciente: curación o análisis clínico
                // Si no hay pacientes, espera semáforo o notificación
                // Luego libera el semáforo:
                SimulacionCentroMedico.enfermerosdisponibles.release();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}