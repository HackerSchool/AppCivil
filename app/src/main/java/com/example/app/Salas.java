package com.example.app;
import android.util.Log;

import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;

public class Salas
{
	public static void updateSalaStatesForHour(int hora, int minuto) {
		for(Sala sala : salasList) {
			sala.updateEstadoAtual(hora, minuto);
		}
	}

	interface ObterSalasTerminouCallback {
        void concluido();
    }

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    //Ao fazer um request incrementa-se. Quando chegar a zero pode mostrar-se o estado das salas
    static int numeroDeRequestsPorTerminar = 0;

	public static HashMap<String, Sala> salas;
	public static ArrayList<Sala> salasList;

	/**
	 * Inicializa a lista de salas, preenchendo-a com a lista de salas
	 */
	public static void initSalas() {
		salas = new HashMap<>();
		salasList = new ArrayList<>();

        addSala(new Sala("2448131361736", "V1.06"));
        addSala(new Sala("2448131361735", "V1.07"));
        addSala(new Sala("2448131361734", "V1.08"));
        addSala(new Sala("2448131361733", "V1.09"));

        addSala(new Sala("2448131361682", "V1.26"));
        addSala(new Sala("2448131361681", "V1.25"));
        addSala(new Sala("2448131361680", "V1.24"));
        addSala(new Sala("2448131361679", "V1.23"));

        addSala(new Sala("2448131361683", "V1.31" ));
        addSala(new Sala("2448131361685", "V1.32"));
        addSala(new Sala("2448131361687", "V1.33"));
        addSala(new Sala("2448131361689", "V1.34"));
	}

	/**
	 * Adiciona uma sala à lista de salas
	 * @param sala
	 */
	private static void addSala(Sala sala) {
		salas.put(sala.id, sala);
		salasList.add(sala);
	}


