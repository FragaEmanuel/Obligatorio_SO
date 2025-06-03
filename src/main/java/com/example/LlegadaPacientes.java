package com.example;

import java.util.List;

import com.example.Consulta.TipoConsulta;

public class LlegadaPacientes extends Thread {

    private final CentroMedico centroMedico;
    private List<Consulta> consultas;
    private final int cantidadconsultas;

    public LlegadaPacientes(CentroMedico centroMedico, List<Consulta> consultas) {
        this.centroMedico = centroMedico;
        this.cantidadconsultas = consultas.size();
        this.consultas = consultas;
    }

    @Override
    public void run() {
        CargarConsultasDelArchivo();
        
        while (SimulacionCentroMedico.getHora() < 720 && !consultas.isEmpty())   {

            Consulta consulta = consultas.get(0);
            if (consulta.getTiempoLlegada() >= SimulacionCentroMedico.getHora()) {
                consultas.remove(0);
                centroMedico.getRecepcionista().agregarConsulta(consulta);
                SimulacionCentroMedico.haypacientes.release();
            } 
        }
    }


    private void CargarConsultasDelArchivo() {
        ManejadorArchivosGenerico manejador = new ManejadorArchivosGenerico();
        String[] contenido = manejador.leerArchivo("Obligatorio_SO/src/main/java/com/example/Consultas1.txt");
        for (String linea : contenido) {
            String[] partes = linea.split(",");
            if (partes.length >= 2) {
                TipoConsulta tipoConsulta = TipoConsulta.valueOf(partes[1].trim().toUpperCase());
                String[] HORA_HM = partes[2].split(":");
                int hora = Integer.parseInt(HORA_HM[0].trim())*60 + Integer.parseInt(HORA_HM[1].trim());
                Consulta consulta = new Consulta(tipoConsulta, partes[0].trim(), hora, Integer.parseInt(partes[3].trim()));
                consultas.add(consulta);
            }
        }
        consultas.sort((c1, c2) -> Integer.compare(c1.getTiempoLlegada(), c2.getTiempoLlegada()));
    }
}