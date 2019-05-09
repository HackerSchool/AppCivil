package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdaptador extends RecyclerView.Adapter<RecyclerViewAdaptador.ViewHolder>{
    private static final String TAG = "RecyclerViewAdaptador";

    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<String> mImages= new ArrayList<>();
    private ArrayList<String> mHoras = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdaptador( Context Context, ArrayList<String> ImageNames, ArrayList<String> Images, ArrayList<String> Horas) {
        this.mImageNames = ImageNames;
        this.mImages = Images;
        this.mContext = Context;
        this.mHoras = Horas;
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

        Glide.with(mContext) //Glide é uma biblioteca
                .asBitmap() //Diz ao glide q queremos como um bitmap
                .load(mImages.get(position)) //Dar load ao url
                .into(holder.image); //Dar load no holder

        holder.imageName.setText(mImageNames.get(position));//Definir o Nome que aparece ao lado da imagem
        if (mHoras.get(position).equals("1")) {
            holder.hora.setText("Disponível por mais 1 hora.");//Definir o que vai aparecer por baixo do nome ^^
        }
        else{
            holder.hora.setText(String.format("Disponível por mais %s horas.", mHoras.get(position)));//Definir o que vai aparecer por baixo do nome ^^
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Definir o que acontece quando se carrega no Holder
                Log.d(TAG, "onClick: click on: " + mImageNames.get(position));
                Toast.makeText(mContext, "Mostrar Horário da "+mImageNames.get(position), Toast.LENGTH_SHORT).show();
                //Eventualmente Abrir outra Activity ?
            }


        });
    }


    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
}
