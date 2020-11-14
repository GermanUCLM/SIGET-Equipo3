package es.uclm.esi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esi.model.Reunion;
import es.uclm.esi.repository.RepositoryCalendarioPersonal;

/**
 * 
 * @author Víctor Clase para recepción de mensajes http relacionados con obtener
 *         el calendario personal
 */
@RestController
public class ControllerCalendarioPersonal {

	@Autowired
	RepositoryCalendarioPersonal calendarioRepository;

	
//	@GetMapping("/pruebaConsulta")
//	public String getPrueba() {
//		
//		List<Reunion> reuniones = calendarioRepository.findReunionesMes(12, 2020);
//
//		return reuniones.toString();
//	}
	@PostMapping("/getCalendarioPersonalMes")
	public String getCalendarioPersonalMes(@RequestBody Map<String, Object> entrada) {
		JSONObject jso = new JSONObject(entrada);
		int mespeticion = jso.getInt("mes");
		int anopeticion = jso.getInt("ano");
		String usuario = jso.getString("usuario");

		List<Reunion> reuniones = calendarioRepository.findReunionesMes(mespeticion, anopeticion);
		ArrayList<Integer> dias = new ArrayList<Integer>();
		int dia;
		for (Reunion reunion : reuniones) {
			dia = reunion.getDia();
			if(!dias.contains(dia)) {
				dias.add(dia);
			}
		}
		JSONObject jsoret = new JSONObject();

		int[] diasjson = new int[dias.size()];
		for (int i = 0; i < dias.size(); i++) {
			diasjson[i] = dias.get(i);
		}
		
		jsoret.put("reuniones", diasjson);
		jsoret.put("mes", mespeticion);
		jsoret.put("ano", anopeticion);
		
		return jsoret.toString();
	}

	@PostMapping("/getDetallesReunion")
	public String getDetallesReunion(@RequestBody Map<String, Object> entrada) {
		JSONObject jso = new JSONObject(entrada);		
		JSONObject jsoret = new JSONObject();
		JSONArray jsa = new JSONArray();
		JSONArray jsaAsistentes = new JSONArray();
		JSONObject jsoreunion = new JSONObject();
		
		try {
			int contadorReuniones = 1;
			int wid = 1;
			while (wid>0) {
				if (calendarioRepository.findById(wid).getDia() == jso.getInt("dia")
						&& calendarioRepository.findById(wid).getMes() == jso.getInt("mes")) {
					jsoreunion.put("titulo", calendarioRepository.findById(wid).getTitulo());
					jsoreunion.put("id", contadorReuniones);
					jsoreunion.put("hora", calendarioRepository.findById(wid).getHora());
					for (int i = 0; i < calendarioRepository.findById(wid).getAsistentes().length; i++) {
						jsaAsistentes.put(calendarioRepository.findById(wid).getAsistentes()[i]);
					}
					jsoreunion.put("asistentes", jsaAsistentes);
					jsoreunion.put("descripcion", calendarioRepository.findById(wid).getDescripcion());
					jsa.put(jsoreunion);
					contadorReuniones++;
					
				} 
				wid++;
			}
		} catch (Exception e) {}
		
		jsoret.put("dia", jso.getInt("dia"));
		jsoret.put("mes", jso.getInt("mes"));
		jsoret.put("ano", jso.getInt("ano"));
		jsoret.put("reuniones", jsa);
		
		return jsoret.toString();
	}

}
