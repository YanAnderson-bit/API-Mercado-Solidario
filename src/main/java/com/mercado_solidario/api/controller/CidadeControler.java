package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

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
import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.Estado;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.CidadeRepository;
import com.mercado_solidario.api.service.CidadeServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/cidades")
public class CidadeControler {

	@Autowired
	private CidadeRepository cidadeRepository;

	@Autowired
	private CidadeServices cidadeServices;

	// Comando GET
	@GetMapping
	public CollectionModel<EntityModel<Cidade>> listar(@RequestParam(required = false) String nome,
			@RequestParam(required = false) String estado) {
		Specification<Cidade> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (estado != null) {
				Join<Cidade, Estado> estadoJoin = root.join("estado", JoinType.LEFT);
				predicates.add(criteriaBuilder.like(estadoJoin.get("nome"), "%" + estado + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<EntityModel<Cidade>> cidades = cidadeRepository.findAll(spec).stream()
				.map(cidade -> EntityModel.of(cidade,
						linkTo(methodOn(CidadeControler.class).buscar(cidade.getId())).withSelfRel(),
						linkTo(methodOn(CidadeControler.class).listar(nome, estado)).withRel("cidades")))
				.collect(Collectors.toList());

		return CollectionModel.of(cidades, linkTo(methodOn(CidadeControler.class).listar(nome, estado)).withSelfRel());
	}

	@GetMapping("/{cidadeId}") // -> /cidades/xidadeId
	public ResponseEntity<EntityModel<Cidade>> buscar(@PathVariable("cidadeId") Long Id) {
		return cidadeRepository.findById(Id)
				.map(cidade -> EntityModel.of(cidade,
						linkTo(methodOn(CidadeControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(CidadeControler.class).remover(Id)).withRel("delete"),
						linkTo(methodOn(CidadeControler.class).listar(null, null)).withRel("cidades")))
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
	 * "estado":{
	 * "id" = id
	 * }
	 * }
	 */
	public ResponseEntity<EntityModel<Cidade>> adicionar(@RequestBody Cidade cidade) {
		Cidade savedCidade = cidadeServices.salvar(cidade);
		return ResponseEntity.created(linkTo(methodOn(CidadeControler.class).buscar(savedCidade.getId())).toUri())
				.body(EntityModel.of(savedCidade,
						linkTo(methodOn(CidadeControler.class).buscar(savedCidade.getId())).withSelfRel(),
						linkTo(methodOn(CidadeControler.class).listar(null, null)).withRel("cidades")));
	}

	// Comandos PUT
	@PutMapping("/{estadoId}")
	public ResponseEntity<?> atualizar(@PathVariable("estadoId") Long Id, @RequestBody Cidade cidade) {
		try {
			return cidadeRepository.findById(Id)
					.map(cidadeAtual -> {
						BeanUtils.copyProperties(cidade, cidadeAtual, "id");
						cidadeServices.salvar(cidadeAtual);
						return ResponseEntity.ok(EntityModel.of(cidadeAtual,
								linkTo(methodOn(CidadeControler.class).buscar(Id)).withSelfRel(),
								linkTo(methodOn(CidadeControler.class).listar(null, null)).withRel("cidades")));
					})
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// Comando PATCH
	@PatchMapping("/{cidadeId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("cidadeId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return cidadeRepository.findById(Id)
				.map(cidade -> {
					merge(campos, cidade);
					return atualizar(Id, cidade);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	private void merge(Map<String, Object> camposOrigem, Cidade cidadeDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Cidade cidadeOrigem = objectMapper.convertValue(camposOrigem, Cidade.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Cidade.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, cidadeOrigem);

			ReflectionUtils.setField(field, cidadeDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{cidadeId}")
	public ResponseEntity<?> remover(@PathVariable("cidadeId") Long Id) {
		try {
			cidadeServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location", linkTo(methodOn(CidadeControler.class).listar(null, null)).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
