package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado_solidario.api.entity.Estado;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.EstadoRepository;
import com.mercado_solidario.api.service.EstadoServices;

@RestController
@RequestMapping(value = "/estados")
public class EstadoControler {

	@Autowired
	private EstadoRepository estadoRepository;

	@Autowired
	private EstadoServices estadoServices;

	// Comando GET
	@GetMapping
	public List<Estado> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String sigla) {
		Specification<Estado> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (sigla != null) {

				predicates.add(criteriaBuilder.like(root.get("sigla"), "%" + sigla + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return estadoRepository.findAll(spec);
	}

	@GetMapping("/{estadoId}")
	public ResponseEntity<Estado> buscar(@PathVariable("estadoId") Long Id) {
		Optional<Estado> estado = estadoRepository.findById(Id);

		if (estado.isPresent()) {
			return ResponseEntity.ok(estado.get());
		}

		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Estado adicionar(@RequestBody Estado estado) {
		/*
		 * Modelo:
		 * {
		 * "nome":"nome",
		 * "sigla": "sigla"
		 * }
		 */
		return estadoServices.salvar(estado);
	}

	// Comandos PUT
	@PutMapping("/{estadoId}")
	public ResponseEntity<?> atualizar(@PathVariable("estadoId") Long Id, @RequestBody Estado estado) {
		/*
		 * Modelo:
		 * {
		 * "nome":"nome",
		 * "sigla": "sigla"
		 * }
		 */
		try {
			Optional<Estado> estadoAtual = estadoRepository.findById(Id);

			if (estadoAtual.isPresent()) {
				BeanUtils.copyProperties(estado, estadoAtual.get(), "id");
				Estado estadoSalvo = estadoServices.salvar(estadoAtual.get());

				return ResponseEntity.ok(estadoSalvo);
			}

			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	// Comando PATCH
	@PatchMapping("/{estadoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("estadoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		Optional<Estado> estado = estadoRepository.findById(Id);

		if (estado.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, estado.get());
		return atualizar(Id, estado.get());
	}

	private void merge(Map<String, Object> camposOrigem, Estado estadoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Estado estadoOrigem = objectMapper.convertValue(camposOrigem, Estado.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Estado.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, estadoOrigem);

			ReflectionUtils.setField(field, estadoDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{estadoId}")
	public ResponseEntity<Estado> remover(@PathVariable("estadoId") Long Id) {// -> /estados/estadoId
		try {

			estadoServices.excluir(Id);
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}
}
