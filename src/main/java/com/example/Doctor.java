package com.example;

public class Doctor extends Thread {
    private final int id;
    private final CentroMedico centro;

    public Doctor(int id, CentroMedico centro) {
        this.id = id;
        this.centro = centro;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Consulta consulta = centro.obtenerConsultaParaAtender();
                Registro.log("[Doctor " + id + "] Atendiendo " + consulta.getIdPaciente());
                sleep(consulta.getDuracionConsulta() * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
