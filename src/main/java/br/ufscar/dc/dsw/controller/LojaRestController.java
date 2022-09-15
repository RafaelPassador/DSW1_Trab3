package br.ufscar.dc.dsw.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
public class LojaRestController {

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
		user.setDescricao((String) json.get("descricao"));
		user.setRole("ROLE_STORE");
		user.setEnabled(true);

	}

	private boolean isCNPJValid(String cnpj, Long id) {
		for (Usuario usuario : service.buscarTodos()) {
			if (usuario.getId() != id && usuario.getRole().toLowerCase().equals("role_store")
					&& usuario.getCPF().equals(cnpj))
				return false;
		}

		return cnpj.length() == 18;
	}

	private boolean isUsernameValid(String username, Long id) {
		for (Usuario usuario : service.buscarTodos()) {
			if (usuario.getId() != id && usuario.getUsername().equals(username))
				return false;
		}

		return true;
	}

	@GetMapping(path = "/lojas")
	public ResponseEntity<List<Usuario>> lista() {
		try {
			List<Usuario> listaTodos = service.buscarTodos(), lista = new ArrayList<>();
			for (Usuario u : listaTodos)
				if (u.getRole().toLowerCase().equals("role_store"))
					lista.add(u);

			if (lista.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(lista);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = "/lojas")
	@ResponseBody
	public ResponseEntity<Usuario> cria(@RequestBody JSONObject json) {
		try {
			if (isJSONValid(json.toString())) {
				System.out.println("Json valido");
				Usuario user = new Usuario();
				parse(user, json);
				if (!isCNPJValid(user.getCPF(), Long.parseLong("-1"))
						|| !isUsernameValid(user.getUsername(), Long.parseLong("-1")))
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

	@GetMapping(path = "/lojas/{id}")
	public ResponseEntity<Usuario> lista(@PathVariable("id") long id) {
		Usuario user = service.buscarPorId(id);
		if (user == null || !user.getRole().toLowerCase().equals("role_store")) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(user);
	}

	@PutMapping(path = "/lojas/{id}")
	public ResponseEntity<Usuario> atualiza(@PathVariable("id") long id, @RequestBody JSONObject json) {
		try {
			if (isJSONValid(json.toString())) {
				Usuario user = service.buscarPorId(id);
				if (user == null) {
					return ResponseEntity.notFound().build();
				} else {
					parse(user, json);
					if (!isCNPJValid(user.getCPF(), user.getId()) || !isUsernameValid(user.getUsername(), user.getId()))
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

	@DeleteMapping(path = "/lojas/{id}")
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
