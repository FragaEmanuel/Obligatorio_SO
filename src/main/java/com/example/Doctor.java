package com.example;

import javax.swing.CellEditor;

public class Doctor extends Thread {
    String nombre;
    int horaEntrada;
    CentroMedico centroMedico;

    public Doctor(String nombre, CentroMedico centroMedico) {
        this.centroMedico = centroMedico;
        this.nombre = nombre;
        SimulacionCentroMedico.medicosdisponibles.release();
    }

    @Override
    public void run() {
    
    }
}
