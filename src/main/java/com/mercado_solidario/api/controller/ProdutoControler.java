package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.ProdutoRepository;
import com.mercado_solidario.api.service.ProdutoServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/produtos")
public class ProdutoControler {

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private ProdutoServices produtoServices;

	// Comando GET
	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Produto>>> listar(@RequestParam(required = false) String nome,
			@RequestParam(required = false) BigDecimal menorPreco,
			@RequestParam(required = false) BigDecimal maiorPreco,
			@RequestParam(required = false) Boolean disponivel,
			@RequestParam(required = false) String natureza,
			@RequestParam(required = false) String origem,
			@RequestParam(required = false) String categoria) {
		Specification<Produto> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (maiorPreco != null && menorPreco == null) {
				predicates.add(
						criteriaBuilder.lessThan(root.get("preco"), maiorPreco));
			} else if (menorPreco != null && maiorPreco == null) {
				predicates.add(
						criteriaBuilder.greaterThan(root.get("preco"), menorPreco));
			} else if (menorPreco != null && maiorPreco != null) {
				predicates.add(
						criteriaBuilder.between(root.get("preco"), menorPreco, maiorPreco));
			} else {
				predicates.add(criteriaBuilder.conjunction());
			}
			if (disponivel != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("disponivel"), disponivel));
			}
			if (categoria != null) {
				predicates.add(
						criteriaBuilder.like(root.get("categoria"), "%" + categoria + "%"));
			}
			if (natureza != null) {
				predicates.add(
						criteriaBuilder.like(root.get("natureza"), "%" + natureza + "%"));
			}
			if (origem != null) {
				predicates.add(
						criteriaBuilder.like(root.get("origem"), "%" + origem + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<EntityModel<Produto>> produtos = produtoRepository.findAll(spec)
				.stream()
				.map(produto -> EntityModel.of(produto,
						linkTo(methodOn(ProdutoControler.class).buscar(produto.getId())).withRel("self"),
						linkTo(methodOn(ProdutoControler.class).listar(nome, menorPreco, maiorPreco, disponivel,
								natureza, origem, categoria)).withRel("all-produtos")))
				.collect(Collectors.toList());

		return ResponseEntity.ok(CollectionModel.of(produtos,
				linkTo(methodOn(ProdutoControler.class).listar(nome, menorPreco, maiorPreco, disponivel, natureza,
						origem, categoria)).withSelfRel()));
	}

	@GetMapping("/{produtoId}") // -> /produtos/prdutoId
	public ResponseEntity<EntityModel<Produto>> buscar(@PathVariable("produtoId") Long id) {
		return produtoRepository.findById(id)
				.map(produto -> EntityModel.of(produto,
						linkTo(methodOn(ProdutoControler.class).buscar(id)).withSelfRel(),
						linkTo(methodOn(ProdutoControler.class).remover(id))
								.withRel("remover"),
						linkTo(methodOn(ProdutoControler.class).listar(null, null, null, null, null, null, null))
								.withRel("all-produtos")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/*
	 * // -> /produtos/por-nome?nome=nome_buscado
	 * 
	 * @GetMapping("/fornecedor/{produtoId}")
	 * public ResponseEntity<List<Fornecedor>>
	 * FornecedoresPorProduto(@PathVariable("produtoId") Long Id) {
	 * Optional<Produto> produto = produtoRepository.findById(Id);
	 * 
	 * if(produto.isPresent()) {
	 * //return ResponseEntity.ok(produto.get().getFornecedores());
	 * <-------------Concertar
	 * }
	 * 
	 * return ResponseEntity.notFound().build();
	 * }
	 */

	// -> /produtos/por-nome?nome=nome_buscado
	@GetMapping("/fornecedor/{produtoId}")
	public ResponseEntity<EntityModel<Fornecedor>> FornecedorPorProduto(@PathVariable("produtoId") Long id) {
		return produtoRepository.findById(id)
				.map(produto -> EntityModel.of(produto.getFornecedor(),
						linkTo(methodOn(ProdutoControler.class).FornecedorPorProduto(id)).withSelfRel(),
						linkTo(methodOn(ProdutoControler.class).buscar(id)).withRel("produto")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<EntityModel<Produto>> adicionar(@RequestBody Produto produto) {
		Produto savedProduto = produtoServices.salvar(produto);
		return ResponseEntity.created(linkTo(methodOn(ProdutoControler.class).buscar(savedProduto.getId())).toUri())
				.body(EntityModel.of(savedProduto,
						linkTo(methodOn(ProdutoControler.class).buscar(savedProduto.getId())).withSelfRel(),
						linkTo(methodOn(ProdutoControler.class).listar(null, null, null, null, null, null, null))
								.withRel("all-produtos")));
	}
	/*
	 * Modelo:
	 * {
	 * "nome": nome,
	 * "descricao": descricao,
	 * "preco": preco,
	 * "ativo": ativo,
	 * "natureza": natureza,
	 * "origem": origem,
	 * "categoria": categoria
	 * }
	 */

	// Comandos PUT
	@PutMapping("/{produtoId}")
	public ResponseEntity<EntityModel<Produto>> atualizar(@PathVariable("produtoId") Long id,
			@RequestBody Produto produto) {
		return produtoRepository.findById(id)
				.map(produtoExistente -> {
					BeanUtils.copyProperties(produto, produtoExistente, "id");
					Produto savedProduto = produtoServices.salvar(produtoExistente);
					return ResponseEntity.ok(EntityModel.of(savedProduto,
							linkTo(methodOn(ProdutoControler.class).buscar(id)).withSelfRel(),
							linkTo(methodOn(ProdutoControler.class).listar(null, null, null, null, null, null, null))
									.withRel("all-produtos")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando PATCH
	@PatchMapping("/{produtoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("produtoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		Optional<Produto> produto = produtoRepository.findById(Id);

		if (produto.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, produto.get());
		return atualizar(Id, produto.get());
	}

	private void merge(Map<String, Object> camposOrigem, Produto produtoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Produto produtoOrigem = objectMapper.convertValue(camposOrigem, Produto.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Produto.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, produtoOrigem);

			ReflectionUtils.setField(field, produtoDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{produtoId}")
	public ResponseEntity<?> remover(@PathVariable Long usuarioId) {
		try {
			produtoServices.excluir(usuarioId);
			return ResponseEntity.ok(EntityModel.of(null,
					linkTo(methodOn(ProdutoControler.class).listar(null, null, null, null, null, null, null))
							.withRel("allProdutos")));
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}