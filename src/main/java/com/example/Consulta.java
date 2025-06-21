package com.example;


public class Consulta extends Thread implements Comparable<Consulta> {
    private final TipoConsulta tipo;
    private final int horaLlegada;
    private int prioridad;
    private final int idPaciente;
    private final int duracionConsulta;
    private boolean pendiente = true;

    private boolean analisisSangre;
    private boolean analisisOrina;
    private boolean informeOdontologico;

    public Consulta(TipoConsulta tipo, int idPaciente, int horaLlegada) {
        this.tipo = tipo;
        this.idPaciente = idPaciente;
        this.horaLlegada = horaLlegada;
        this.duracionConsulta = tipo.duracionPorDefecto();
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
                int tiempoEspera = (SimulacionCentroMedico.getHora() - horaLlegada); // minutos
                this.prioridad = Math.min(95, prioridad + tiempoEspera / 5); // Aumenta prioridad cada 5 minutos
                if (tiempoEspera > 120) {
                    this.pendiente = false; // Consulta no válida si excede el tiempo límit
                }
            } catch (Exception e) {
                // Salta excepcion si no se ha inicializado la simulacion
            }
        } else {
            try {
                int tiempoEspera = (SimulacionCentroMedico.getHora() - horaLlegada);
                this.prioridad = Math.min(95, prioridad + (tiempoEspera / 10)); //Aumenta prioridad cada 10 minutos
            } catch (Exception e) {
                // Salta excepcion si no se ha inicializado la simulacion
            }
        }
    }

    // Getters
    public TipoConsulta getTipo() { return tipo; }
    public int getPrioridad() { return prioridad; }
    public int getIdPaciente() { return idPaciente; }
    public int getTiempoLlegada() { return horaLlegada; }
    public int getDuracionConsulta() { return duracionConsulta; }

    @Override
    public int compareTo(Consulta o) {
        // Prioridad más alta primero
        int cmp = Integer.compare(this.horaLlegada, o.horaLlegada); // Primero comparar por tiempo de llegada
        if (cmp == 0) {
            cmp = Integer.compare(this.idPaciente, o.idPaciente); // Si tiempos iguales, comparar por prioridad
        }

        return cmp;
    }

    public boolean EsValida() {
        return pendiente;
    }

    @Override
    public void run() {
        try {
            int horaActual = SimulacionCentroMedico.getHora();
            if (horaActual + duracionConsulta > SimulacionCentroMedico.DURACION_SIMULACION) {
                this.pendiente = false;
                System.out.println("/ Paciente " + idPaciente + " NO atendido: no hay tiempo suficiente para su consulta (" + tipo + ").");

                synchronized (SimulacionCentroMedico.class) {
                    SimulacionCentroMedico.consultasPerdidas++;
                    SimulacionCentroMedico.perdidasPorTipo.merge(tipo, 1, Integer::sum);
                }
                return;
            }

            // Intentar adquirir los recursos necesarios según el tipo de consulta
            boolean recursosObtenidos = false;
            while (!recursosObtenidos && pendiente) {
                try {
                    switch (tipo) {
                        case EMERGENCIA:
                            SimulacionCentroMedico.haysalaEmergencia.acquire();
                            SimulacionCentroMedico.medicosdisponibles.acquire();
                            SimulacionCentroMedico.enfermerosdisponibles.acquire();
                            recursosObtenidos = true;
                            break;
                        case CONTROL:
                        case CARNE:
                            SimulacionCentroMedico.consultaoriodisponibles.acquire();
                            SimulacionCentroMedico.medicosdisponibles.acquire();
                            SimulacionCentroMedico.enfermerosdisponibles.acquire();
                            recursosObtenidos = true;
                            break;
                        case CURACION:
                            SimulacionCentroMedico.consultaoriodisponibles.acquire();
                            SimulacionCentroMedico.enfermerosdisponibles.acquire();
                            recursosObtenidos = true;
                            break;
                        case ANALISIS:
                            SimulacionCentroMedico.consultaoriodisponibles.acquire();
                            SimulacionCentroMedico.enfermerosdisponibles.acquire();
                            recursosObtenidos = true;
                            break;
                        case ODONTOLOGIA:
                            // Implementa lógica específica si tienes recursos para odontología
                            recursosObtenidos = true;
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pendiente = false;
                    break;
                }
            }
            SimulacionCentroMedico.ObtenerRecursos.release();

            // Simular la atención de la consulta
            if (recursosObtenidos && pendiente) {
                System.out.println("- Hilo paciente " + idPaciente + " (" + tipo + ") INICIADO. Duración: " + duracionConsulta + " min");

                if (tipo != TipoConsulta.CARNE || (analisisOrina && analisisSangre)){
                    for (int i = 0; i < duracionConsulta; i++) {
                        synchronized (SimulacionCentroMedico.getLock()) {
                            SimulacionCentroMedico.getLock().wait();
                            SimulacionCentroMedico.hilosListos++;
                            System.out.println("+ Paciente " + idPaciente + " minuto " + (i + 1) + "/" + duracionConsulta);
                            SimulacionCentroMedico.getLock().notifyAll(); // Notifica al principal que terminó el minuto
                            SimulacionCentroMedico.getLock().wait();
                        }
                    }
                }
                pendiente = false;
                // Registrar consulta atendida
                synchronized (SimulacionCentroMedico.class) {
                    SimulacionCentroMedico.consultasAtendidas++;
                    SimulacionCentroMedico.atendidasPorTipo.merge(tipo, 1, Integer::sum);
                }
                System.out.println("<<< Hilo paciente " + idPaciente + " FINALIZADO.");
            } else if (!pendiente) {
                // Registrar consulta perdida
                synchronized (SimulacionCentroMedico.class) {
                    SimulacionCentroMedico.consultasPerdidas++;
                    SimulacionCentroMedico.perdidasPorTipo.merge(tipo, 1, Integer::sum);
                }
                System.out.println(">>> Hilo paciente " + idPaciente + " cancelado por exceso de espera.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("--- Hilo paciente " + idPaciente + " interrumpido.");

        } finally {
            // Liberar recursos según el tipo de consulta
            switch (tipo) {
                case EMERGENCIA:
                    SimulacionCentroMedico.haysalaEmergencia.release();
                    SimulacionCentroMedico.medicosdisponibles.release();
                    SimulacionCentroMedico.enfermerosdisponibles.release();
                    break;
                case CONTROL:
                case CARNE:
                    SimulacionCentroMedico.consultaoriodisponibles.release();
                    SimulacionCentroMedico.medicosdisponibles.release();
                    SimulacionCentroMedico.enfermerosdisponibles.release();
                    break;
                case CURACION:
                    SimulacionCentroMedico.consultaoriodisponibles.release();
                    SimulacionCentroMedico.enfermerosdisponibles.release();
                    break;
                case ANALISIS:
                    analisisSangre = true;
                    analisisOrina = true;
                    SimulacionCentroMedico.consultaoriodisponibles.release();
                    SimulacionCentroMedico.enfermerosdisponibles.release();
                    break;
                case ODONTOLOGIA:
                    // Libera recursos específicos si corresponde
                    break;
            }
        }
    }
}
