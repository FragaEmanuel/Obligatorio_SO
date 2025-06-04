package com.example;


public class Consulta implements Comparable<Consulta> {
    public enum TipoConsulta { EMERGENCIA, CONTROL, CURACION, ODONTOLOGIA, ANALISIS, CARNE }
    
    private final TipoConsulta tipo;
    private final int tiempoLlegada;
    private int prioridad;
    private final String idPaciente;
    private final int duracionConsulta;
    private static final int TIEMPO_LIMITE_EMERGENCIA = 240; // minutos
    private boolean Valida = true; // Indica si la consulta es válida
    
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
            case ANALISIS: return 50;
            case CARNE: return 50;
            case ODONTOLOGIA: return 30;
            default: return 0;
        }
    }
    
    public void actualizarPrioridad() {
        if (this.tipo == TipoConsulta.EMERGENCIA) {
            try {
                int tiempoEspera = (SimulacionCentroMedico.getHora() - tiempoLlegada); // minutos
                this.prioridad = Math.min(95, prioridad + tiempoEspera / 5); // Aumenta prioridad cada 5 minutos
                if (tiempoEspera > 120) {
                     this.Valida = false; // Consulta no válida si excede el tiempo límit
                }
            } catch (Exception e) { 
               // Salta excepcion si no se ha inicializado la simulacion
            }
        } else {
             try {
                int tiempoEspera = (SimulacionCentroMedico.getHora() - tiempoLlegada); 
                this.prioridad = Math.min(95, prioridad + (tiempoEspera / 10)); //Aumenta prioridad cada 10 minutos
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
