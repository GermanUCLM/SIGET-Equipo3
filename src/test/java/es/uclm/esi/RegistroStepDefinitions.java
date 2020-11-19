package es.uclm.esi;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import es.uclm.esi.payload.request.CalendarioDiaRequest;
import es.uclm.esi.payload.request.CalendarioMesRequest;
import es.uclm.esi.payload.request.SignupRequest;
import es.uclm.esi.repository.UserRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RegistroStepDefinitions extends SpringIntegrationTest {
	
	@Autowired
	UserRepository userR;
	
	ResponseEntity<String> response;
	String url = DEFAULT_URL + "api/auth/signup/";
	SignupRequest request;
	boolean borrar = false;
	String nuevo;

	@When("registro un usuario con usuario {string}, pass {string} y email {string}")
	public void registro_un_usuario_con_usuario_pass_y_email(String string, String string2, String string3) {
	    request = new SignupRequest();
	    nuevo = string;
	    request.setUsername(string);
	    request.setPassword(string2);
	    request.setEmail(string3);
	}

	@When("{string} es nuevo para borrarlo")
	public void es_nuevo_para_borrarlo(String string) {
	    if(string.toLowerCase().equals("si"))
	    	borrar = true;
	}
	@Then("el mensaje sera {string}")
	public void el_mensaje_sera(String string) {
		HttpEntity hrequest = new HttpEntity(request);
		
		try {
			response = restTemplate.postForEntity(url, hrequest, String.class);
			
			assertEquals(string, response.getBody().substring(12, response.getBody().length()-2));
			userR.deleteByUsername(nuevo);
		} catch (HttpClientErrorException e) {
			
			assertEquals(string, e.getMessage().substring(19, e.getMessage().length()-3));
				
			
		}
		//borro para que no falle despues
		if(borrar) {
			
			userR.deleteByUsername(nuevo);
		}
			
	}

}