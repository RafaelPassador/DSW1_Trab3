package br.ufscar.dc.dsw.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.ufscar.dc.dsw.domain.Proposta;
import br.ufscar.dc.dsw.service.spec.IPropostaService;

@RestController
public class PropostasRestController {

	@Autowired
	private IPropostaService service;

	@GetMapping(path = "/propostas/carros/{id}")
	public ResponseEntity<List<Proposta>> listaCarros(@PathVariable("id") long id) {
		List<Proposta> listPropostas = new ArrayList<>();
		for (Proposta proposta : service.buscarTodos()) {
			if (proposta.getCarro().getId() == id) {
				listPropostas.add(proposta);
			}
		}
		if (listPropostas.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(listPropostas);
	}

	@GetMapping(path = "/propostas/clientes/{id}")
	public ResponseEntity<List<Proposta>> listaClientes(@PathVariable("id") long id) {
		List<Proposta> listPropostas = new ArrayList<>();
		for (Proposta proposta : service.buscarTodos()) {
			if (proposta.getUsuario().getId() == id) {
				listPropostas.add(proposta);
			}
		}
		if (listPropostas.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(listPropostas);
	}
}