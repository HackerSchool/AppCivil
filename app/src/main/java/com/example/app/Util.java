package com.example.app;
/**
 * Esta classe contem funções utilitárias, que não se encaixam necessáriamente em nenhuma outra
 */
public class Util {
    /**
     * Esta função calcula a duração de um intervalo em minutos
     * @return minutos livres
     */
    public static int calcuclarDuracaoIntervaloEmMinutos(int[] intervalo) {
        return calcuclarDuracaoIntervaloEmMinutos(intervalo[0], intervalo[1], intervalo[2], intervalo[3]);
    }
    /**
     * Esta função calcula a duração de um intervalo em minutos
     * @return minutos livres
     */
    public static int calcuclarDuracaoIntervaloEmMinutos(int horaInicio, int minutoInicio, int horaFinal, int minutoFinal) {
        /*if(intervalo[0] == intervalo[2]) {
            //Começa e acaba à mesma hora, logo está disponível por menos de uma hora
            return intervalo[3] - intervalo[1];
        }*/

        int horasLivres = horaFinal - horaInicio;
        return horasLivres*60 - minutoInicio + minutoFinal;
    }
}
