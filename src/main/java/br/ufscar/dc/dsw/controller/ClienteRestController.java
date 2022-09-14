package br.ufscar.dc.dsw.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufscar.dc.dsw.domain.Usuario;
import br.ufscar.dc.dsw.service.spec.IUsuarioService;

@RestController
public class ClienteRestController {

	@Autowired
	private IUsuarioService service;

	@Autowired
	private BCryptPasswordEncoder encoder;

	private boolean isJSONValid(String jsonStr) {
		try {
			return new ObjectMapper().readTree(jsonStr) != null;
		} catch (IOException e) {
			return false;
		}
	}

	private void parse(Usuario user, JSONObject json) throws ParseException {
		Object id = json.get("id");
		if (id != null) {
			if (id instanceof Integer) {
				user.setId(((Integer) id).longValue());
			} else {
				user.setId(((Long) id));
			}
		}
		user.setUsername((String) json.get("username"));
		user.setPassword((String) json.get("password"));
		user.setCPF((String) json.get("cpf"));
		user.setName((String) json.get("name"));
		user.setTelefone((String) json.get("telefone"));
		user.setSexo((String) json.get("sexo"));
		Date birth = new SimpleDateFormat("dd/MM/yyyy").parse((String) json.get("nascimento"));
		user.setNascimento(birth);
		user.setRole("ROLE_USER");
		user.setEnabled(true);

	}

	@GetMapping(path = "/clientes")
	public ResponseEntity<List<Usuario>> lista() {
		List<Usuario> listaTodos = service.buscarTodos(), lista = new ArrayList<>();
		for (Usuario u : listaTodos)
			if (u.getRole().toLowerCase().equals("role_user"))
				lista.add(u);

		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
	}

	@PostMapping(path = "/clientes")
	@ResponseBody
	public ResponseEntity<Usuario> cria(@RequestBody JSONObject json) {
		System.out.println("CHEGOUUU AQUI");
		try {
			if (isJSONValid(json.toString())) {
				System.out.println("Json valido");
				Usuario user = new Usuario();
				parse(user, json);
				user.setPassword(encoder.encode(user.getPassword()));
				service.salvar(user);
				return ResponseEntity.ok(user);
			} else {
				System.out.println("Json invalido");
				return ResponseEntity.badRequest().body(null);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);
		}
	}

}
