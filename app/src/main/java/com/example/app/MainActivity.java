package com.example.app;

import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.Calendar;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity  implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "MainActivity";

    //Variáveis

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mHoras = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ListView listView;
    private String[] navdrawer;
    private ActionBarDrawerToggle drawerListener;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar rightNow = Calendar.getInstance();
        int hora_obtida = rightNow.get(Calendar.HOUR_OF_DAY); //Hora atual inicial (quando a app é aberta
        int minuto_obtido = rightNow.get(Calendar.MINUTE);

        String minuto_atual_inicial = formato_hora(minuto_obtido);
        String hora_atual_inicial = formato_hora(hora_obtida);

        TextView hora_atual = findViewById(R.id.hora_escolhida);
        hora_atual.setText("Hora: " +hora_atual_inicial+":"+minuto_atual_inicial);

        Log.d(TAG, "onCreate: Started");
        initImageBitmaps();

        final Button button = findViewById(R.id.button);

        Button botao_hora_atual = findViewById(R.id.botao_hora);

        botao_hora_atual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar rightNow = Calendar.getInstance();
                int hora_obtida = rightNow.get(Calendar.HOUR_OF_DAY);//Hora atual do telemóvel (sem os algarismos certos)
                int minuto_obtido = rightNow.get(Calendar.MINUTE);//Minuto atual do telemóvel (sem os algarismos certos)

                String minuto_atual_novo = formato_hora(minuto_obtido);
                String hora_atual_nova = formato_hora(hora_obtida);

                TextView hora_atual = findViewById(R.id.hora_escolhida);
                hora_atual.setText("Hora: " +hora_atual_nova+":"+minuto_atual_novo);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment escolherhora = new EscolherHora();
                escolherhora.show(getSupportFragmentManager(), "time picker");
            }
        });

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView hora_escolhida = findViewById(R.id.hora_escolhida); //Hora escolhida no Dialog
        String hora_nova = formato_hora(hourOfDay);
        String minuto_novo = formato_hora(minute);
        hora_escolhida.setText("Hora: " + hora_nova + ":"+ minuto_novo);

    }

    /**
     * Esta função devolve o inteiro num formato de hora;
     * Pôr o inteiro com algarismos certos (0 passa para 00, 1 para 01 etc.)
     * @param num_inteiro a hora ou minuto escolhido escolhida
     * @return O número inteiro no formato de hora
     */
    public String formato_hora(int num_inteiro){
        if (num_inteiro == 0) {
            return "00";
        } else {
            if ((int) (Math.log10(num_inteiro) + 1) == 1) {
                return("0"+num_inteiro);
            }
            else{
                return String.valueOf(num_inteiro);
            }
        }
    }



    private void initImageBitmaps(){
        Log.d(TAG, "initImageBitmaps: a preparar as imagens");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V0.01");
        mHoras.add("3");

        mImageUrls.add("http://chemulisboa.weebly.com/uploads/2/6/1/8/26189991/7524493.jpg?1401722318");
        mNames.add("Sala V0.02");
        mHoras.add("2");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V0.03");
        mHoras.add("1");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V0.07");
        mHoras.add("2");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V0.14");
        mHoras.add("3");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V1.20");
        mHoras.add("5");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V1.18");
        mHoras.add("3");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V1.01");
        mHoras.add("4");

        mImageUrls.add("http://www.civil.ist.utl.pt/~arq/images/salanormal1.jpg");
        mNames.add("Sala V1.14");
        mHoras.add("2");

        initRecyclerView();


    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: A iniciar a Recycler View");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdaptador adaptador = new RecyclerViewAdaptador(this, mNames, mImageUrls, mHoras);
        recyclerView.setAdapter(adaptador);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



    }

    public final class Sala {
        String id, nome;
        String urlFoto;

        ArrayList<String> eventos;
        ArrayList<int[]> freeTimes;

        //Indica se foi lida com sucesso
        boolean lidaComSucesso;
        //Indica o erro que ocorreu ao ler a sala
        String erroALer;

        public Sala(String id, String nome) {
            this.id = id;
            this.nome = nome;
            this.freeTimes = freeTimes;

            lidaComSucesso = true;
        }

        /**
         * Esta função devolve o texto a ser apresentado a dizer durante quanto tempo a sala vai estar livre ou
         *  a partir de quando
         * @param hora a hora escolhida
         * @param minuto o minuto escolhido
         * @return O texto a ser apresentado
         */
        public String obterDisponibilidadeString(int hora, int minuto) {
//comentario de teste
//Comentario do miguel :D
            String minuto_novo = formato_hora(minuto);
            String hora_nova = formato_hora(hora);
            return "Disponivel por "+hora_nova+":"+minuto_novo+" horas";
        }

        /**
         *
         * @param freeTimes
         */
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
}

