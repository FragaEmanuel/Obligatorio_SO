package com.example;

public class Paciente extends Thread {
    private final String nombre;
    private final Consulta consulta;
    private final CentroMedico centro;

    public Paciente(String nombre, Consulta consulta, CentroMedico centro) {
        this.nombre = nombre;
        this.consulta = consulta;
        this.centro = centro;
    }

    @Override
    public void run() {
        try {
            sleep(consulta.getTiempoLlegada() * 1000);
            centro.agregarConsulta(consulta);
            Registro.log("[Paciente] " + nombre + " llegó - " + consulta.getTipo());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
