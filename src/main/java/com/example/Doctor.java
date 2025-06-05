package com.example;

import javax.swing.CellEditor;

public class Doctor extends Thread {
    String idDoctor;
    int horaEntrada;
    CentroMedico centroMedico;

    public Doctor(String idDoctor, CentroMedico centroMedico) {
        this.centroMedico = centroMedico;
        this.idDoctor = idDoctor;
        SimulacionCentroMedico.medicosdisponibles.release();
    }

    @Override
    public void run() {
        while (SimulacionCentroMedico.getHora() < 720) {
            if (centroMedico.getRecepcionista().HayConsultas() == false) {
                SimulacionCentroMedico.haypacientes.acquire();
            }

            SimulacionCentroMedico.haypacientesCola1.acquire();
            SimulacionCentroMedico.enfermerosdisponibles.acquire();

            try {
                Consulta consulta = centroMedico.getRecepcionista().obtenerSiguienteConsulta();
                
                int horaAtendido = SimulacionCentroMedico.getHora();
                while (SimulacionCentroMedico.getHora() > horaAtendido + consulta.getDuracionConsulta()){
                // Simula el tiempo de atención del médico
                }
            } catch (InterruptedException e) {
                SimulacionCentroMedico.haypacientesCola1.release();
            } finally { 
                SimulacionCentroMedico.medicosdisponibles.release(); // Libera el médico para que pueda atender a otro paciente
                SimulacionCentroMedico.enfermerosdisponibles.release();
            }
        }
    }
}
