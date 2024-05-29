package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.ProdutoRepository;
import com.mercado_solidario.api.service.ProdutoServices;

@RestController
@RequestMapping(value = "/produtos")
public class ProdutoControler {

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private ProdutoServices produtoServices;

	// Comando GET
	@GetMapping
	public List<Produto> listar(@RequestParam(required = false) String nome,
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
		return produtoRepository.findAll(spec);

	}

	@GetMapping("/{produtoId}") // -> /produtos/prdutoId
	public ResponseEntity<Produto> buscar(@PathVariable("produtoId") Long Id) {
		Optional<Produto> produto = produtoRepository.findById(Id);

		if (produto.isPresent()) {
			return ResponseEntity.ok(produto.get());
		}

		return ResponseEntity.notFound().build();
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
	public ResponseEntity<Fornecedor> FornecedorPorProduto(@PathVariable("produtoId") Long Id) {
		Optional<Produto> produto = produtoRepository.findById(Id);

		if (produto.isPresent()) {
			return ResponseEntity.ok(produto.get().getFornecedor());
		}

		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Produto adicionar(@RequestBody Produto produto) {
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
		return produtoServices.salvar(produto);
	}

	// Comandos PUT
	@PutMapping("/{produtoId}")
	public ResponseEntity<?> atualizar(@PathVariable("produtoId") Long Id, @RequestBody Produto produto) {
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
		try {
			Optional<Produto> produtoAtual = produtoRepository.findById(Id);

			if (produtoAtual.isPresent()) {
				BeanUtils.copyProperties(produto, produtoAtual.get(), "id");
				Produto produtoSalvo = produtoServices.salvar(produtoAtual.get());

				return ResponseEntity.ok(produtoSalvo);
			}

			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

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
	public ResponseEntity<Produto> remover(@PathVariable("produtoId") Long Id) {
		try {

			produtoServices.excluir(Id);
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}

}