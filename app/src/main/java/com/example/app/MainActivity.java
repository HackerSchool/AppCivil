package com.example.app;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity  implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "MainActivity";

    //Variáveis

    RequestQueue volleyQueue;

    RecyclerView recyclerView;
    RecyclerViewAdaptador recyclerViewAdapter;

    ProgressBar progressBar;
    TextView hora_atual;
    Button button;
    Button botao_hora_atual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hora_atual = findViewById(R.id.hora_escolhida);
        button = findViewById(R.id.button);
        botao_hora_atual = findViewById(R.id.botao_hora);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //Inicializa a lista de salas se ainda não tiver sido
        if(Salas.salas == null) {
            Salas.initSalas();
        }

        volleyQueue = Volley.newRequestQueue(this);

        Calendar rightNow = Calendar.getInstance();
        int hora_obtida = rightNow.get(Calendar.HOUR_OF_DAY); //Hora atual inicial (quando a app é aberta
        int minuto_obtido = rightNow.get(Calendar.MINUTE);

        String minuto_atual_inicial = formato_hora(minuto_obtido);
        String hora_atual_inicial = formato_hora(hora_obtida);

        hora_atual.setText("Hora: " +hora_atual_inicial+":"+minuto_atual_inicial);

        Log.d(TAG, "onCreate: Started");

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

                initRecyclerView();

                horaAlterada(hora_obtida, minuto_obtido);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment escolherhora = new EscolherHora();
                escolherhora.show(getSupportFragmentManager(), "time picker");
            }
        });

        initRecyclerView();
        horaAlterada(hora_obtida, minuto_obtido);
    }

    /**
     * Quando se escolhe uma hora na coisinha para escolher
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
        TextView hora_escolhida = findViewById(R.id.hora_escolhida); //Hora escolhida no Dialog
        String hora_nova = formato_hora(hourOfDay);
        String minuto_novo = formato_hora(minute);
        hora_escolhida.setText("Hora: " + hora_nova + ":"+ minuto_novo);

        horaAlterada(hourOfDay, minute);
    }

    /**
     * Faz aparecer ou desaparecer a progressbar
     * @param loading
     */
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * É chamada quando se altera a hora. Atualiza a lista
     */
    private void horaAlterada(final int hora, final int minuto) {
        Salas.resetRequestsObterEstado(volleyQueue);

        Salas.getSalaData(new Date(), volleyQueue, new Salas.ObterSalasTerminouCallback() {
            @Override
            public void concluido() {
                Salas.updateSalaStatesForHour(hora, minuto);
                //Atualiza a lista
                recyclerViewAdapter.updateSalasToShow(hora, minuto);
                setLoading(false);
            }
        });

        recyclerViewAdapter.clear();
        setLoading(true);
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

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: A iniciar a Recycler View");
        recyclerView = findViewById(R.id.recycler_view);
        recyclerViewAdapter = new RecyclerViewAdaptador(this);
        recyclerViewAdapter.clickSalaCallback = new RecyclerViewAdaptador.ClickSalaCallback() {
            @Override
            public void onClick(Sala sala) {
                String url = String.format("https://fenix.tecnico.ulisboa.pt/spaces-view/schedule/%s", sala.id);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        };

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

