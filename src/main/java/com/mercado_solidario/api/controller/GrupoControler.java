package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/grupos")
public class GrupoControler {

	@Autowired
	private GrupoRepository grupoRepository;

	@Autowired
	private GrupoServices grupoServices;

	@GetMapping
	public CollectionModel<EntityModel<Grupo>> listar(@RequestParam(required = false) String nome,
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
		List<EntityModel<Grupo>> grupos = grupoRepository.findAll(spec).stream()
				.map(grupo -> EntityModel.of(grupo,
						linkTo(methodOn(GrupoControler.class).buscar(grupo.getId())).withSelfRel(),
						linkTo(methodOn(GrupoControler.class).listar(nome, permissao)).withRel("grupos")))
				.collect(Collectors.toList());

		return CollectionModel.of(grupos, linkTo(methodOn(GrupoControler.class).listar(nome, permissao)).withSelfRel());
	}

	@GetMapping("/{grupoId}") // -> /grupos/grupoId
	public ResponseEntity<EntityModel<Grupo>> buscar(@PathVariable("grupoId") Long Id) {
		return grupoRepository.findById(Id)
				.map(grupo -> EntityModel.of(grupo,
						linkTo(methodOn(GrupoControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(GrupoControler.class).listar(null, null)).withRel("grupos")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
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
	public ResponseEntity<EntityModel<Grupo>> adicionar(@RequestBody Grupo grupo) {
		Grupo savedGrupo = grupoServices.salvar(grupo);
		return ResponseEntity.created(linkTo(methodOn(GrupoControler.class).buscar(savedGrupo.getId())).toUri())
				.body(EntityModel.of(savedGrupo,
						linkTo(methodOn(GrupoControler.class).buscar(savedGrupo.getId())).withSelfRel(),
						linkTo(methodOn(GrupoControler.class).listar(null, null)).withRel("grupos")));
	}

	// Comandos PUT
	@PutMapping("/{grupoId}")
	public ResponseEntity<?> atualizar(@PathVariable("grupoId") Long Id, @RequestBody Grupo grupo) {
		try {
			return grupoRepository.findById(Id)
					.map(grupoAtual -> {
						BeanUtils.copyProperties(grupo, grupoAtual, "id");
						Grupo updatedGrupo = grupoServices.salvar(grupoAtual);
						return ResponseEntity.ok(EntityModel.of(updatedGrupo,
								linkTo(methodOn(GrupoControler.class).buscar(updatedGrupo.getId())).withSelfRel(),
								linkTo(methodOn(GrupoControler.class).listar(null, null)).withRel("grupos")));
					})
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PatchMapping("/{grupoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("grupoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return grupoRepository.findById(Id)
				.map(grupo -> {
					merge(campos, grupo);
					grupoServices.salvar(grupo);
					return ResponseEntity.ok(EntityModel.of(grupo,
							linkTo(methodOn(GrupoControler.class).buscar(grupo.getId())).withSelfRel(),
							linkTo(methodOn(GrupoControler.class).listar(null, null)).withRel("grupos")));
				})
				.orElse(ResponseEntity.notFound().build());
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
	public ResponseEntity<?> remover(@PathVariable("grupoId") Long Id) {
		try {
			grupoServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location", linkTo(methodOn(GrupoControler.class).listar(null, null)).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(EntityModel.of(null,
							linkTo(methodOn(GrupoControler.class).buscar(Id)).withRel(IanaLinkRelations.SELF),
							linkTo(methodOn(GrupoControler.class).listar(null, null)).withRel("grupos")));
		}
	}

}
