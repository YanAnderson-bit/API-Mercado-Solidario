package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.entity.PedidoProduto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.PedidoProdutoRepository;
import com.mercado_solidario.api.repository.PedidoRepository;
import com.mercado_solidario.api.service.PedidoServices;

@RestController
@RequestMapping(value = "/pedidos")
public class PedidoControler {

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private PedidoProdutoRepository pedidoProdutoRepository;

	@Autowired
	private PedidoServices pedidoServices;

	// Comando GET
	@GetMapping
	public List<Pedido> listar(@RequestParam(required = false) String codigo,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) BigDecimal taxaFreteInicial,
			@RequestParam(required = false) BigDecimal taxaFreteFinal,
			@RequestParam(required = false) BigDecimal valorInicial,
			@RequestParam(required = false) BigDecimal valorFinal) {
		Specification<Pedido> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (codigo != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("codigo"), codigo));
			}
			if (status != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("status"), status));
			}
			if (taxaFreteFinal != null && taxaFreteInicial == null) {
				predicates.add(
						criteriaBuilder.lessThan(root.get("taxaFrete"), taxaFreteFinal));
			} else if (taxaFreteInicial != null && taxaFreteFinal == null) {
				predicates.add(
						criteriaBuilder.greaterThan(root.get("taxaFrete"), taxaFreteInicial));
			} else if (taxaFreteInicial != null && taxaFreteFinal != null) {
				predicates.add(
						criteriaBuilder.between(root.get("taxaFrete"), taxaFreteInicial, taxaFreteFinal));
			}
			if (valorFinal != null && valorInicial == null) {
				predicates.add(
						criteriaBuilder.lessThan(root.get("valor"), valorFinal));
			} else if (valorInicial != null && valorFinal == null) {
				predicates.add(
						criteriaBuilder.greaterThan(root.get("valor"), valorInicial));
			} else if (valorInicial != null && valorFinal != null) {
				predicates.add(
						criteriaBuilder.between(root.get("valor"), valorInicial, valorFinal));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		return pedidoRepository.findAll(spec);
	}

	@GetMapping("/{pedidoId}") // -> /pedidos/pedidoId
	public ResponseEntity<Pedido> buscar(@PathVariable("pedidoId") Long Id) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isPresent()) {
			return ResponseEntity.ok(pedido.get());
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping("/{pedidoId}/produtos") // -> /pedidos/pedidoId /produtos
	public ResponseEntity<List<PedidoProduto>> pordutosPorPedido(@PathVariable("pedidoId") Long Id) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isPresent()) {
			return ResponseEntity.ok(pedido.get().getPedidoProdutos());
		}

		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Pedido adicionar(@RequestBody Pedido pedido) {
		/*
		 * Modelo:
		 * {
		 * "codigo":"codigo",
		 * "taxaFrete":taxaFrete,
		 * "dataEntrega":"AAAA-MM-DD",
		 * "usuario":{
		 * "id":id
		 * },
		 * "endereço":{
		 * "id":id
		 * }
		 * "pedidoProdutos":[{
		 * "quantidade": quantidade,
		 * "observacao": observacao,
		 * "produto":{
		 * "id":id
		 * }
		 * }]
		 * }
		 */
		return pedidoServices.salvar(pedido);
	}

	// Comandos PUT
	@PutMapping("/{pedidoId}")
	public ResponseEntity<?> atualizar(@PathVariable("pedidoId") Long Id, @RequestBody Pedido pedido) {
		try {
			Optional<Pedido> pedidoAtual = pedidoRepository.findById(Id);

			if (pedidoAtual.isPresent()) {
				if (pedidoAtual.get().getStatus() == "CREATED")
					pedido.criacao(pedidoAtual.get());

				BeanUtils.copyProperties(pedido, pedidoAtual.get(), "id", "endereço");
				Pedido pedidoSalvo = pedidoServices.salvar(pedidoAtual.get());

				return ResponseEntity.ok(pedidoSalvo);
			}

			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	// Comando PATCH
	@PatchMapping("/{pedidoId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("pedidoId") Long Id,
			@RequestBody Map<String, Object> campos) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, pedido.get());

		return atualizar(Id, pedido.get());
	}

	private void merge(Map<String, Object> camposOrigem, Pedido pedidoDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		Pedido pedidoOrigem = objectMapper.convertValue(camposOrigem, Pedido.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(Pedido.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, pedidoOrigem);

			ReflectionUtils.setField(field, pedidoDestino, novoValor);
		});
	}

	@PatchMapping("/confirmar/{pedidoId}")
	public ResponseEntity<?> confirmar(@PathVariable("pedidoId") Long Id) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().confirmar();

		return atualizar(Id, pedido.get());
	}

	@PatchMapping("/entregue/{pedidoId}")
	public ResponseEntity<?> entregue(@PathVariable("pedidoId") Long Id) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().entregue();

		return atualizar(Id, pedido.get());
	}

	@PatchMapping("/cancelar/{pedidoId}")
	public ResponseEntity<?> cancelar(@PathVariable("pedidoId") Long Id) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().cancelar();

		return atualizar(Id, pedido.get());
	}

	@PatchMapping("/adicionar-produto/{pedidoId}")
	public ResponseEntity<?> adicionarProduto(@PathVariable("pedidoId") Long Id,
			@RequestBody PedidoProduto pedidoProduto) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().getPedidoProdutos().add(pedidoProduto);

		return atualizar(Id, pedido.get());
	}

	@PatchMapping("/remover-produto/{pedidoId}/{produtoIndex}") /// pedidos/removerProduto/{pedidoId}/{produtoIndex}
	public ResponseEntity<?> removerProduto(@PathVariable("pedidoId") Long Id,
			@PathVariable("produtoIndex") int produtoIndex) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<PedidoProduto> produtosAtuais = pedido.get().getPedidoProdutos();

		PedidoProduto produto = produtosAtuais.get(produtoIndex);
		produtosAtuais.remove(produtoIndex);
		pedidoProdutoRepository.delete(produto);

		pedido.get().setPedidoProdutos(produtosAtuais);

		Pedido pedidoSalvo = pedidoServices.salvar(pedido.get());

		return ResponseEntity.ok(pedidoSalvo);
	}

	@PatchMapping("/remover-produto/{pedidoId}") /// pedidos/removerProduto/{pedidoId}?produtoId=produtoId
	public ResponseEntity<?> removerProduto(@PathVariable("pedidoId") Long Id, Long produtoId) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<PedidoProduto> produtosAtuais = pedido.get().getPedidoProdutos();
		List<PedidoProduto> produtoRemover = new ArrayList<>();
		for (PedidoProduto produto : pedido.get().getPedidoProdutos()) {
			if (produto.getProduto().getId().equals(produtoId)) {
				produtoRemover.add(produto);
			}
		}
		for (PedidoProduto produto : produtoRemover) {
			produtosAtuais.remove(produto);
			pedidoProdutoRepository.delete(produto);
		}

		pedido.get().setPedidoProdutos(produtosAtuais);

		Pedido pedidoSalvo = pedidoServices.salvar(pedido.get());

		return ResponseEntity.ok(pedidoSalvo);
	}

	@PatchMapping("/remover-produto/{pedidoId}/{nomeProduto}") /// pedidos/removerProduto/{pedidoId}{nomeProduto}
	public ResponseEntity<?> removerProdutoNome(@PathVariable("pedidoId") Long Id,
			@PathVariable("nomeProduto") String nome) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Long removerId = Long.valueOf(-1);
		for (PedidoProduto produto : pedido.get().getPedidoProdutos()) {
			if (produto.getProduto().getNome().equalsIgnoreCase(nome))
				removerId = produto.getProduto().getId();
		}

		if (removerId >= 0)
			return removerProduto(Id, removerId);
		else
			return ResponseEntity.notFound().build();
	}

	@PatchMapping("/aumentar-quantidade/{pedidoId}/{indexProduto}") /// pedidos//aumentar-quantidade/{pedidoId}/{indexProduto}?quantia=quantia
	public ResponseEntity<?> addQuantidade(@PathVariable("pedidoId") Long Id, @PathVariable("indexProduto") int index,
			int quantia) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().incrementoDecremento(index, quantia, true);

		return atualizar(Id, pedido.get());
	}

	@PatchMapping("/diminuir-quantidade/{pedidoId}/{indexProduto}") /// pedidos//diminuir-quantidade/{pedidoId}/{indexProduto}?quantia=quantia
	public ResponseEntity<?> subQuantidade(@PathVariable("pedidoId") Long Id, @PathVariable("indexProduto") int index,
			int quantia) {
		Optional<Pedido> pedido = pedidoRepository.findById(Id);

		if (pedido.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		pedido.get().incrementoDecremento(index, quantia, false);

		return atualizar(Id, pedido.get());
	}

	// Comandos DELET
	@DeleteMapping("/{pedidoId}")
	public ResponseEntity<Pedido> remover(@PathVariable("pedidoId") Long Id) {
		try {

			pedidoServices.excluir(Id);
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}

}
