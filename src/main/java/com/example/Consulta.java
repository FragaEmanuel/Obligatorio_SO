package com.example;

public class Consulta extends Thread implements Comparable<Consulta> {
    private final TipoConsulta tipo;
    private final int horaLlegada;
    private int prioridad;
    private final int idPaciente;
    private final int duracionConsulta;
    private boolean pendiente = true;
    private boolean recursosAdquiridos = false;

    public Consulta(TipoConsulta tipo, int idPaciente, int horaLlegada) {
        this.tipo = tipo;
        this.idPaciente = idPaciente;
        this.horaLlegada = horaLlegada;
        this.duracionConsulta = tipo.duracionPorDefecto();
        this.prioridad = calcularPrioridadInicial();
    }

    private int calcularPrioridadInicial() {
        return switch (tipo) {
            case EMERGENCIA -> 80;
            case ODONTOLOGIA -> 30;
            default -> 50;
        };
    }

    public void actualizarPrioridad() {
        int tiempoEspera = SimulacionCentroMedico.getHora() - horaLlegada;
        if (tiempoEspera > 120 && tipo == TipoConsulta.EMERGENCIA) {
            pendiente = false;
        }

        if (tipo == TipoConsulta.EMERGENCIA) {
            prioridad = Math.min(95, prioridad + tiempoEspera / 5);
        } else {
            prioridad = Math.min(95, prioridad + tiempoEspera / 10);
        }
    }

    public boolean esValida() {
        return pendiente;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public TipoConsulta getTipo() {
        return tipo;
    }

    public int getTiempoLlegada() {
        return horaLlegada;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setRecursosAdquiridos(boolean adquiridos) {
        this.recursosAdquiridos = adquiridos;
    }

    @Override
    public int compareTo(Consulta o) {
        return Integer.compare(o.getPrioridad(), this.getPrioridad()); // orden descendente
    }

    @Override
    public void run() {
        try {
            int horaInicio = SimulacionCentroMedico.getHora();
            if (horaInicio + duracionConsulta > SimulacionCentroMedico.DURACION_SIMULACION) {
                marcarPerdida("no hay tiempo suficiente para su consulta");
                return;
            }

            if (!pendiente) {
                marcarPerdida("cancelada por espera excesiva o interrupción");
                return;
            }

            System.out.println("- Hilo paciente " + idPaciente + " (" + tipo + ") INICIADO. Duración: " + duracionConsulta + " min");

            int minutosCompletados = 0;
            while (minutosCompletados < duracionConsulta) {
                synchronized (SimulacionCentroMedico.getLock()) {
                    int horaPrev = SimulacionCentroMedico.getHora();

                    System.out.println("+ Paciente " + idPaciente + " minuto " + (minutosCompletados + 1) + "/" + duracionConsulta);
                    SimulacionCentroMedico.hilosListos++;
                    SimulacionCentroMedico.getLock().notifyAll();

                    // Esperar hasta que el reloj avance un minuto
                    while (SimulacionCentroMedico.getHora() == horaPrev) {
                        SimulacionCentroMedico.getLock().wait();
                    }

                    minutosCompletados++;
                }
            }

            registrarAtendida();
            System.out.println("<<< Hilo paciente " + idPaciente + " FINALIZADO.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            marcarPerdida("interrumpido por error");
        } finally {
            if (recursosAdquiridos) liberarRecursos();
        }
    }


    private void liberarRecursos() {
        switch (tipo) {
            case EMERGENCIA -> {
                SimulacionCentroMedico.haysalaEmergencia.release();
                SimulacionCentroMedico.MedicosDisponibles.release();
                SimulacionCentroMedico.EnfermerosDisponibles.release();
            }
            case CONTROL, CARNE -> {
                SimulacionCentroMedico.consultaoriodisponibles.release();
                SimulacionCentroMedico.MedicosDisponibles.release();
                SimulacionCentroMedico.EnfermerosDisponibles.release();
            }
            case CURACION, ANALISIS -> {
                SimulacionCentroMedico.consultaoriodisponibles.release();
                SimulacionCentroMedico.EnfermerosDisponibles.release();
            }
            case ODONTOLOGIA -> {
                SimulacionCentroMedico.consultaoriodisponibles.release();
                SimulacionCentroMedico.MedicosDisponibles.release();
                SimulacionCentroMedico.EnfermerosDisponibles.release();
            }
        }
    }

    private void registrarAtendida() {
        synchronized (SimulacionCentroMedico.class) {
            SimulacionCentroMedico.consultasAtendidas++;
            SimulacionCentroMedico.atendidasPorTipo.merge(tipo, 1, Integer::sum);
            if (SimulacionCentroMedico.getHora() < 360) {
                SimulacionCentroMedico.atendidasTurno1++;
            } else {
                SimulacionCentroMedico.atendidasTurno2++;
            }
        }
    }

    private void marcarPerdida(String motivo) {
        System.out.println("/ Paciente " + idPaciente + " NO atendido (" + tipo + "): " + motivo + ".");
        pendiente = false;
        synchronized (SimulacionCentroMedico.class) {
            SimulacionCentroMedico.consultasPerdidas++;
            SimulacionCentroMedico.perdidasPorTipo.merge(tipo, 1, Integer::sum);
        }
    }
}