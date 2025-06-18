
package com.example.Util;

import java.util.Random;

import com.example.ManejadorArchivosGenerico;
import com.example.TipoConsulta;

public final class generadorDeConsultas {
    private generadorDeConsultas() {} // Evita instanciación


    public static void main(String[] args) {
        generarListaConsultasAleatorias("src/main/java/com/example/Util/Consultas1.csv");
    }

    public static void generarListaConsultasAleatorias(String nombreCompletoArchivo) {
        Random random = new Random();
        int cantidad = random.nextInt(200, 300);
        TipoConsulta[] tipos = TipoConsulta.values();
        String[] ListaConsultas = new String[cantidad];

        for (int i = 0; i < cantidad; i++) {
            TipoConsulta tipo = tipos[random.nextInt(tipos.length)];
            int idPaciente = i;
            int horaLlegada = random.nextInt(720);
            String horaReloj = String.format("%d:%02d", 8 + (horaLlegada / 60), horaLlegada % 60);

            String x = tipo + ";" + idPaciente + ";" + horaReloj;
            ListaConsultas[i] = x;
        }

        ManejadorArchivosGenerico.escribirArchivo(nombreCompletoArchivo, ListaConsultas);
    }
}