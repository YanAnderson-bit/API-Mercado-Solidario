package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
import com.mercado_solidario.api.entity.Permissao;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.PermissaoRepository;
import com.mercado_solidario.api.service.PermissaoServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/permissoes")
public class PermissaoControler {

	@Autowired
	private PermissaoRepository permissaoRepository;

	@Autowired
	private PermissaoServices permissaoServices;

	// Comando GET
	@GetMapping
	public CollectionModel<EntityModel<Permissao>> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String sigla) {
		Specification<Permissao> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<EntityModel<Permissao>> permissoes = permissaoRepository.findAll(spec)
				.stream()
				.map(permissao -> EntityModel.of(permissao,
						linkTo(methodOn(PermissaoControler.class).buscar(permissao.getId())).withRel("buscar"),
						linkTo(methodOn(PermissaoControler.class).listar(nome, sigla)).withSelfRel()))
				.collect(Collectors.toList());

		return CollectionModel.of(permissoes,
				linkTo(methodOn(PermissaoControler.class).listar(nome, sigla)).withSelfRel());
	}

	// @PreAuthorize("hasAuthority('POST_PATCH_ALLOWED')")
	@GetMapping("/{permissaoId}") // -> /permissoes/permissaoId
	public ResponseEntity<EntityModel<Permissao>> buscar(@PathVariable("permissaoId") Long Id) {
		return permissaoRepository.findById(Id)
				.map(permissao -> EntityModel.of(permissao,
						linkTo(methodOn(PermissaoControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(PermissaoControler.class).listar(null, null)).withRel("permissoes")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	// @PostAuthorize()
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EntityModel<Permissao> adicionar(@RequestBody Permissao permissao) {
		Permissao savedPermissao = permissaoServices.salvar(permissao);
		return EntityModel.of(savedPermissao,
				linkTo(methodOn(PermissaoControler.class).buscar(savedPermissao.getId())).withRel("buscar"),
				linkTo(methodOn(PermissaoControler.class).listar(null, null)).withRel("permissoes"));
	}

	// Comandos PUT
	@PutMapping("/{permissaoId}")
	public ResponseEntity<EntityModel<Permissao>> atualizar(@PathVariable("permissaoId") Long id,
			@RequestBody Permissao permissao) {
		return permissaoRepository.findById(id)
				.map(permissaoExistente -> {
					BeanUtils.copyProperties(permissao, permissaoExistente, "id");
					Permissao savedPermissao = permissaoServices.salvar(permissaoExistente);
					return ResponseEntity.ok(EntityModel.of(savedPermissao,
							linkTo(methodOn(PermissaoControler.class).buscar(id)).withSelfRel(),
							linkTo(methodOn(PermissaoControler.class).listar(null, null)).withRel("all-permissoes")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando PATCH
	@PatchMapping("/{permissaoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("permissaoId") Long id,
			@RequestBody Map<String, Object> campos) {
		return permissaoRepository.findById(id)
				.map(permissao -> {
					merge(campos, permissao);
					Permissao updatedPermissao = permissaoServices.salvar(permissao);
					return ResponseEntity.ok(EntityModel.of(updatedPermissao,
							linkTo(methodOn(PermissaoControler.class).buscar(id)).withSelfRel(),
							linkTo(methodOn(PermissaoControler.class).listar(null, null)).withRel("all-permissoes")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	private void merge(Map<String, Object> camposOrigem, Permissao permissaoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Permissao permissaoOrigem = objectMapper.convertValue(camposOrigem, Permissao.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Permissao.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, permissaoOrigem);

			ReflectionUtils.setField(field, permissaoDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{permissaoId}")
	public ResponseEntity<?> remover(@PathVariable("permissaoId") Long id) {
		return permissaoRepository.findById(id)
				.map(permissao -> {
					permissaoServices.excluir(id);
					return ResponseEntity.noContent().build();
				})
				.orElse(ResponseEntity.notFound().build());
	}
}