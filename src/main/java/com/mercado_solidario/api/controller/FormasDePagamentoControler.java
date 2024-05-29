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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado_solidario.api.entity.FormasDePagamento;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.FormasDePagamentoRepository;
import com.mercado_solidario.api.service.FormasDePagamentoServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/formasDePagamento")
public class FormasDePagamentoControler {

	@Autowired
	private FormasDePagamentoRepository formasDePagamentoRepository;

	@Autowired
	private FormasDePagamentoServices formasDePagamentoServices;

	// Comando GET
	@GetMapping
	public CollectionModel<EntityModel<FormasDePagamento>> listar() {
		List<EntityModel<FormasDePagamento>> formasDePagamento = formasDePagamentoRepository.findAll().stream()
				.map(forma -> EntityModel.of(forma,
						linkTo(methodOn(FormasDePagamentoControler.class).buscar(forma.getId())).withSelfRel(),
						linkTo(methodOn(FormasDePagamentoControler.class).listar()).withRel("formasDePagamento")))
				.collect(Collectors.toList());

		return CollectionModel.of(formasDePagamento,
				linkTo(methodOn(FormasDePagamentoControler.class).listar()).withSelfRel());
	}

	@GetMapping("/{formasDePagamentoId}") // -> /formasDePagamento/formasDePagamentoId
	public ResponseEntity<EntityModel<FormasDePagamento>> buscar(@PathVariable("formasDePagamentoId") Long Id) {
		return formasDePagamentoRepository.findById(Id)
				.map(formasDePagamento -> EntityModel.of(formasDePagamento,
						linkTo(methodOn(FormasDePagamentoControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(FormasDePagamentoControler.class).listar()).withRel("formasDePagamento")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<EntityModel<FormasDePagamento>> adicionar(@RequestBody FormasDePagamento formasDePagamento) {
		FormasDePagamento savedFormasDePagamento = formasDePagamentoServices.salvar(formasDePagamento);
		return ResponseEntity
				.created(linkTo(methodOn(FormasDePagamentoControler.class).buscar(savedFormasDePagamento.getId()))
						.toUri())
				.body(EntityModel.of(savedFormasDePagamento,
						linkTo(methodOn(FormasDePagamentoControler.class).buscar(savedFormasDePagamento.getId()))
								.withSelfRel(),
						linkTo(methodOn(FormasDePagamentoControler.class).listar()).withRel("formasDePagamento")));
	}

	// Comandos PUT
	@PutMapping("/{formasDePagamentoId}")
	public ResponseEntity<?> atualizar(@PathVariable("formasDePagamentoId") Long Id,
			@RequestBody FormasDePagamento formasDePagamento) {
		try {
			return formasDePagamentoRepository.findById(Id)
					.map(formasDePagamentoAtual -> {
						BeanUtils.copyProperties(formasDePagamento, formasDePagamentoAtual, "id");
						FormasDePagamento updatedFormasDePagamento = formasDePagamentoServices
								.salvar(formasDePagamentoAtual);
						return ResponseEntity.ok(EntityModel.of(updatedFormasDePagamento,
								linkTo(methodOn(FormasDePagamentoControler.class)
										.buscar(updatedFormasDePagamento.getId())).withSelfRel(),
								linkTo(methodOn(FormasDePagamentoControler.class).listar())
										.withRel("formasDePagamento")));
					})
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// Comando PATCH
	@PatchMapping("/{formasDePagamentoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("formasDePagamentoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return formasDePagamentoRepository.findById(Id)
				.map(formasDePagamento -> {
					merge(campos, formasDePagamento); // Apply changes to the existing entity
					FormasDePagamento updatedFormasDePagamento = formasDePagamentoServices.salvar(formasDePagamento); // Save
																														// the
					return ResponseEntity.ok(EntityModel.of(updatedFormasDePagamento,
							linkTo(methodOn(FormasDePagamentoControler.class).buscar(updatedFormasDePagamento.getId()))
									.withSelfRel(),
							linkTo(methodOn(FormasDePagamentoControler.class).listar()).withRel("formasDePagamento")));
				})
				.orElse(ResponseEntity.notFound().build()); // Return not found if the entity does not exist
	}

	private void merge(Map<String, Object> camposOrigem, FormasDePagamento formasDePagamentoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		FormasDePagamento formasDePagamentoOrigem = objectMapper.convertValue(camposOrigem, FormasDePagamento.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(FormasDePagamento.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, formasDePagamentoOrigem);

			ReflectionUtils.setField(field, formasDePagamentoDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{formasDePagamentoId}")
	public ResponseEntity<?> remover(@PathVariable("formasDePagamentoId") Long Id) {
		try {
			formasDePagamentoServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location", linkTo(methodOn(FormasDePagamentoControler.class).listar()).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(EntityModel.of(null,
							linkTo(methodOn(FormasDePagamentoControler.class).buscar(Id))
									.withRel(IanaLinkRelations.SELF),
							linkTo(methodOn(FormasDePagamentoControler.class).listar()).withRel("formasDePagamento")));
		}
	}

}
