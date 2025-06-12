package com.example;//package com.example;

import static com.example.SimulacionCentroMedico.getHora;

public class Doctor extends Thread {
    private final int id;
    private final CentroMedico centro;

    public Doctor(int id, CentroMedico centro) {
        this.id = id;
        this.centro = centro;
    }
    public int getTurno() { // tendria que simular el tiempo de cambio de turno
        int h = getHora();
        if (h < 360) return 1; // 8:00 a 14:00
        else return 2;          // 14:00 a 20:00
    }


    @Override
    public void run() {
        while (true) {
            try {
                SimulacionCentroMedico.medicosdisponibles.acquire();  // Espera turno
                Consulta consulta = centro.obtenerConsultaParaAtender();
                Registro.log("[Doctor " + id + "] Atendiendo " + consulta.getIdPaciente());
                Thread.sleep(consulta.getDuracionConsulta() * 1000);
                Registro.log("[Doctor " + id + "] Termino de atender al paciente " + consulta.getIdPaciente());
                SimulacionCentroMedico.medicosdisponibles.release();  // Libera turno

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}