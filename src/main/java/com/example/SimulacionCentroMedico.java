import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.example.CentroMedico;



public class SimulacionCentroMedico {


    private static int Hora;

    static Semaphore haysala = new Semaphore(1);
    static Semaphore haysalaEmergencia = new Semaphore(1);
    static Semaphore medicosdisponibles;
    static Semaphore enfermerosdisponibles;
    static Semaphore pacientesparacurar = new Semaphore(0);
    static Semaphore haypacientes = new Semaphore(0);
    static Semaphore genteParaAtender = new Semaphore(0);

    public static int getHora() {
        return Hora;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Número de médicos: ");
        int medicos = scanner.nextInt();
        medicosdisponibles = new Semaphore(medicos);
        
        
        System.out.print("Número de enfermeros: ");
        int enfermeros = scanner.nextInt();
        enfermerosdisponibles = new Semaphore(enfermeros);
       
        System.out.print("¿Sala de emergencias reservada? (true/false): ");
        boolean salaEmergencia = scanner.nextBoolean();
        
        CentroMedico centro = new CentroMedico(medicos, enfermeros, salaEmergencia);
    
        while (Hora <= 720) {
            
            Hora++;
        }
    }
}
