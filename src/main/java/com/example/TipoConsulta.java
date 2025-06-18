package com.example;

public enum TipoConsulta {
    EMERGENCIA, CONTROL, CURACION, ODONTOLOGIA, ANALISIS, CARNE;

    public int duracionPorDefecto() {
        switch (this) {
            case EMERGENCIA: return 30;
            case CONTROL:    return 15;
            case CURACION:   return 10;
            case ODONTOLOGIA:return 20;
            case ANALISIS:   return 12;
            case CARNE:      return 8;
            default:         return 10;
        }
    }
}