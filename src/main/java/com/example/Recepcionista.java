package com.example;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Recepcionista {
    private final String nombre;
    private final Queue<Consulta> consultasPendientes = new PriorityBlockingQueue<>();

    public Recepcionista(String nombre) {
        this.nombre = nombre;
    }

    public void agregarConsulta(Consulta c) {
        consultasPendientes.add(c);
    }

    public void agregarConsultasDelMinuto(int minutoActual) {
        List<Consulta> reinsertar = new ArrayList<>();

        while (!consultasPendientes.isEmpty()) {
            Consulta c = consultasPendientes.peek();
            if (c.getTiempoLlegada() <= minutoActual) {
                c.actualizarPrioridad();
                reinsertar.add(consultasPendientes.poll());
            } else {
                break;
            }
        }

        consultasPendientes.addAll(reinsertar);
    }

    public List<Consulta> atenderConsultasDisponibles() {
        List<Consulta> lanzadas = new ArrayList<>();

        // Hacemos una copia para evitar modificación concurrente
        List<Consulta> intentadas = new ArrayList<>(consultasPendientes);

        for (Consulta consulta : intentadas) {
            if (!consulta.esValida()) {
                consultasPendientes.remove(consulta);
                continue;
            }

            if (intentarReservarRecursos(consulta)) {
                consulta.setRecursosAdquiridos(true);
                consulta.start();
                consultasPendientes.remove(consulta);
                lanzadas.add(consulta);
            }
        }

        return lanzadas;
    }

    private boolean intentarReservarRecursos(Consulta c) {
        TipoConsulta tipo = c.getTipo();

        boolean tieneMedico = false, tieneEnfermero = false, tieneConsultorio = false, tieneSalaEmergencia = false;

        try {
            if (!SimulacionCentroMedico.ObtenerRecursos.tryAcquire()) return false;

            switch (tipo) {
                case EMERGENCIA -> {
                    if (SimulacionCentroMedico.haysalaEmergencia.tryAcquire()) {
                        tieneSalaEmergencia = true;
                        if (SimulacionCentroMedico.MedicosDisponibles.tryAcquire()) {
                            tieneMedico = true;
                            if (SimulacionCentroMedico.EnfermerosDisponibles.tryAcquire()) {
                                tieneEnfermero = true;
                                return true;
                            }
                        }
                    }
                }
                case CURACION, ANALISIS -> {
                    if (SimulacionCentroMedico.consultaoriodisponibles.tryAcquire()) {
                        tieneConsultorio = true;
                        if (SimulacionCentroMedico.EnfermerosDisponibles.tryAcquire()) {
                            tieneEnfermero = true;
                            return true;
                        }
                    }
                }
                case CONTROL, CARNE, ODONTOLOGIA -> {
                    if (SimulacionCentroMedico.consultaoriodisponibles.tryAcquire()) {
                        tieneConsultorio = true;
                        if (SimulacionCentroMedico.MedicosDisponibles.tryAcquire()) {
                            tieneMedico = true;
                            if (SimulacionCentroMedico.EnfermerosDisponibles.tryAcquire()) {
                                tieneEnfermero = true;
                                return true;
                            }
                        }
                    }
                }
            }

            // Si no se pudo completar, liberar los recursos adquiridos parciales
            if (tieneSalaEmergencia) SimulacionCentroMedico.haysalaEmergencia.release();
            if (tieneConsultorio) SimulacionCentroMedico.consultaoriodisponibles.release();
            if (tieneMedico) SimulacionCentroMedico.MedicosDisponibles.release();
            if (tieneEnfermero) SimulacionCentroMedico.EnfermerosDisponibles.release();

        } finally {
            SimulacionCentroMedico.ObtenerRecursos.release();
        }

        return false;
    }
}