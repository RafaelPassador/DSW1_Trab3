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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
		String pass = (String) json.get("password");
		if (pass != null) {
			user.setPassword(encoder.encode(pass));
		}
		user.setCPF((String) json.get("cpf"));
		user.setName((String) json.get("name"));
		user.setTelefone((String) json.get("telefone"));
		user.setSexo((String) json.get("sexo"));
		Date birth = new SimpleDateFormat("dd/MM/yyyy").parse((String) json.get("nascimento"));
		user.setNascimento(birth);
		user.setRole("ROLE_USER");
		user.setEnabled(true);

	}

	private boolean isCPFValid(String cpf) {
		for (Usuario u : service.buscarTodos()) {
			if (u.getRole().toLowerCase().equals("role_user") && u.getCPF().equals(cpf))
				return false;
		}
		return true;
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
		try {
			if (isJSONValid(json.toString())) {
				System.out.println("Json valido");
				Usuario user = new Usuario();
				parse(user, json);
				if (!isCPFValid(user.getCPF()))
					return ResponseEntity.badRequest().body(null);
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

	@GetMapping(path = "/clientes/{id}")
	public ResponseEntity<Usuario> lista(@PathVariable("id") long id) {
		Usuario user = service.buscarPorId(id);
		if (user == null || !user.getRole().toLowerCase().equals("role_user")) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(user);
	}

	@PutMapping(path = "/clientes/{id}")
	public ResponseEntity<Usuario> atualiza(@PathVariable("id") long id, @RequestBody JSONObject json) {
		System.out.println("PUTZIN DOS CRIA");
		try {
			if (isJSONValid(json.toString())) {
				Usuario user = service.buscarPorId(id);
				System.out.println("FOUND THE " + id);
				if (user == null) {
					return ResponseEntity.notFound().build();
				} else {
					parse(user, json);
					if (!isCPFValid(user.getCPF()))
						return ResponseEntity.badRequest().body(null);
					service.salvar(user);
					return ResponseEntity.ok(user);
				}
			} else {
				return ResponseEntity.badRequest().body(null);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(null);
		}
	}

	@DeleteMapping(path = "/clientes/{id}")
	public ResponseEntity<Boolean> remove(@PathVariable("id") long id) {

		Usuario user = service.buscarPorId(id);
		if (user == null) {
			return ResponseEntity.notFound().build();
		} else {
			service.excluir(id);
			return ResponseEntity.noContent().build();
		}
	}

}
