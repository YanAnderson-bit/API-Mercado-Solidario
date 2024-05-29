package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.entity.PedidoProduto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.PedidoProdutoRepository;
import com.mercado_solidario.api.repository.PedidoRepository;
import com.mercado_solidario.api.service.PedidoServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
	public CollectionModel<EntityModel<Pedido>> listar(@RequestParam(required = false) String codigo,
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
		List<EntityModel<Pedido>> pedidos = pedidoRepository.findAll(spec).stream()
				.map(pedido -> EntityModel.of(pedido,
						linkTo(methodOn(PedidoControler.class).buscar(pedido.getId())).withSelfRel(),
						linkTo(methodOn(PedidoControler.class).listar(codigo, status, taxaFreteInicial, taxaFreteFinal,
								valorInicial, valorFinal)).withRel("pedidos")))
				.collect(Collectors.toList());
		return CollectionModel.of(pedidos, linkTo(methodOn(PedidoControler.class).listar(codigo, status,
				taxaFreteInicial, taxaFreteFinal, valorInicial, valorFinal)).withSelfRel());
	}

	@GetMapping("/{pedidoId}") // -> /pedidos/pedidoId
	public ResponseEntity<EntityModel<Pedido>> buscar(@PathVariable("pedidoId") Long Id) {
		return pedidoRepository.findById(Id)
				.map(pedido -> EntityModel.of(pedido,
						linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
								.withRel("pedidos"),
						linkTo(methodOn(PedidoControler.class).pordutosPorPedido(Id)).withRel("produtos"),
						linkTo(methodOn(PedidoControler.class).confirmar(Id)).withRel("confirmar"),
						linkTo(methodOn(PedidoControler.class).entregue(Id)).withRel("entregue"),
						linkTo(methodOn(PedidoControler.class).cancelar(Id)).withRel("cancelar")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{pedidoId}/produtos")
	public ResponseEntity<CollectionModel<EntityModel<PedidoProduto>>> pordutosPorPedido(
			@PathVariable("pedidoId") Long Id) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					List<EntityModel<PedidoProduto>> produtos = pedido.getPedidoProdutos().stream()
							.map(pedidoProduto -> EntityModel.of(pedidoProduto,
									linkTo(methodOn(PedidoControler.class).buscar(Id)).withRel("pedido"),
									linkTo(methodOn(PedidoControler.class).pordutosPorPedido(Id)).withSelfRel()))
							.collect(Collectors.toList());
					return ResponseEntity.ok(CollectionModel.of(produtos));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
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
	public ResponseEntity<EntityModel<Pedido>> adicionar(@RequestBody Pedido pedido) {
		Pedido savedPedido = pedidoServices.salvar(pedido);
		return ResponseEntity.created(linkTo(methodOn(PedidoControler.class).buscar(savedPedido.getId())).toUri())
				.body(EntityModel.of(savedPedido,
						linkTo(methodOn(PedidoControler.class).buscar(savedPedido.getId())).withSelfRel(),
						linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
								.withRel("pedidos")));
	}

	// Comandos PUT
	@PutMapping("/{pedidoId}")
	public ResponseEntity<?> atualizar(@PathVariable("pedidoId") Long Id, @RequestBody Pedido pedido) {
		try {
			Optional<Pedido> pedidoAtual = pedidoRepository.findById(Id);
			if (pedidoAtual.isPresent()) {
				BeanUtils.copyProperties(pedido, pedidoAtual.get(), "id");
				Pedido updatedPedido = pedidoServices.salvar(pedidoAtual.get());
				return ResponseEntity.ok(EntityModel.of(updatedPedido,
						linkTo(methodOn(PedidoControler.class).buscar(updatedPedido.getId())).withSelfRel(),
						linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
								.withRel("pedidos")));
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
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					merge(campos, pedido);
					Pedido updatedPedido = pedidoServices.salvar(pedido);
					return ResponseEntity.ok(EntityModel.of(updatedPedido,
							linkTo(methodOn(PedidoControler.class).buscar(updatedPedido.getId())).withSelfRel(),
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
									.withRel("pedidos")));
				})
				.orElse(ResponseEntity.notFound().build());
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
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					pedido.confirmar();
					Pedido updatedPedido = pedidoServices.salvar(pedido);
					return ResponseEntity.ok(EntityModel.of(updatedPedido,
							linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
									.withRel("pedidos"),
							linkTo(methodOn(PedidoControler.class).entregue(Id)).withRel("entregue"),
							linkTo(methodOn(PedidoControler.class).cancelar(Id)).withRel("cancelar")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/entregue/{pedidoId}")
	public ResponseEntity<?> entregue(@PathVariable("pedidoId") Long Id) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					pedido.entregue();
					Pedido updatedPedido = pedidoServices.salvar(pedido);
					return ResponseEntity.ok(EntityModel.of(updatedPedido,
							linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
									.withRel("pedidos")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/cancelar/{pedidoId}")
	public ResponseEntity<?> cancelar(@PathVariable("pedidoId") Long Id) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					pedido.cancelar();
					Pedido updatedPedido = pedidoServices.salvar(pedido);
					return ResponseEntity.ok(EntityModel.of(updatedPedido,
							linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
									.withRel("pedidos")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/adicionar-produto/{pedidoId}")
	public ResponseEntity<?> adicionarProduto(@PathVariable("pedidoId") Long Id,
			@RequestBody PedidoProduto pedidoProduto) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					pedido.getPedidoProdutos().add(pedidoProduto);
					Pedido updatedPedido = pedidoServices.salvar(pedido);
					return ResponseEntity.ok(EntityModel.of(updatedPedido,
							linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
									.withRel("pedidos"),
							linkTo(methodOn(PedidoControler.class).removerProduto(Id,
									pedidoProduto.getProduto().getId())).withRel("removerProduto")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/remover-produto/{pedidoId}/{produtoId}")
	public ResponseEntity<?> removerProduto(@PathVariable("pedidoId") Long Id, @PathVariable Long produtoId) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					PedidoProduto toRemove = pedido.getPedidoProdutos().stream()
							.filter(pp -> pp.getProduto().getId().equals(produtoId))
							.findFirst()
							.orElse(null);
					if (toRemove != null) {
						pedido.getPedidoProdutos().remove(toRemove);
						pedidoProdutoRepository.delete(toRemove);
						Pedido updatedPedido = pedidoServices.salvar(pedido);
						return ResponseEntity.ok(EntityModel.of(updatedPedido,
								linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
								linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
										.withRel("pedidos")));
					}
					return ResponseEntity.notFound().build();
				})
				.orElse(ResponseEntity.notFound().build());
	}

	public ResponseEntity<?> removerProdutoNome(@PathVariable("pedidoId") Long Id,
			@PathVariable("nomeProduto") String nome) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					Optional<PedidoProduto> produtoToRemove = pedido.getPedidoProdutos().stream()
							.filter(p -> p.getProduto().getNome().equalsIgnoreCase(nome))
							.findFirst();
					if (produtoToRemove.isPresent()) {
						pedido.getPedidoProdutos().remove(produtoToRemove.get());
						pedidoProdutoRepository.delete(produtoToRemove.get());
						Pedido updatedPedido = pedidoServices.salvar(pedido);
						return ResponseEntity.ok(EntityModel.of(updatedPedido,
								linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
								linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
										.withRel("pedidos")));
					}
					return ResponseEntity.notFound().build();
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/aumentar-quantidade/{pedidoId}/{indexProduto}") /// pedidos//aumentar-quantidade/{pedidoId}/{indexProduto}?quantia=quantia
	public ResponseEntity<?> aumentarQuantidade(@PathVariable("pedidoId") Long Id,
			@PathVariable("indexProduto") int index, @RequestParam("quantia") int quantia) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					if (index >= 0 && index < pedido.getPedidoProdutos().size()) {
						PedidoProduto produto = pedido.getPedidoProdutos().get(index);
						produto.setQuantidade(produto.getQuantidade() + quantia);
						Pedido updatedPedido = pedidoServices.salvar(pedido);
						return ResponseEntity.ok(EntityModel.of(updatedPedido,
								linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
								linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
										.withRel("pedidos")));
					}
					return ResponseEntity.badRequest().body("Invalid product index.");
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/diminuir-quantidade/{pedidoId}/{indexProduto}") /// pedidos//diminuir-quantidade/{pedidoId}/{indexProduto}?quantia=quantia
	public ResponseEntity<?> diminuirQuantidade(@PathVariable("pedidoId") Long Id,
			@PathVariable("indexProduto") int index, @RequestParam("quantia") int quantia) {
		return pedidoRepository.findById(Id)
				.map(pedido -> {
					if (index >= 0 && index < pedido.getPedidoProdutos().size()) {
						PedidoProduto produto = pedido.getPedidoProdutos().get(index);
						int newQuantity = Math.max(0, produto.getQuantidade() - quantia); // Prevents quantity from
																							// going negative
						produto.setQuantidade(newQuantity);
						Pedido updatedPedido = pedidoServices.salvar(pedido);
						return ResponseEntity.ok(EntityModel.of(updatedPedido,
								linkTo(methodOn(PedidoControler.class).buscar(Id)).withSelfRel(),
								linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null))
										.withRel("pedidos")));
					}
					return ResponseEntity.badRequest().body("Invalid product index.");
				})
				.orElse(ResponseEntity.notFound().build());
	}

	// Comandos DELET
	@DeleteMapping("/{pedidoId}")
	public ResponseEntity<?> remover(@PathVariable("pedidoId") Long Id) {
		try {
			pedidoServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location",
							linkTo(methodOn(PedidoControler.class).listar(null, null, null, null, null, null)).toUri()
									.toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

}
