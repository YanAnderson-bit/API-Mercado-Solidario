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
import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.entity.Estado;
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.FornecedorRepository;
import com.mercado_solidario.api.service.FornecedorServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/fornecedores")
public class FornecedorControler {

	@Autowired
	private FornecedorRepository fornecedorRepository;

	@Autowired
	private FornecedorServices fornecedorServices;

	@GetMapping
	public CollectionModel<EntityModel<Fornecedor>> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String cidade,
			@RequestParam(required = false) String estado) {
		Specification<Fornecedor> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (cidade != null) {
				Join<Fornecedor, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				predicates.add(criteriaBuilder.like(cidadeJoin.get("nome"), "%" + cidade + "%"));
			}
			if (estado != null) {
				Join<Fornecedor, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				Join<Cidade, Estado> estadoJoin = cidadeJoin.join("estado");
				predicates.add(criteriaBuilder.like(estadoJoin.get("nome"), "%" + estado + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<EntityModel<Fornecedor>> fornecedores = fornecedorRepository.findAll(spec).stream()
				.map(fornecedor -> EntityModel.of(fornecedor,
						linkTo(methodOn(FornecedorControler.class).buscar(fornecedor.getId())).withSelfRel(),
						linkTo(methodOn(FornecedorControler.class).listar(nome, cidade, estado))
								.withRel("fornecedores")))
				.collect(Collectors.toList());

		return CollectionModel.of(fornecedores,
				linkTo(methodOn(FornecedorControler.class).listar(nome, cidade, estado)).withSelfRel());
	}

	@GetMapping("/{fornecedorId}") // -> /fornecedores/fornecedoreId
	public ResponseEntity<EntityModel<Fornecedor>> buscar(@PathVariable("fornecedorId") Long Id) {
		return fornecedorRepository.findById(Id)
				.map(fornecedor -> EntityModel.of(fornecedor,
						linkTo(methodOn(FornecedorControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(FornecedorControler.class).listar(null, null, null)).withRel("fornecedores")))
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
	 * "endereço": {
	 * "id" = id
	 * }
	 * }
	 */
	public ResponseEntity<EntityModel<Fornecedor>> adicionar(@RequestBody Fornecedor fornecedor) {
		Fornecedor savedFornecedor = fornecedorServices.salvar(fornecedor);
		return ResponseEntity
				.created(linkTo(methodOn(FornecedorControler.class).buscar(savedFornecedor.getId())).toUri())
				.body(EntityModel.of(savedFornecedor,
						linkTo(methodOn(FornecedorControler.class).buscar(savedFornecedor.getId())).withSelfRel(),
						linkTo(methodOn(FornecedorControler.class).listar(null, null, null)).withRel("fornecedores")));
	}

	// Comandos PUT
	@PutMapping("/{fornecedorId}")
	public ResponseEntity<?> atualizar(@PathVariable("fornecedorId") Long Id, @RequestBody Fornecedor fornecedor) {
		try {
			return fornecedorRepository.findById(Id)
					.map(fornecedorAtual -> {
						BeanUtils.copyProperties(fornecedor, fornecedorAtual, "id");
						Fornecedor updatedFornecedor = fornecedorServices.salvar(fornecedorAtual);
						return ResponseEntity.ok(EntityModel.of(updatedFornecedor,
								linkTo(methodOn(FornecedorControler.class).buscar(updatedFornecedor.getId()))
										.withSelfRel(),
								linkTo(methodOn(FornecedorControler.class).listar(null, null, null))
										.withRel("fornecedores")));
					})
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PatchMapping("/{fornecedorId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("fornecedorId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return fornecedorRepository.findById(Id)
				.map(fornecedor -> {
					merge(campos, fornecedor);
					fornecedorServices.salvar(fornecedor);
					return ResponseEntity.ok(EntityModel.of(fornecedor,
							linkTo(methodOn(FornecedorControler.class).buscar(fornecedor.getId())).withSelfRel(),
							linkTo(methodOn(FornecedorControler.class).listar(null, null, null))
									.withRel("fornecedores")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	private void merge(Map<String, Object> camposOrigem, Fornecedor fornecedorDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Fornecedor fornecedorOrigem = objectMapper.convertValue(camposOrigem, Fornecedor.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Fornecedor.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, fornecedorOrigem);

			ReflectionUtils.setField(field, fornecedorDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{fornecedorId}")
	public ResponseEntity<?> remover(@PathVariable("fornecedorId") Long Id) {
		try {
			fornecedorServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location",
							linkTo(methodOn(FornecedorControler.class).listar(null, null, null)).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(EntityModel.of(null,
							linkTo(methodOn(FornecedorControler.class).buscar(Id)).withRel(IanaLinkRelations.SELF),
							linkTo(methodOn(FornecedorControler.class).listar(null, null, null))
									.withRel("fornecedores")));
		}
	}

}
