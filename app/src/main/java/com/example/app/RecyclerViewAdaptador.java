package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdaptador extends RecyclerView.Adapter<RecyclerViewAdaptador.ViewHolder>{
    private static final String TAG = "RecyclerViewAdaptador";

    private Context mContext;

    private List<Sala> salas; //Lista de salas a mostrar
    private List<String> stringsDisponibilidades; //Texto a apresentar com as disponibilidades

    ClickSalaCallback clickSalaCallback;

    public RecyclerViewAdaptador(Context Context) {
        this.mContext = Context;
        salas = new ArrayList<>();
        stringsDisponibilidades = new ArrayList<>();
    }

    public void updateSalasToShow(final int hora, final int minuto) {
        salas.clear();

        salas.addAll(Salas.salasList);

        Collections.sort(salas, new Comparator<Sala>() {
            @Override
            public int compare(Sala s1, Sala s2) {
                if(s1.numMinutosLivre < 15 && s2.numMinutosLivre < 15)
                    return 0;

                if(s1.estado == EstadoSala.DisponivelAgora && s2.estado == EstadoSala.DisponivelMaisTarde) {
                    if(s1.numMinutosLivre < 15)
                        return -1;
                    return 1;
                } else if(s1.estado == EstadoSala.DisponivelMaisTarde && s2.estado == EstadoSala.DisponivelAgora) {
                    if(s2.numMinutosLivre < 15)
                        return 1;
                    return -1;
                }

                if(s1.estado == EstadoSala.DisponivelAgora && s2.estado == EstadoSala.DisponivelAgora)
                    return s1.numMinutosLivre > s2.numMinutosLivre  ? 1 : -1;

                if(s1.estado == EstadoSala.DisponivelMaisTarde && s2.estado == EstadoSala.DisponivelMaisTarde)
                    return s1.numMinutosAteEstarLivre > s2.numMinutosAteEstarLivre ? 1 : -1;

                return 0;
            }
        });

        for(Sala sala : Salas.salasList) {
            String stringDisponibilidade;
            if(sala.lidaComSucesso)
                stringDisponibilidade = sala.obterDisponibilidadeString(hora, minuto);
            else
                stringDisponibilidade = "Erro a obter dados";

            stringsDisponibilidades.add(stringDisponibilidade);
        }


        //Avisa o adapter que a lista de salas mudou
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder; //É esta a parte que recicla, usa-se isto basicamente em todos os recyclerviews quase copy paste
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: chegou aqui"); //Debugging

        final Sala sala = salas.get(position);

        Glide.with(mContext) //Glide é uma biblioteca
                .asBitmap() //Diz ao glide q queremos como um bitmap
                .load(sala.urlFoto) //Dar load ao url
                .into(holder.image); //Dar load no holder

        holder.imageName.setText(sala.nome);//Definir o Nome que aparece ao lado da imagem

        holder.hora.setText(stringsDisponibilidades.get(position));//Definir o que vai aparecer por baixo do nome ^^


        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Definir o que acontece quando se carrega no Holder
                Log.d(TAG, "onClick: click on: " + sala.nome);
                //Toast.makeText(mContext, "Mostrar Horário da "+ sala.nome, Toast.LENGTH_SHORT).show();
                //Eventualmente Abrir outra Activity ?
                clickSalaCallback.onClick(sala);
            }


        });
    }


    @Override
    public int getItemCount() {
        return salas.size();
    }

    /**
     * Limpa a lista de salas
     */
    public void clear() {
        int numSalas = salas.size();
        salas.clear();
        stringsDisponibilidades.clear();
        notifyItemRangeRemoved(0, numSalas);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        RelativeLayout parentLayout;
        TextView imageName;
        TextView hora;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            hora = itemView.findViewById(R.id.hora);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    public interface ClickSalaCallback {
        void onClick(Sala sala);
    }
}
