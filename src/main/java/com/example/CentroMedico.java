package com.example;



public class CentroMedico {
    
    private final int medicosDisponibles;
    private final int enfermerosDisponibles;
    private final Recepcionista Recepcionista;
    
    public CentroMedico(int medicos, int enfermeros, boolean salaEmergencia) {
        this.medicosDisponibles = medicos;
        this.enfermerosDisponibles = enfermeros;
        
        generarEnfermeros(enfermeros);
        generarDoctores(medicos);
        this.Recepcionista = new Recepcionista("Recepcionista");
    }
    
    

    public void generarEnfermeros(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            new Enfermero("Enfermero " + (i + 1)).start();
        }
    }

    public void generarDoctores(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            new Doctor("Doctor " + (i + 1),this ).start();
        }
    }

    public Recepcionista getRecepcionista() {
        return Recepcionista;
    }
}
