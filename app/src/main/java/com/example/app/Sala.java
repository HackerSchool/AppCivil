package com.example.app;
import java.util.ArrayList;

public final class Sala {
    private static final String FOTO_DEFAULT = "http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg";
    String id, nome;
    String urlFoto;
    ArrayList<SalaEvento> eventos;

    //Comtem intervalos enquanto vai estar livre
    ArrayList<int[]> freeTimes;

    //Indica se foi lida com sucesso
    boolean lidaComSucesso;
    //Indica o erro que ocorreu ao ler a sala
    String erroALer;

    EstadoSala estado;
    int numMinutosLivre;
    int numMinutosAteEstarLivre;


    public Sala(String id, String nome) {
        this.id = id;
        this.nome = nome;
        urlFoto = FOTO_DEFAULT;

        lidaComSucesso = true;
    }

    public Sala(String id, String nome, String urlFoto) {
        this.id = id;
        this.nome = nome;
        this.urlFoto = urlFoto;
    }

    /**
     * Esta função devolve o texto a ser apresentado a dizer durante quanto tempo a sala vai estar livre ou
     *  a partir de quando
     * @param hora a hora escolhida
     * @param minuto o minuto escolhido
     * @return O texto a ser apresentado
     */
    public String obterDisponibilidadeString(int hora, int minuto) {
        for(int i = 0; i<freeTimes.size(); i++) {
            int[] intervalo = freeTimes.get(i);
            if(hora > intervalo[0] || (hora == intervalo[0] && minuto >= intervalo[1])) {
                //Se a hora atual é depois do inicio deste intervalo
                if(hora < intervalo[2] || (hora == intervalo[2] && minuto <= intervalo[3])) {
                    //Se a hora atual é antes do final deste intervalo
                    //!!Está a meio de um tempo livre!!
                    int numMinutosLivre = Util.calcuclarDuracaoIntervaloEmMinutos(hora, minuto, intervalo[2], intervalo[3]);

                    return String.format("Disponível por mais %s.", formatMinutesToDisplay(numMinutosLivre));
                }
            }

            //Nào está a meio deste tempo livre
            if(intervalo[0] > hora || (intervalo[0] == hora && intervalo[1] > minuto)) {
                //Se o intervalo livre já começa depois da hora atual
                int numMinutosAteEstarLivre = Util.calcuclarDuracaoIntervaloEmMinutos(hora, minuto, intervalo[0], intervalo[1]);
                int numMinutosLivre = Util.calcuclarDuracaoIntervaloEmMinutos(intervalo);

                return String.format("Disponível daqui a %s por %s.",
                        formatMinutesToDisplay(numMinutosAteEstarLivre),
                        formatMinutesToDisplay(numMinutosLivre));
            }
        }
        return "??";
    }

    /**
     *
     * @param hora
     * @param minuto
     * @return Devolve um objeto cujo primeiro elemento é um EstadoSala. O segundo é o numero de minutos livre. O terciro é o numero de minutos ate estar livre
     */
    public void updateEstadoAtual(int hora, int minuto) {
        for(int i = 0; i<freeTimes.size(); i++) {
            int[] intervalo = freeTimes.get(i);
            if(hora > intervalo[0] || (hora == intervalo[0] && minuto >= intervalo[1])) {
                //Se a hora atual é depois do inicio deste intervalo
                if(hora < intervalo[2] || (hora == intervalo[2] && minuto <= intervalo[3])) {
                    //Se a hora atual é antes do final deste intervalo
                    //!!Está a meio de um tempo livre!!
                    int numMinutosLivre = Util.calcuclarDuracaoIntervaloEmMinutos(hora, minuto, intervalo[2], intervalo[3]);

                    estado = EstadoSala.DisponivelAgora;
                    this.numMinutosLivre = numMinutosLivre;
                    this.numMinutosAteEstarLivre = 0;
                }
            }

            //Nào está a meio deste tempo livre
            if(intervalo[0] > hora || (intervalo[0] == hora && intervalo[1] > minuto)) {
                //Se o intervalo livre já começa depois da hora atual
                int numMinutosAteEstarLivre = Util.calcuclarDuracaoIntervaloEmMinutos(hora, minuto, intervalo[0], intervalo[1]);
                int numMinutosLivre = Util.calcuclarDuracaoIntervaloEmMinutos(intervalo);

                estado = EstadoSala.DisponivelMaisTarde;
                this.numMinutosLivre = numMinutosLivre;
                this.numMinutosAteEstarLivre = numMinutosAteEstarLivre;
            }
        }
    }

    /**
     * Mostra como x horas e y minutos
     * @param minutesTotal minutos totais
     * @return texto a mostrar
     */
    private String formatMinutesToDisplay(int minutesTotal) {
        if(minutesTotal == 1)
            return "1 minuto";
        if(minutesTotal < 60)
            return String.format("%d minutos", minutesTotal);

        String message = null;
        int minutes = minutesTotal % 60;
        int hours = minutesTotal / 60;

        if (hours == 1)
            message = "1 hora";
        else
            message = String.format("%d horas", hours);

        if(minutes != 0)
            //Se não forem horas certas
            if(minutes == 1)
                message += " e 1 minuto";
            else
                message += String.format(" e %d minutos", minutes);

        return message;
    }


    public void setFreeTimes(ArrayList<int[]> freeTimes) {
        this.freeTimes = freeTimes;
    }

    /**
     * Marca que houve um erro a ler os dados desta sala
     * @param ex Erro
     */
    public void setError(Exception ex) {
        lidaComSucesso = false;
        erroALer = ex.getMessage();
    }
}
