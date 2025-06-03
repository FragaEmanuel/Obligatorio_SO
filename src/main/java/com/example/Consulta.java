package com.example;


public class Consulta implements Comparable<Consulta> {
    public enum TipoConsulta { EMERGENCIA, CONTROL, CURACION, ODONTOLOGIA, ANALISIS, CARNE }
    
    private final TipoConsulta tipo;
    private final int tiempoLlegada;
    private int prioridad;
    private final String idPaciente;
    private final int duracionConsulta;
    
    public Consulta(TipoConsulta tipo, String idPaciente, int horaLlegada,int duracionConsulta) {
        this.tipo = tipo;
        this.idPaciente = idPaciente;
        this.tiempoLlegada = horaLlegada;
        this.duracionConsulta = duracionConsulta;
        this.prioridad = calcularPrioridadInicial();
    }
    
    private int calcularPrioridadInicial() {
        switch (tipo) {
            case EMERGENCIA: return 80;
            case CONTROL: return 50;
            case CURACION: return 50;
            case ODONTOLOGIA: return 30;
            default: return 0;
        }
    }
    
    public void actualizarPrioridad() {
        if (tipo == TipoConsulta.EMERGENCIA) {
            try {
                int tiempoEspera = (SimulacionCentroMedico.getHora() - tiempoLlegada); // minutos
                prioridad = Math.min(95, prioridad + tiempoEspera / 40);
                if (tiempoEspera > 240) {
                    //paso tiempo limite
                }
            } catch (Exception e) { 
               // Salta excepcion si no se ha inicializado la simulacion
            }
        }
    }
    
    // Getters
    public TipoConsulta getTipo() { return tipo; }
    public int getPrioridad() { return prioridad; }
    public String getIdPaciente() { return idPaciente; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getDuracionConsulta() { return duracionConsulta; }

    @Override
    public int compareTo(Consulta o) {
        // Prioridad más alta primero
        return Integer.compare(o.prioridad, this.prioridad);
    }
}