    public static void resetRequestsObterEstado(RequestQueue volleyQueue) {
        numeroDeRequestsPorTerminar = 0;
        volleyQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
	/**
	 * Obtem o as horas livres da sala para esse dia
	 */
	public static void getSalaData(Date day, RequestQueue volleyQueue, ObterSalasTerminouCallback callback) {
	    //String da data para fazer o request sobre os eventos nessa data
	    String dayStr = dateFormat.format(day);

		//Para cada sala faz um request e processa-o
		for(Sala sala : salas.values()) {
            try {
                obterSala(sala, dayStr, volleyQueue, callback);
            } catch (Exception e) {
                Log.e("Erro", "Erro", e);
                sala.setError(e);
            }
        }
	}



	private static void obterSala(final Sala sala, final String day, RequestQueue volleyQueue, final ObterSalasTerminouCallback callback) throws Exception {
        String url = String.format("https://fenix.tecnico.ulisboa.pt/api/fenix/v1/spaces/%s?day=%s", sala.id, day);
        JsonObjectRequest jsObjRequest =
                new JsonObjectRequest(
                        Request.Method.GET, // Requisição via HTTP_GET
                        url,   // url da requisição
                        null,  // JSONObject a ser enviado via POST
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    processSala(sala, response, day);
                                } catch (Exception e) {
                                    Log.e("Erro", "Erro", e);
                                    sala.setError(e);
                                }

                                decrementarNumeroRequestsPorTerminar(callback);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Erro", "Erro", error);
                                sala.setError(error);
                                decrementarNumeroRequestsPorTerminar(callback);
                            }
                        });

        numeroDeRequestsPorTerminar++;
        volleyQueue.add(jsObjRequest);

    }

    private static void decrementarNumeroRequestsPorTerminar(ObterSalasTerminouCallback callback) {
        numeroDeRequestsPorTerminar--;
        if(numeroDeRequestsPorTerminar == 0) {
            callback.concluido();
        }
    }

	/**
	 * Processa os eventos da sala, criando a lista de tempos livres da sala
	 * @param sala
	 * @param salaResponse
	 * @param dayStr
	 * @throws Exception
	 */
	private static void processSala(Sala sala, JSONObject salaResponse, String dayStr) throws Exception {
		String name = salaResponse.getString("name");
		System.out.println("Sala: " + name);
		System.out.println("Num eventos: " + ((JSONArray)salaResponse.get("events")).length());
		System.out.println();

		JSONArray eventsArray = (JSONArray)salaResponse.get("events");

		//Lista que contem os intervalos a que a sala está livre (cada elemento é do tipo {startH, startM, endH, endM})
		ArrayList<int[]> freeTimes = new ArrayList<>(eventsArray.length()+1);

		//Começa com a sala livre o dia inteiro
		freeTimes.add(new int[]{ 0, 0, 23, 59 });

		//Processa cada evento nessa sala para contruir a lista de tempos livres
        for(int i = 0; i<eventsArray.length(); i++) {
            JSONObject event = eventsArray.getJSONObject(i);

            //Se o evento não for para o dia certo é ignorado
            if(!event.getString("day").equalsIgnoreCase(dayStr))
                continue;

            //Processa o evento
            processEvent(event, dayStr, freeTimes);
        }

        sala.setFreeTimes(freeTimes);
	}

	/**
	 * Processa um evento, alterando a ArrayList freeTimes, para corresponder ao tempo livre dessa sala
	 * @param event Evento em JSON
	 * @param dateStr Data de hoje em string
	 * @param freeTimes Array dos tempos livres da sala que vai ser alterada
	 * @throws Exception
	 */
	private static void processEvent(JSONObject event, String dateStr, ArrayList<int[]> freeTimes) throws Exception {
        JSONObject period = event.getJSONObject("period");

        //As strings start e end começam com a data, assim, retira-se essa parte para ficar só com as horas
        int trimDayLen = dateStr.length() + 1;
        String startStr = period.getString("start").substring(trimDayLen);
        String endStr = period.getString("end").substring(trimDayLen);

        //Converte a hora e minuto para int
		int[] startHour = parseHourString(startStr);
		int[] endHour = parseHourString(endStr);

		//Procura a hora a que começa o tempo livre
		int startFreeTimeIdx = -1;
		int endFreeTimeIdx = -1;

		for(int i = 0; i<freeTimes.size(); i++) {
			int[] hourRange = freeTimes.get(i);

			boolean eventoComecaDepoisDaHoraDeInicioDesteTempo = (startHour[0] > hourRange[0] ||
					(startHour[0] == hourRange[0] && startHour[1] >= hourRange[1]));
			boolean eventoAcabaAntesDoFinalDesteEvento = (endHour[0] < hourRange[2] ||
					(endHour[0] == hourRange[2] && endHour[1] <= hourRange[3]));
			if((startFreeTimeIdx == -1 && eventoComecaDepoisDaHoraDeInicioDesteTempo) ||
					(eventoComecaDepoisDaHoraDeInicioDesteTempo && eventoAcabaAntesDoFinalDesteEvento)) {
				//Se o evento começar depois ou ao mesmo tempo do inicio deste tempo livre
				startFreeTimeIdx = i;
			}

			//if(eventoComecaDepoisDaHoraDeInicioDesteTempo )

			if(endFreeTimeIdx == -1 && (endHour[0] < hourRange[2] ||
					(endHour[0] == hourRange[2] && endHour[1] <= hourRange[3]))) {
				//Se o evento acabar antes ou ao mesmo tempo do final deste tempo livre
				endFreeTimeIdx = i;
			}
		}

		//Altera os tempos livres
		if(startFreeTimeIdx != -1 && endFreeTimeIdx != -1 ) {
			if (startFreeTimeIdx == endFreeTimeIdx)
			{
				//Se o evento estiver no meio de um tempo livre, divide esse tempo ao meio
				int[] hourRange = freeTimes.get(startFreeTimeIdx);

				boolean startHourSame = startHour[0] == hourRange[0] && startHour[1] == hourRange[1];
				boolean endHourSame = endHour[0] == hourRange[2] && endHour[1] == hourRange[3];

				if(startHourSame && endHourSame) {
					//Se o evento for do tamanho do tempo livre, apaga esse tempo livre
					freeTimes.remove(startFreeTimeIdx);
				} else if(startHourSame && !endHourSame) {
					//Se o inicio do evento corresponder ao inicio do tempo livre, altera o tempo livre para comecar mais tarde
					//(O tempo livre só vai começar depois do final do evento)
					freeTimes.set(startFreeTimeIdx, new int[] {endHour[0], endHour[1], hourRange[2], hourRange[3]});
				} else if(!startHourSame && endHourSame) {
					//Se o final do evento corresponder ao final do tempo livre, altera o tempo livre para acabar mais cedo
					//(O tempo livre vai acabar no inicio do evento)
					freeTimes.set(startFreeTimeIdx, new int[] {hourRange[0], hourRange[1], startHour[0], startHour[1]});
				} else {
					//O evento está no meio do tempo livre, divide o tempo livre em dois
					freeTimes.set(startFreeTimeIdx, new int[] {hourRange[0], hourRange[1], startHour[0], startHour[1]});
					if(endHour[0] != hourRange[2] || endHour[1] != hourRange[3])
						freeTimes.add(startFreeTimeIdx + 1, new int[] {endHour[0], endHour[1], hourRange[2], hourRange[3]});
				}
			} else {
				throw new Exception("startFreeTimeIdx != endFreeTimeIdx !!!");
			}
		}
    }

    private static int[] parseHourString(String str) {
		int separatorIdx = str.indexOf(':');

		String h = str.substring(0, separatorIdx);
		String m = str.substring(separatorIdx+1);

		return new int[] { Integer.parseInt(h), Integer.parseInt(m) };
	}
}