package com.example;


public class Enfermero extends Thread {
    String nombre;
    CentroMedico centroMedico;

    public Enfermero(String nombre, CentroMedico centroMedico) {
       
        this.nombre = nombre;
        this.centroMedico = centroMedico;
        SimulacionCentroMedico.enfermerosdisponibles.release();
    }

    @Override
    public void run() {
        while (SimulacionCentroMedico.getHora() < 720) {
            if (centroMedico.getRecepcionista().HayConsultas() == false) {
                SimulacionCentroMedico.haypacientes.acquire();
            }

            SimulacionCentroMedico.haypacientesCola2.acquire();
            SimulacionCentroMedico.enfermerosdisponibles.acquire();

            try {
                Consulta consulta = centroMedico.getRecepcionista().obtenerSiguienteConsulta();
                
                int horaAtendido = SimulacionCentroMedico.getHora();
                while (SimulacionCentroMedico.getHora() > horaAtendido + consulta.getDuracionConsulta()){
                // Simula el tiempo de atención del médo
                }
            } catch (InterruptedException e) {
                SimulacionCentroMedico.haypacientesCola2.release();
            } finally { 
                SimulacionCentroMedico.medicosdisponibles.release(); // Libera el médico para que pueda atender a otro paciente
                SimulacionCentroMedico.enfermerosdisponibles.release();
            }
        }
    }
}
