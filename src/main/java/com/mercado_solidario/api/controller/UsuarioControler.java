package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.swing.GroupLayout.Group;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.mercado_solidario.api.entity.Grupo;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.GrupoRepository;
import com.mercado_solidario.api.repository.UsuarioRepository;
import com.mercado_solidario.api.service.UsuarioServices;

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
	public List<Usuario> listar(@RequestParam(required = false) String nome,
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
						criteriaBuilder.equal(root.get("nome"), "%" + nome + "%"));
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
			} else {
				predicates.add(criteriaBuilder.conjunction()); // if no dates are provided, return all
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return usuarioRepository.findAll(spec);
	}

	@GetMapping("/{usuarioId}") // -> /usuarios/usuarioId
	public ResponseEntity<Usuario> buscar(@PathVariable("usuarioId") Long Id) {
		Optional<Usuario> usuario = usuarioRepository.findById(Id);

		if (usuario.isPresent()) {
			return ResponseEntity.ok(usuario.get());
		}

		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario adicionar(@RequestBody Usuario usuario) {
		/*
		 * Modelo:
		 * {
		 * "nome":"nome",
		 * "email":"mail",
		 * "senha":"*****",
		 * "endereço": {
		 * ...
		 * }
		 * }
		 */
		// return usuario;
		usuario.setDataCadastro(Date.from(Instant.now()));
		// usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

		Set<Grupo> visitorGrupos = new HashSet<>();
		Grupo grupo = grupoRepository.findById((long) 1).get();
		visitorGrupos.add(grupo);
		usuario.setGrupo(visitorGrupos);

		Endereço endereço = usuario.getEndereço();
		usuario.setEndereço(endereço);

		usuario.setNew(true);

		return usuarioServices.salvar(usuario);
	}

	// Comandos PUT
	@PutMapping("/{usuarioId}")
	public ResponseEntity<?> atualizar(@PathVariable("usuarioId") Long Id, @RequestBody Usuario usuario) {
		try {
			Optional<Usuario> usuarioAtual = usuarioRepository.findById(Id);

			Date cadastro = usuarioAtual.get().getDataCadastro();
			usuario.setDataCadastro(cadastro);

			if (usuarioAtual.isPresent()) {
				BeanUtils.copyProperties(usuario, usuarioAtual.get(), "id", "endereço", "senha");
				Usuario usuarioSalvo = usuarioServices.salvar(usuarioAtual.get());

				return ResponseEntity.ok(usuarioSalvo);
			}

			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}
	//////////////////////////////////

	@PatchMapping("/{usuarioId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("usuarioId") Long Id,
			@RequestBody Map<String, Object> campos) {
		Optional<Usuario> usuario = usuarioRepository.findById(Id);

		if (usuario.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, usuario.get());

		return atualizar(Id, usuario.get());
	}

	@PatchMapping("/{usuarioId}/{grupoId}/{vincular}")
	public void vincularDesvincularGrupo(@PathVariable("usuarioId") Long usuarioId,
			@PathVariable("grupoId") Long grupoId, @PathVariable("vincular") boolean vincular) {

		if (vincular) {
			usuarioServices.associarGrupo(usuarioId, grupoId);
		} else {
			usuarioServices.desassociarGrupo(usuarioId, grupoId);
		}
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
	public ResponseEntity<Usuario> remover(@PathVariable("usuarioId") Long Id) {
		try {

			usuarioServices.excluir(Id);
			return ResponseEntity.noContent().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}
}
