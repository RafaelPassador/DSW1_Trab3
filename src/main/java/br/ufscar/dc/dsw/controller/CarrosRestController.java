package br.ufscar.dc.dsw.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufscar.dc.dsw.domain.Carro;
import br.ufscar.dc.dsw.domain.Usuario;
import br.ufscar.dc.dsw.service.spec.ICarroService;
import br.ufscar.dc.dsw.service.spec.IUsuarioService;

@RestController
public class CarrosRestController {

	@Autowired
	private ICarroService carroService;

	@Autowired
	private IUsuarioService usuarioService;

	private boolean isJSONValid(String jsonStr) {
		try {
			return new ObjectMapper().readTree(jsonStr) != null;
		} catch (IOException e) {
			return false;
		}
	}

	private void parse(Carro carro, Usuario loja, JSONObject json) throws ParseException {
		Object id = json.get("id");
		if (id != null) {
			if (id instanceof Integer) {
				carro.setId(((Integer) id).longValue());
			} else {
				carro.setId(((Long) id));
			}
		}
		carro.setAno((Integer) json.get("ano"));
		carro.setChassi((String) json.get("chassi"));
		carro.setDescricao((String) json.get("descricao"));
		carro.setLoja(loja);
		carro.setModelo((String) json.get("modelo"));
		carro.setPictures((String) json.get("pictures"));
		carro.setPlaca((String) json.get("placa"));
		carro.setQuilometragem(BigDecimal.valueOf((Double) json.get("quilometragem")));
		carro.setValor(BigDecimal.valueOf((Double) json.get("valor")));
	}

	private boolean isPlacaValid(String placa, Long id) {
		for (Carro carro : carroService.searchAll()) {
			if (carro.getId() != id && carro.getPlaca().equals(placa))
				return false;
		}
		return placa.length() == 7;
	}

	@PostMapping(path = "/carros/lojas/{id}")
	@ResponseBody
	public ResponseEntity<Carro> cria(@PathVariable("id") long id, @RequestBody JSONObject json) {
		Usuario loja = usuarioService.buscarPorId(id);

		try {
			if (isJSONValid(json.toString())) {
				System.out.println("Json valido");
				Carro carro = new Carro();
				parse(carro, loja, json);

				if (!isPlacaValid(carro.getPlaca(), carro.getId()))
					return ResponseEntity.badRequest().build();
				carroService.salvar(carro);
				return ResponseEntity.ok(carro);
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

	@GetMapping(path = "/carros/lojas/{id}")
	public ResponseEntity<List<Carro>> listaPorLoja(@PathVariable("id") long id) {
		List<Carro> listaTodos = carroService.searchAll(), lista = new ArrayList<>();
		for (Carro carro : listaTodos)
			if (carro.getLoja().getId() == id)
				lista.add(carro);

		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
	}

	@GetMapping(path = "/carros/modelos/{nome}")
	public ResponseEntity<List<Carro>> listaPorModelo(@PathVariable("nome") String modelo) {
		List<Carro> listaTodos = carroService.searchAll(), lista = new ArrayList<>();
		for (Carro carro : listaTodos)
			if (carro.getModelo().toLowerCase().equals(modelo.toLowerCase()))
				lista.add(carro);

		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
	}
}