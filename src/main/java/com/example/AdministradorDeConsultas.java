package com.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class AdministradorDeConsultas {

    private final String nombre;
    private final Queue<Consulta> consultasPendientes = new PriorityBlockingQueue<>();

    public AdministradorDeConsultas(String nombre) {
        this.nombre = nombre;
    }

    public void agregarConsulta(Consulta c) {
        consultasPendientes.add(c);
    }

    public List<Consulta> obtenerConsultasNoLanzadas() {
        return new ArrayList<>(consultasPendientes);
    }

    //Actualiza la prioridad de las consultas activas en base al minuto actual
    //Las consultas que aún no han llegado (tiempoLlegada < minutoActual) no se actualizan.
    public void actualizarPrioridadConsultasActivas(int minutoActual) {
        for (Consulta c : consultasPendientes) {
            if (c.getTiempoLlegada() >= minutoActual) {
                c.actualizarPrioridad();
            }
        }
    }


    public List<Consulta> atenderConsultasDisponibles() {
        List<Consulta> lanzadas = new ArrayList<>();
        Iterator<Consulta> iterator = consultasPendientes.iterator();

        while (iterator.hasNext()) {
            Consulta consulta = iterator.next();

            //  Evita lanzar la consulta si aún no ha llegado (tiempoLlegada > horaSimulada)
            if (consulta.getTiempoLlegada() <= SimulacionCentroMedico.getHora()) {
                continue;
            }

            //  Evita lanzar la consulta si terminaría después del cambio de turno (14:00, minuto 360)
            int horaActual = SimulacionCentroMedico.getHora();
            int horaFin = horaActual + consulta.getDuracionConsulta();
            if (horaActual < 360 && horaFin > 360) { // compara si hay una consulta que empiece antes del cambio de turno y termine despues de este
                continue; // No la atiende todavía, se reintentará después del cambio de turno
            }

            //  Consulta ya inválida
            if (!consulta.esValida()) {
                iterator.remove(); // Ya está descartada
                continue;
            }

            //  Intentar reservar recursos
            if (intentarReservarRecursos(consulta)) {
                consulta.setRecursosAdquiridos(true);
                consulta.start();
                iterator.remove(); // Ya se lanzó
                lanzadas.add(consulta);
            }
        }

        return lanzadas;
    }

    private boolean intentarReservarRecursos(Consulta c) {
        TipoConsulta tipo = c.getTipo();

        boolean tieneMedico = false, tieneOdontologo = false, tieneEnfermeroFijo = false;
        boolean tieneEnfermeroRotativo = false, tieneConsultorio = false, tieneSalaEmergencia = false;

        try {
            if (!SimulacionCentroMedico.ObtenerRecursos.tryAcquire()) return false;

            switch (tipo) {
                case EMERGENCIA -> {
                    if (SimulacionCentroMedico.haysalaEmergencia.tryAcquire()) {
                        tieneSalaEmergencia = true;
                        if (SimulacionCentroMedico.MedicosDisponibles.tryAcquire()) {
                            tieneMedico = true;
                            if (SimulacionCentroMedico.EnfermerosRotativos.tryAcquire()) {
                                tieneEnfermeroRotativo = true;
                                return true;
                            }
                        }
                    }
                }

                case CURACION, ANALISIS -> {
                    if (SimulacionCentroMedico.consultaoriodisponibles.tryAcquire()) {
                        tieneConsultorio = true;

                        if (SimulacionCentroMedico.cantidadEnfermerosFijos > 0) {
                            if (SimulacionCentroMedico.EnfermerosFijos.tryAcquire()) {
                                tieneEnfermeroFijo = true;
                                c.setTieneEnfermeroFijo(true);
                                return true;
                            } else {
                                SimulacionCentroMedico.consultaoriodisponibles.release();
                            }
                        } else {
                            if (SimulacionCentroMedico.EnfermerosRotativos.tryAcquire()) {
                                tieneEnfermeroRotativo = true;
                                c.setTieneEnfermeroRotativo(true);
                                return true;
                            } else {
                                SimulacionCentroMedico.consultaoriodisponibles.release();
                            }
                        }
                    }
                }

                case CONTROL, CARNE -> {
                    if (SimulacionCentroMedico.consultaoriodisponibles.tryAcquire()) {
                        tieneConsultorio = true;
                        if (SimulacionCentroMedico.MedicosDisponibles.tryAcquire()) {
                            tieneMedico = true;
                            if (SimulacionCentroMedico.EnfermerosRotativos.tryAcquire()) {
                                tieneEnfermeroRotativo = true;
                                return true;
                            }
                        }
                    }
                }

                case ODONTOLOGIA -> {
                    if (SimulacionCentroMedico.consultaoriodisponibles.tryAcquire()) {
                        tieneConsultorio = true;
                        if (SimulacionCentroMedico.OdontologosDisponibles.tryAcquire()) {
                            tieneOdontologo = true;
                            if (SimulacionCentroMedico.EnfermerosRotativos.tryAcquire()) {
                                tieneEnfermeroRotativo = true;
                                return true;
                            }
                        }
                    }
                }
            }

            // Si fallo, liberamos
            if (tieneSalaEmergencia) SimulacionCentroMedico.haysalaEmergencia.release();
            if (tieneConsultorio) SimulacionCentroMedico.consultaoriodisponibles.release();
            if (tieneMedico) SimulacionCentroMedico.MedicosDisponibles.release();
            if (tieneOdontologo) SimulacionCentroMedico.OdontologosDisponibles.release();
            if (tieneEnfermeroFijo) SimulacionCentroMedico.EnfermerosFijos.release();
            if (tieneEnfermeroRotativo) SimulacionCentroMedico.EnfermerosRotativos.release();

        } finally {
            SimulacionCentroMedico.ObtenerRecursos.release();
        }

        return false;
    }
}