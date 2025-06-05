package com.example;



public class CentroMedico {
    
    private final Doctor[] medicosDisponibles;
    private final Enfermero[] enfermerosDisponibles;
    private final Recepcionista Recepcionista;
    
    public CentroMedico(int medicos, int enfermeros) {
        this.medicosDisponibles = new Doctor[medicos];
        this.enfermerosDisponibles = new Enfermero[enfermeros];
        
        generarEnfermeros(enfermeros);
        generarDoctores(medicos);
        this.Recepcionista = new Recepcionista("Recepcionista");
    }
    
    

    public void generarEnfermeros(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            new Enfermero("Enfermero " + (i + 1),this).start();
            SimulacionCentroMedico.enfermerosdisponibles.release();
        }
    }
    public void generarDoctores(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Doctor doctor = new Doctor("Doctor " + (i + 1), this);
            medicosDisponibles[i] = doctor;
            doctor.start();
            SimulacionCentroMedico.medicosdisponibles.release();
        }
    }

    public Recepcionista getRecepcionista() {
        return Recepcionista;
    }
}
