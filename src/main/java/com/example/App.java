package com.example;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        pruebapriorbaja prueba = new pruebapriorbaja();
        pruebaprioralta prueba2 = new pruebaprioralta();
        pruebapriorbaja prueba3 = new pruebapriorbaja();
        pruebaprioralta prueba4 = new pruebaprioralta();
        pruebapriorbaja prueba5 = new pruebapriorbaja();
        pruebaprioralta prueba6 = new pruebaprioralta();
        pruebapriorbaja prueba7 = new pruebapriorbaja();
        pruebaprioralta prueba8 = new pruebaprioralta();
        prueba2.start();
        prueba.start();
        prueba3.start();
        prueba4.start();
        prueba5.start();
        prueba6.start();
        prueba7.start();
        prueba8.start();

    }

}


class pruebapriorbaja extends Thread{

    public pruebapriorbaja() {
        this.setPriority(5);
    }

    public void run() {
        System.out.println("Prioridad baja");
    
    }
}

class pruebaprioralta extends Thread{

    public pruebaprioralta() {
        this.setPriority(10);
    }

    public void run() {
            System.out.println("Prioridad alta");
    }
}

