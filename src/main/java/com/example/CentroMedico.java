package com.example;



public class CentroMedico {
    
    private final Doctor[] medicosDisponibles;
    private final Enfermero[] enfermerosDisponibles;
    private final Recepcionista Recepcionista;
    
    public CentroMedico(int medicos, int enfermeros, boolean salaEmergencia) {
        this.medicosDisponibles = new Doctor[medicos];
        this.enfermerosDisponibles = new Enfermero[enfermeros];
        
        generarEnfermeros(enfermeros);
        generarDoctores(medicos);
        this.Recepcionista = new Recepcionista("Recepcionista");
    }
    
    

    public void generarEnfermeros(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            new Enfermero("Enfermero " + (i + 1)).start();
        }
    }

    public void iniciarDoctores(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Doctor doctor = new Doctor(i + 1, this);
            doctores.add(doctor);
            doctor.start();
        }
    }

    public void detenerDoctores() {
        for (Doctor doctor : doctores) {
            doctor.detener();
            doctor.interrupt();
        }
    }

    public Recepcionista getRecepcionista() {
        return Recepcionista;
    }
}
