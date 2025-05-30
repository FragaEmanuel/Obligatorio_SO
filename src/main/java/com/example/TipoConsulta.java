/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example;

enum TipoConsulta {
    URGENTE,
    CONTROL, 
    ODONTOLOGIA,
    CURACION,
    OTRO;


    public static TipoConsulta fromString(String tipo) {
        switch (tipo.toUpperCase()) {
            case "URGENTE":
                return URGENTE;
            case "CONTROL":
                return CONTROL;
            case "ODONTOLOGIA":
                return ODONTOLOGIA;
            case "CURACION":
                return CURACION; // Assuming "CURACION" is treated as "CONSULTA"
            default:
                return OTRO;
        }
    }
}
