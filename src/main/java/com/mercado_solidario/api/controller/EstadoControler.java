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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado_solidario.api.entity.Estado;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.EstadoRepository;
import com.mercado_solidario.api.service.EstadoServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/estados")
public class EstadoControler {

	@Autowired
	private EstadoRepository estadoRepository;

	@Autowired
	private EstadoServices estadoServices;

	// Comando GET
	@GetMapping
	public CollectionModel<EntityModel<Estado>> listar(
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
		List<EntityModel<Estado>> estados = estadoRepository.findAll(spec).stream()
				.map(estado -> EntityModel.of(estado,
						linkTo(methodOn(EstadoControler.class).buscar(estado.getId())).withSelfRel(),
						linkTo(methodOn(EstadoControler.class).listar(nome, sigla)).withRel("estados")))
				.collect(Collectors.toList());

		return CollectionModel.of(estados, linkTo(methodOn(EstadoControler.class).listar(nome, sigla)).withSelfRel());
	}

	@GetMapping("/{estadoId}")
	public ResponseEntity<EntityModel<Estado>> buscar(@PathVariable("estadoId") Long Id) {
		return estadoRepository.findById(Id)
				.map(estado -> EntityModel.of(estado,
						linkTo(methodOn(EstadoControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(EstadoControler.class).listar(null, null)).withRel("estados")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<EntityModel<Estado>> adicionar(@RequestBody Estado estado) {
		Estado savedEstado = estadoServices.salvar(estado);
		return ResponseEntity.created(linkTo(methodOn(EstadoControler.class).buscar(savedEstado.getId())).toUri())
				.body(EntityModel.of(savedEstado,
						linkTo(methodOn(EstadoControler.class).buscar(savedEstado.getId())).withSelfRel(),
						linkTo(methodOn(EstadoControler.class).listar(null, null)).withRel("estados")));
	}

	// Comando PATCH
	@PatchMapping("/{estadoId}")
	public ResponseEntity<EntityModel<Estado>> atualizaParcial(@PathVariable("estadoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return estadoRepository.findById(Id)
				.map(estado -> {
					merge(campos, estado); // Merge the changes into the existing entity
					Estado updatedEstado = estadoServices.salvar(estado); // Save the updated entity

					// Return the updated entity wrapped in an EntityModel with HATEOAS links
					return ResponseEntity.ok(EntityModel.of(updatedEstado,
							linkTo(methodOn(EstadoControler.class).buscar(updatedEstado.getId())).withSelfRel(),
							linkTo(methodOn(EstadoControler.class).listar(null, null)).withRel("estados")));
				})
				.orElse(ResponseEntity.notFound().build()); // Return not found if the entity does not exist
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
	public ResponseEntity<?> remover(@PathVariable("estadoId") Long Id) {
		try {
			estadoServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location", linkTo(methodOn(EstadoControler.class).listar(null, null)).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(EntityModel.of(null,
							linkTo(methodOn(EstadoControler.class).buscar(Id)).withRel(IanaLinkRelations.SELF),
							linkTo(methodOn(EstadoControler.class).listar(null, null)).withRel("estados")));
		}
	}
}
