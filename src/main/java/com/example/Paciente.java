package com.example;

public class Paciente {
    enum TipoPaciente {
        ADULTO, ANCIANO, EMBARAZADA, NIÑO
    }

    private String nombre;
    private TipoPaciente tipo;
    private Consulta consulta;


    public Paciente(String nombre, TipoPaciente tipo) {
        this.nombre = nombre;
        
    }

  

    public String getNombre() {
        return nombre;
    }

    public TipoPaciente getTipo() {
        return tipo;
    }

    public Consulta getConsulta() {
        return consulta;
    }
}
