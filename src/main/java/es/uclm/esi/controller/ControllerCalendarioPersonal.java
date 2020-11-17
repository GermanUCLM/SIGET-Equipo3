package es.uclm.esi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esi.model.Asistente;
import es.uclm.esi.model.Reunion;
import es.uclm.esi.repository.RepositoryCalendarioPersonal;
import es.uclm.esi.security.jwt.JwtUtils;
import io.jsonwebtoken.Jwts;

/**
 * 
 * @author Víctor Clase para recepción de mensajes http relacionados con obtener
 *         el calendario personal
 */
@RestController
public class ControllerCalendarioPersonal { 

	@Autowired
	RepositoryCalendarioPersonal calendarioRepository;
	
	@Value("${siget.app.jwtSecret}")
	private String jwtSecret;
	
//	@GetMapping("/pruebaConsulta")
//	public String getPrueba() {
//		
//		List<Reunion> reuniones = calendarioRepository.findReunionesMes(12, 2020);
//
//		return reuniones.toString();
//	}
	@PostMapping("/getCalendarioPersonalMes")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public String getCalendarioPersonalMes(@RequestBody Map<String, Object> entrada,@RequestHeader("Authorization") String token) {
		JSONObject jso = new JSONObject(entrada);
		int mespeticion = jso.getInt("mes");
		int anopeticion = jso.getInt("ano");
		String usuario = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token.substring(7, token.length())).getBody().getSubject();
		List<Reunion> reuniones = calendarioRepository.findReunionesMes(mespeticion, anopeticion);
		ArrayList<Integer> dias = new ArrayList<Integer>();
		int dia;
		Asistente[] asistentes;
		System.out.println(usuario);
		//Hay que controlar que no exista usuario porque no se haya pasado el token o cualquier tipo de error
		for (Reunion reunion : reuniones) {
			asistentes = reunion.getAsistentes();
			for (Asistente asistente : asistentes) {
				if(asistente.getUsuario().equalsIgnoreCase(usuario)) { 
					dia = reunion.getDia();
					if(!dias.contains(dia)) {
						dias.add(dia);
					}
				}
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
		jsoret.put("usuario", usuario);
		
		System.out.println("Estoy enviando "+jsoret);
		
		return jsoret.toString();
	}

	@PostMapping("/getDetallesReunion")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public String getDetallesReunion(@RequestBody Map<String, Object> entrada,@RequestHeader("Authorization") String token) {
		JSONObject jso = new JSONObject(entrada);		
		JSONObject jsoret = new JSONObject();
		JSONArray jsa = new JSONArray();
		JSONObject jsoreunion = new JSONObject();
		String usuario = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token.substring(7, token.length())).getBody().getSubject();
		List<Reunion> reuniones = calendarioRepository.findByDia(jso.getInt("dia"),jso.getInt("mes"),jso.getInt("ano"));
		Asistente[] asistentes;
		int contadorReuniones = 1;
		for (Reunion reunion : reuniones) {
			asistentes = reunion.getAsistentes();
			for (Asistente asistente : asistentes) {
				if (asistente.getUsuario().equalsIgnoreCase(usuario)) {
					jsoreunion.put("titulo", reunion.getTitulo());
					jsoreunion.put("id", contadorReuniones);
					jsoreunion.put("hora", reunion.getHora());
					jsoreunion.put("asistentes", asistentes);
					jsoreunion.put("descripcion", reunion.getDescripcion());
					jsa.put(jsoreunion);
					contadorReuniones++;
				}
			}
		}
		
		jsoret.put("dia", jso.getInt("dia"));
		jsoret.put("mes", jso.getInt("mes"));
		jsoret.put("ano", jso.getInt("ano"));
		jsoret.put("reuniones", jsa);
		System.out.println("Me ha llegado esto "+jso);
		System.out.println("Estoy enviando "+jsoret);
		return jsoret.toString();
	}

}
