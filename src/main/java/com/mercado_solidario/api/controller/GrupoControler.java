package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
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
import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Permissao;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.GrupoRepository;
import com.mercado_solidario.api.service.GrupoServices;

@RestController
@RequestMapping(value = "/grupos")
public class GrupoControler {

	@Autowired
	private GrupoRepository grupoRepository;

	@Autowired
	private GrupoServices grupoServices;

	@GetMapping
	public List<Grupo> listar(@RequestParam(required = false) String nome,
			@RequestParam(required = false) String permissao) {
		Specification<Grupo> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (permissao != null) {
				Join<Grupo, Permissao> permissaoJoin = root.join("permissao", JoinType.LEFT);
				predicates.add(criteriaBuilder.like(permissaoJoin.get("nome"), "%" + permissao + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return grupoRepository.findAll(spec);
	}

	@GetMapping("/{grupoId}") // -> /grupos/grupoId
	public ResponseEntity<Grupo> buscar(@PathVariable("grupoId") Long Id) {
		Optional<Grupo> grupo = grupoRepository.findById(Id);

		if (grupo.isPresent()) {
			return ResponseEntity.ok(grupo.get());
		}

		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Grupo adicionar(@RequestBody Grupo grupo) {
		/*
		 * Modelo:
		 * {
		 * "nome":"nome",
		 * "permissao":[{
		 * "id" = id1
		 * },{
		 * "id" = id2
		 * },
		 * ...{
		 * "id" = idn
		 * },]
		 * }
		 */
		return grupoServices.salvar(grupo);
	}

	// Comandos PUT
	@PutMapping("/{grupoId}")
	public ResponseEntity<?> atualizar(@PathVariable("grupoId") Long Id, @RequestBody Grupo grupo) {
		try {
			Optional<Grupo> grupoAtual = grupoRepository.findById(Id);

			if (grupoAtual.isPresent()) {
				BeanUtils.copyProperties(grupo, grupoAtual.get(), "id");
				Grupo grupoSalvo = grupoServices.salvar(grupoAtual.get());

				return ResponseEntity.ok(grupoSalvo);
			}

			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	@PatchMapping("/{grupoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("grupoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		Optional<Grupo> grupo = grupoRepository.findById(Id);

		if (grupo.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, grupo.get());

		return atualizar(Id, grupo.get());
	}

	private void merge(Map<String, Object> camposOrigem, Grupo grupoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Grupo grupoOrigem = objectMapper.convertValue(camposOrigem, Grupo.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Grupo.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, grupoOrigem);

			ReflectionUtils.setField(field, grupoDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{grupoId}")
	public ResponseEntity<Grupo> remover(@PathVariable("grupoId") Long Id) {
		try {

			grupoServices.excluir(Id);
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}

}
