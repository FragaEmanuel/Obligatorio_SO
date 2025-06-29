package com.example;

public class CentroMedico {

    // Cantidades iniciales de recursos
    private final int medicosDisponibles;
    private final int odontologosDisponibles;
    private final int enfermerosFijos;
    private final int enfermerosRotativos;
    private final int consultoriosDisponibles;

    //Administrador de las consultas
    private final AdministradorDeConsultas administrador;

    public CentroMedico(int medicos, int odontologos, int fijos, int rotativos, int consultorios) {
        this.medicosDisponibles = medicos;
        this.odontologosDisponibles = odontologos;
        this.enfermerosFijos = fijos;
        this.enfermerosRotativos = rotativos;
        this.consultoriosDisponibles = consultorios;

        // Inicializa semáforos de consultorios
        for (int i = 0; i < consultorios; i++) {
            SimulacionCentroMedico.consultaoriodisponibles.release();
        }

        // Inicializa semáforos de personal de salud
        for (int i = 0; i < medicos; i++) {
            SimulacionCentroMedico.MedicosDisponibles.release();
        }

        for (int i = 0; i < odontologos; i++) {
            SimulacionCentroMedico.OdontologosDisponibles.release();
        }

        for (int i = 0; i < fijos; i++) {
            SimulacionCentroMedico.EnfermerosFijos.release();
        }

        for (int i = 0; i < rotativos; i++) {
            SimulacionCentroMedico.EnfermerosRotativos.release();
        }

        // Crear el administrador de las consultas
        this.administrador = new AdministradorDeConsultas("Administrador");
    }

    public AdministradorDeConsultas getAdministrador() {
        return administrador;
    }
}