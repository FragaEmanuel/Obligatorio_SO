package com.example;



public class CentroMedico {

    private final int medicosDisponibles;
    private final int enfermerosDisponibles;
    private final int consultoriosdisponibles;
    private final Recepcionista Recepcionista;

    public CentroMedico(int medicos, int enfermeros, int consultorios) {
        this.medicosDisponibles = medicos;
        this.enfermerosDisponibles = enfermeros;
        this.consultoriosdisponibles = consultorios;

        for (int i = 0; i < consultorios; i++) {
            SimulacionCentroMedico.consultaoriodisponibles.release();
        }
        for (int i = 0; i < enfermeros; i++) {
            SimulacionCentroMedico.enfermerosdisponibles.release();
        }
        for (int i = 0; i < medicos; i++) {
            SimulacionCentroMedico.medicosdisponibles.release();
        }
        this.Recepcionista = new Recepcionista("Recepcionista");
    }

    public Recepcionista getRecepcionista() {
        return Recepcionista;
    }
}