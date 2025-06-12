package com.example;

public class Consulta implements Comparable<Consulta> {
    public enum TipoConsulta { EMERGENCIA, CONTROL, CURACION, ODONTOLOGIA }

    private final TipoConsulta tipo;
    private final int tiempoLlegada;
    private int prioridad;
    private final String idPaciente;
    private final int duracionConsulta;

    public Consulta(TipoConsulta tipo, String idPaciente, int horaLlegada) {
        this.tipo = tipo;
        this.idPaciente = idPaciente;
        this.tiempoLlegada = horaLlegada;
        this.duracionConsulta = duracionPorTipo(tipo);
        this.prioridad = calcularPrioridadInicial();
    }

    private int duracionPorTipo(TipoConsulta tipo) {
        return switch (tipo) {
            case EMERGENCIA -> 10;
            case CONTROL -> 7;
            case CURACION -> 5;
            case ODONTOLOGIA -> 8;
        };
    }

    private int calcularPrioridadInicial() {
        return switch (tipo) {
            case EMERGENCIA -> 80;
            case CONTROL, CURACION -> 50;
            case ODONTOLOGIA -> 30;
        };
    }

    public void actualizarPrioridad(int horaActual) {
        int espera = horaActual - tiempoLlegada;
        if (tipo == TipoConsulta.EMERGENCIA && espera >= 120) {
            prioridad = 100;
        } else {
            prioridad += espera / 40;
        }
    }

    public TipoConsulta getTipo() { return tipo; }
    public int getPrioridad() { return prioridad; }
    public String getIdPaciente() { return idPaciente; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getDuracionConsulta() { return duracionConsulta; }

    @Override
    public int compareTo(Consulta o) {
        return Integer.compare(o.prioridad, this.prioridad);
    }
}
