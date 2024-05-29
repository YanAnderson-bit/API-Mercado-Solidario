package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.entity.Estado;
import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.GrupoRepository;
import com.mercado_solidario.api.repository.UsuarioRepository;
import com.mercado_solidario.api.service.UsuarioServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioControler {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private GrupoRepository grupoRepository;

	@Autowired
	private UsuarioServices usuarioServices;

	// @Autowired
	// private PasswordEncoder passwordEncoder;

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listar(@RequestParam(required = false) String nome,
			@RequestParam(required = false) String email, @RequestParam(required = false) String cidade,
			@RequestParam(required = false) String estado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
		Specification<Usuario> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (email != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("email"), "%" + email + "%"));
			}
			if (cidade != null) {
				Join<Usuario, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				predicates.add(criteriaBuilder.like(cidadeJoin.get("nome"), "%" + cidade + "%"));
			}
			if (estado != null) {
				Join<Usuario, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				Join<Cidade, Estado> estadoJoin = cidadeJoin.join("estado");
				predicates.add(criteriaBuilder.like(estadoJoin.get("nome"), "%" + estado + "%"));
			}
			if (dataInicio != null && dataFim != null) {
				Date start = Date.from(dataInicio.atZone(ZoneId.systemDefault()).toInstant());
				Date end = Date.from(dataFim.atZone(ZoneId.systemDefault()).toInstant());
				predicates.add(criteriaBuilder.between(root.<Date>get("dataCadastro"), start, end));
			} else if (dataInicio != null) {
				Date start = Date.from(dataInicio.atZone(ZoneId.systemDefault()).toInstant());
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("dataCadastro"), start));
			} else if (dataFim != null) {
				Date end = Date.from(dataFim.atZone(ZoneId.systemDefault()).toInstant());
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("dataCadastro"), end));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<Usuario> usuarios = usuarioRepository.findAll(spec);
		List<EntityModel<Usuario>> usuariosModel = usuarios.stream()
				.map(usuario -> EntityModel.of(usuario,
						linkTo(methodOn(UsuarioControler.class).buscar(usuario.getId())).withSelfRel(),
						linkTo(methodOn(UsuarioControler.class).listar(nome, email, cidade, estado, dataInicio,
								dataFim)).withRel("usuarios")))
				.collect(Collectors.toList());
		return ResponseEntity.ok(CollectionModel.of(usuariosModel,
				linkTo(methodOn(UsuarioControler.class).listar(nome, email, cidade, estado, dataInicio, dataFim))
						.withSelfRel()));
	}

	@GetMapping("/{usuarioId}") // -> /usuarios/usuarioId
	public ResponseEntity<EntityModel<Usuario>> buscar(@PathVariable Long usuarioId) {
		Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
		if (usuario.isPresent()) {
			EntityModel<Usuario> model = EntityModel.of(usuario.get(),
					linkTo(methodOn(UsuarioControler.class).buscar(usuarioId)).withSelfRel(),
					linkTo(methodOn(UsuarioControler.class).listar(null, null, null, null, null, null))
							.withRel("usuarios"));
			return ResponseEntity.ok(model);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<EntityModel<Usuario>> adicionar(@RequestBody Usuario usuario) {
		usuario.setDataCadastro(Date.from(Instant.now()));
		Set<Grupo> visitorGrupos = new HashSet<Grupo>();
		Grupo grupo = grupoRepository.findById((long) 1).get();
		visitorGrupos.add(grupo);
		usuario.setGrupo(visitorGrupos);

		Endereço endereço = usuario.getEndereço();
		usuario.setEndereço(endereço);
		usuario.setNew(true);

		Usuario savedUsuario = usuarioServices.salvar(usuario);

		return ResponseEntity.created(linkTo(methodOn(UsuarioControler.class).buscar(savedUsuario.getId())).toUri())
				.body(EntityModel.of(savedUsuario,
						linkTo(methodOn(UsuarioControler.class).buscar(savedUsuario.getId())).withSelfRel(),
						linkTo(methodOn(UsuarioControler.class).listar(null, null, null, null, null, null))
								.withRel("usuarios")));
	}

	@PatchMapping("/{usuarioId}")
	public ResponseEntity<EntityModel<Usuario>> atualizaParcial(@PathVariable Long usuarioId,
			@RequestBody Map<String, Object> campos) {
		Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

		if (usuario.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, usuario.get());
		Usuario updatedUser = usuarioServices.salvar(usuario.get());

		return ResponseEntity.ok(EntityModel.of(updatedUser,
				linkTo(methodOn(UsuarioControler.class).buscar(updatedUser.getId())).withSelfRel(),
				linkTo(methodOn(UsuarioControler.class).listar(null, null, null, null, null, null))
						.withRel("allUsuarios")));
	}

	public ResponseEntity<?> vincularDesvincularGrupo(@PathVariable("usuarioId") Long usuarioId,
			@PathVariable("grupoId") Long grupoId, @PathVariable("vincular") boolean vincular) {

		if (vincular) {
			usuarioServices.associarGrupo(usuarioId, grupoId);
		} else {
			usuarioServices.desassociarGrupo(usuarioId, grupoId);
		}

		EntityModel<Void> entityModel = EntityModel.of(null);
		entityModel.add(linkTo(methodOn(this.getClass()).vincularDesvincularGrupo(usuarioId, grupoId, true))
				.withRel("associarGrupo"));
		entityModel.add(linkTo(methodOn(this.getClass()).vincularDesvincularGrupo(usuarioId, grupoId, false))
				.withRel("desassociarGrupo"));
		entityModel.add(linkTo(methodOn(UsuarioControler.class).buscar(usuarioId)).withRel("usuario"));
		entityModel.add(linkTo(methodOn(GrupoControler.class).buscar(grupoId)).withRel("grupo"));

		return ResponseEntity.ok(entityModel);
	}

	private void merge(Map<String, Object> camposOrigem, Usuario usuarioDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Usuario usuarioOrigem = objectMapper.convertValue(camposOrigem, Usuario.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Usuario.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, usuarioOrigem);

			ReflectionUtils.setField(field, usuarioDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{usuarioId}")
	public ResponseEntity<?> remover(@PathVariable Long usuarioId) {
		try {
			usuarioServices.excluir(usuarioId);
			return ResponseEntity.ok(EntityModel.of(null,
					linkTo(methodOn(UsuarioControler.class).listar(null, null, null, null, null, null))
							.withRel("allUsuarios")));
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
