package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.Button;
import  java.io.File;
import  android.net.Uri;


public class RecyclerViewAdaptador extends RecyclerView.Adapter<RecyclerViewAdaptador.ViewHolder>{
    private static final String TAG = "RecyclerViewAdaptador";

    private Context mContext;

    private List<Sala> salas; //Lista de salas a mostrar
    private List<String> stringsDisponibilidades; //Texto a apresentar com as disponibilidades
    private List<String> stringsIds; //Ids de cada sala, para serem identificadas

    ClickSalaCallback clickSalaCallback;


    public RecyclerViewAdaptador(Context Context) {
        this.mContext = Context;
        salas = new ArrayList<>();
        stringsDisponibilidades = new ArrayList<>();
        stringsIds = new ArrayList<>();
    }

    public void updateSalasToShow(final int hora, final int minuto) {
        salas.clear();

        salas.addAll(Salas.salasList);

        Collections.sort(salas, new Comparator<Sala>() {
            @Override
            public int compare(Sala s1, Sala s2) {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
                String value1 = sharedPreferences.getString(s1.id, "nfav");
                String value2 = sharedPreferences.getString(s2.id, "nfav");

                if(value1.equals("fav")) {
                    return -1;
                }
                if(value2.equals("fav")) {
                    return 1;
                }

                if(s1.numMinutosLivre < 15 && s2.numMinutosLivre < 15){
                    return 0;}

                if(s1.estado == EstadoSala.DisponivelAgora && s2.estado == EstadoSala.DisponivelMaisTarde) {
                    if(s1.numMinutosLivre < 15){
                        return -1;}
                    return 1;
                } else
                    if(s1.estado == EstadoSala.DisponivelMaisTarde && s2.estado == EstadoSala.DisponivelAgora) {
                    if(s2.numMinutosLivre < 15){
                        return 1;}
                    return -1;
                }

                if(s1.estado == EstadoSala.DisponivelAgora && s2.estado == EstadoSala.DisponivelAgora){
                    return s1.numMinutosLivre > s2.numMinutosLivre  ? 1 : -1;}

                if(s1.estado == EstadoSala.DisponivelMaisTarde && s2.estado == EstadoSala.DisponivelMaisTarde){
                    return s1.numMinutosAteEstarLivre > s2.numMinutosAteEstarLivre ? 1 : -1;}
                return 0;
            }
        });

        for(Sala sala : salas) {
            String stringDisponibilidade;
            if(sala.lidaComSucesso)
                stringDisponibilidade = sala.obterDisponibilidadeString(hora, minuto);
            else
                stringDisponibilidade = "Erro a obter dados";

            stringsDisponibilidades.add(stringDisponibilidade);
            stringsIds.add(sala.id);
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

        DefinirImagem (holder, mContext, stringsIds.get(position));

        holder.imageName.setText(sala.nome);//Definir o Nome que aparece ao lado da imagem

        holder.hora.setText(stringsDisponibilidades.get(position));//Definir o que vai aparecer por baixo do nome ^^

        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Definir o que acontece quando se carrega no Holder
                Log.d(TAG, "onClick: click on: " + sala.nome);
                MudarImagem(v, mContext, stringsIds.get(position));
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
        CircleImageView heart;
        Button botao;
        String SHARED_PREFS;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            hora = itemView.findViewById(R.id.hora);
            heart = itemView.findViewById(R.id.heart);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            botao = itemView.findViewById(R.id.botao_link);
            SHARED_PREFS = "sharedPrefs";
        }
    }

    public interface ClickSalaCallback {
        void onClick(Sala sala);
    }

    private void DefinirImagem (@NonNull ViewHolder holder, Context ctx, String id){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        String value = sharedPreferences.getString(id, "nfav");
        if (value.equals("fav"))
        {
            Glide.with(mContext)
                    .load(R.drawable.ic_favorite_black_24dp)
                    .into(holder.heart);
        }
        else
        {
            Glide.with(mContext)
                    .load(R.drawable.ic_heart)
                    .into(holder.heart);
        }
    }

    private void MudarImagem(@NonNull View itemView, Context ctx, String id){
        CircleImageView imageView = itemView.findViewById(R.id.heart);
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        String value = sharedPreferences.getString(id, "nfav");
        if (value.equals("fav"))
        {
            imageView.setImageResource(R.drawable.ic_heart);
            SetDataUnFav(ctx, itemView, id);
        }
        else
        {
            imageView.setImageResource(R.drawable.ic_favorite_black_24dp);
            SetDataFav(ctx, itemView, id);
        }
    }

    private void SetDataFav(Context ctx, @NonNull View itemView, String id){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, "fav");
        editor.apply();
        String value = sharedPreferences.getString(id, "nfav");
    }

    private void SetDataUnFav(Context ctx, @NonNull View itemView, String id){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, "nfav");
        editor.apply();
        String value = sharedPreferences.getString(id, "nfav");
    }
}
