package com.example;

import javax.swing.CellEditor;

public class Doctor extends Thread {
    int idDoctor;
    int horaEntrada;
    CentroMedico centroMedico;
    Boolean trabajando;

    public Doctor(int idDoctor, CentroMedico centroMedico) {
        this.centroMedico = centroMedico;
        this.idDoctor = idDoctor;
        SimulacionCentroMedico.medicosdisponibles.release();
    }

    @Override
    public void run() {
        SimulacionCentroMedico.haypacientes.acquire();
        while (trabajando && !isInterrupted()) {
            try {
                Consulta consulta = centroMedico.getRecepcionista().obtenerSiguienteConsulta();
                int horaAtendido = SimulacionCentroMedico.getHora();
                while (SimulacionCentroMedico.getHora() > horaAtendido + consulta.getDuracionConsulta()){
                    // Simula el tiempo de atención del médico
                }
                SimulacionCentroMedico.medicosdisponibles.release(); // Libera el médico para que pueda atender a otro paciente

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}