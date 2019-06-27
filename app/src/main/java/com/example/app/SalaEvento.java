package com.example.app;

//Representa um evento no horário da sala
public final class SalaEvento {
    String data;
    int[] horaInicio, horaFinal;

    public SalaEvento(String data, int[] horaInicio, int[] horaFinal) {
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFinal = horaFinal;
    }
}
