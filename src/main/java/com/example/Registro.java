package com.example;

public class Registro {
    private static final String salida = "salida.txt";

    public static synchronized void log(String mensaje) {
        System.out.println(mensaje);
        ManejadorArchivos.escribirLinea(salida, mensaje);
    }
}
