package com.example;//package com.example;

import java.util.ArrayList;
import java.util.List;
 // En realidad no usamos main sino que usamos SimulacionCentroMedico
public class Main {
    public static void main(String[] args) {
       CentroMedico centro = new CentroMedico(2,2,true);
        List<String> lineas = ManejadorArchivos.leerLineas("entrada.txt");
        List<Paciente> pacientes = new ArrayList<>();
        int id = 1;

        for (String linea : lineas) {
            String[] partes = linea.split(";");
            int minutos = Integer.parseInt(partes[0]);
            String nombre = partes[1];
            Consulta.TipoConsulta tipo = Consulta.TipoConsulta.valueOf(partes[2].toUpperCase());
            Consulta consulta = new Consulta(tipo, "P" + id++, minutos);
            pacientes.add(new Paciente(nombre, consulta, centro));
        }

        centro.iniciar(pacientes.toArray(new Paciente[0]), 2);
    }
}
