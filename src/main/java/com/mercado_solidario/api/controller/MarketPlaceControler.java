package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;
import javax.swing.GroupLayout.Group;
import javax.persistence.criteria.Join;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.mercado_solidario.api.entity.MarketPlace;
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.MarketPlaceRepository;
import com.mercado_solidario.api.service.MarketPlaceServices;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/marketplaces")
public class MarketPlaceControler {

	@Autowired
	private MarketPlaceRepository marketplaceRepository;

	@Autowired
	private MarketPlaceServices marketplaceServices;

	// Comando GET
	@GetMapping
	public CollectionModel<EntityModel<MarketPlace>> listar(@RequestParam(required = false) String nome,
			@RequestParam(required = false) String email,
			@RequestParam(required = false) String cidade,
			@RequestParam(required = false) String estado,
			@RequestParam(required = false) String classificacao,
			@RequestParam(required = false) Boolean ativo,
			@RequestParam(required = false) Boolean aberto,
			@RequestParam(required = false) BigDecimal taxaFreteInicial,
			@RequestParam(required = false) BigDecimal taxaFreteFinal,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
		Specification<MarketPlace> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (nome != null) {
				predicates.add(
						criteriaBuilder.like(root.get("nome"), "%" + nome + "%"));
			}
			if (email != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("email"), "%" + email + "%"));
			}
			if (classificacao != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("classificacao"), "%" + classificacao + "%"));
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
			if (ativo != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("ativo"), ativo));
			}
			if (aberto != null) {
				predicates.add(
						criteriaBuilder.equal(root.get("aberto"), aberto));
			}
			if (cidade != null) {
				Join<MarketPlace, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				predicates.add(criteriaBuilder.like(cidadeJoin.get("nome"), "%" + cidade + "%"));
			}
			if (estado != null) {
				Join<MarketPlace, Endereço> enderecoJoin = root.join("endereço");
				Join<Endereço, Cidade> cidadeJoin = enderecoJoin.join("cidade");
				Join<Cidade, Estado> estadoJoin = cidadeJoin.join("estado");
				predicates.add(criteriaBuilder.like(estadoJoin.get("nome"), "%" + estado + "%"));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		List<EntityModel<MarketPlace>> marketplaces = marketplaceRepository.findAll(spec).stream()
				.map(marketplace -> EntityModel.of(marketplace,
						linkTo(methodOn(MarketPlaceControler.class).buscar(marketplace.getId())).withSelfRel(),
						linkTo(methodOn(MarketPlaceControler.class).listar(nome, email, cidade, estado, classificacao,
								ativo, aberto, taxaFreteInicial, taxaFreteFinal, dataInicio, dataFim))
								.withRel("marketplaces")))
				.collect(Collectors.toList());
		return CollectionModel.of(marketplaces,
				linkTo(methodOn(MarketPlaceControler.class).listar(nome, email, cidade, estado, classificacao, ativo,
						aberto, taxaFreteInicial, taxaFreteFinal, dataInicio, dataFim)).withSelfRel());
	}

	@GetMapping("/{marketplaceId}") // -> /marketplace/marketplaceId
	public ResponseEntity<EntityModel<MarketPlace>> buscar(@PathVariable("marketplaceId") Long Id) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> EntityModel.of(marketplace,
						linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withSelfRel(),
						linkTo(methodOn(MarketPlaceControler.class).remover(Id)).withRel("delete"),
						linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
								null, null, null, null)).withRel("marketplaces"),
						linkTo(methodOn(MarketPlaceControler.class).FornecedoresPorMarketPlaces(Id))
								.withRel("fornecedores"),
						linkTo(methodOn(MarketPlaceControler.class).abrir(Id))
								.withRel("abrir"),
						linkTo(methodOn(MarketPlaceControler.class).ativar(Id))
								.withRel("ativar"),
						linkTo(methodOn(MarketPlaceControler.class).fechar(Id))
								.withRel("fechar"),
						linkTo(methodOn(MarketPlaceControler.class).desativar(Id))
								.withRel("desativar"),
						linkTo(methodOn(MarketPlaceControler.class).ProdutosPorMarketPlaces(Id)).withRel("produtos"),
						linkTo(methodOn(MarketPlaceControler.class).PedidosPorMarketPlaces(Id)).withRel("pedidos")))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// -> /marketplace/marketplaceId/fornecedores
	@GetMapping("/{marketplaceId}/fornecedores")
	public ResponseEntity<CollectionModel<EntityModel<Fornecedor>>> FornecedoresPorMarketPlaces(
			@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);
		if (marketplace.isPresent()) {
			List<EntityModel<Fornecedor>> fornecedores = marketplace.get().getFornecedores().stream()
					.map(fornecedor -> EntityModel.of(fornecedor,
							linkTo(methodOn(FornecedorControler.class).buscar(fornecedor.getId())).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).FornecedoresPorMarketPlaces(Id))
									.withRel("fornecedores")))
					.collect(Collectors.toList());
			return ResponseEntity.ok(CollectionModel.of(fornecedores,
					linkTo(methodOn(MarketPlaceControler.class).FornecedoresPorMarketPlaces(Id)).withSelfRel()));
		}
		return ResponseEntity.notFound().build();
	}

	// -> /marketplace/marketplaceId/produtos
	@GetMapping("/{marketplaceId}/produtos")
	public ResponseEntity<CollectionModel<EntityModel<List<EntityModel<Produto>>>>> ProdutosPorMarketPlaces(
			@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);
		if (marketplace.isPresent()) {
			List<EntityModel<List<EntityModel<Produto>>>> produtos = marketplace.get().getFornecedores().stream()
					.map(fornecedor -> EntityModel.of(fornecedor.getProdutos().stream()
							.map(produto -> EntityModel.of(produto,
									linkTo(methodOn(ProdutoControler.class).buscar(produto.getId())).withSelfRel()))
							.collect(Collectors.toList()),
							linkTo(methodOn(FornecedorControler.class).buscar(fornecedor.getId()))
									.withRel("fornecedor")))
					.collect(Collectors.toList());
			return ResponseEntity.ok(CollectionModel.of(produtos,
					linkTo(methodOn(MarketPlaceControler.class).ProdutosPorMarketPlaces(Id)).withSelfRel()));
		}
		return ResponseEntity.notFound().build();
	}

	// -> /marketplace/marketplaceId/pedidos
	@GetMapping("/{marketplaceId}/pedidos")
	public ResponseEntity<CollectionModel<EntityModel<Pedido>>> PedidosPorMarketPlaces(
			@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);
		if (marketplace.isPresent()) {
			List<EntityModel<Pedido>> pedidos = marketplace.get().getPedidos().stream()
					.map(pedido -> EntityModel.of(pedido,
							linkTo(methodOn(PedidoControler.class).buscar(pedido.getId())).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).PedidosPorMarketPlaces(Id)).withRel("pedidos")))
					.collect(Collectors.toList());
			return ResponseEntity.ok(CollectionModel.of(pedidos,
					linkTo(methodOn(MarketPlaceControler.class).PedidosPorMarketPlaces(Id)).withSelfRel()));
		}
		return ResponseEntity.notFound().build();
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	/*
	 * Modelo:
	 * {
	 * "nome": nome,
	 * "classificacao": classificacao,
	 * "taxaFrete": taxaFrete,
	 * "ativo": ativo,
	 * "aberto": aberto,
	 * "endereço": {
	 * "id": id,
	 * },
	 * "formasDePagamento": [{
	 * "id": id,
	 * }]
	 * }
	 */
	public ResponseEntity<EntityModel<MarketPlace>> adicionar(@RequestBody MarketPlace marketplace) {
		MarketPlace savedMarketplace = marketplaceServices.salvar(marketplace);
		return ResponseEntity
				.created(linkTo(methodOn(MarketPlaceControler.class).buscar(savedMarketplace.getId())).toUri())
				.body(EntityModel.of(savedMarketplace,
						linkTo(methodOn(MarketPlaceControler.class).buscar(savedMarketplace.getId())).withSelfRel(),
						linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
								null, null, null, null)).withRel("marketplaces")));
	}

	// Comandos PUT
	@PutMapping("/{marketplaceId}")
	public ResponseEntity<?> atualizar(@PathVariable("marketplaceId") Long Id, @RequestBody MarketPlace marketplace) {
		try {
			Optional<MarketPlace> marketplaceAtual = marketplaceRepository.findById(Id);
			if (marketplaceAtual.isPresent()) {
				marketplace.setDataCadastro(marketplaceAtual.get().getDataCadastro());
				BeanUtils.copyProperties(marketplace, marketplaceAtual.get(), "id", "endereço", "dataCadastro");
				MarketPlace updatedMarketplace = marketplaceServices.salvar(marketplaceAtual.get());
				return ResponseEntity.ok(EntityModel.of(updatedMarketplace,
						linkTo(methodOn(MarketPlaceControler.class).buscar(updatedMarketplace.getId())).withSelfRel(),
						linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
								null, null, null, null)).withRel("marketplaces")));
			}
			return ResponseEntity.notFound().build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// Comando PATCH
	@PatchMapping("/{marketplaceId}")
	public ResponseEntity<?> atualizaParcial(@PathVariable("marketplaceId") Long Id,
			@RequestBody Map<String, Object> campos) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> {
					merge(campos, marketplace);
					marketplaceServices.salvar(marketplace);
					return ResponseEntity.ok(EntityModel.of(marketplace,
							linkTo(methodOn(MarketPlaceControler.class).buscar(marketplace.getId())).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
									null, null, null, null)).withRel("marketplaces")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{marketplaceId}/abrir")
	public ResponseEntity<?> abrir(@PathVariable("marketplaceId") Long Id) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> {
					marketplace.abrir();
					MarketPlace updatedMarketplace = marketplaceServices.salvar(marketplace);
					return ResponseEntity.ok(EntityModel.of(updatedMarketplace,
							linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).fechar(Id)).withRel("fechar"),
							linkTo(methodOn(MarketPlaceControler.class).desativar(Id)).withRel("desativar")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{marketplaceId}/fechar")
	public ResponseEntity<?> fechar(@PathVariable("marketplaceId") Long Id) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> {
					marketplace.fechar();
					MarketPlace updatedMarketplace = marketplaceServices.salvar(marketplace);
					return ResponseEntity.ok(EntityModel.of(updatedMarketplace,
							linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).abrir(Id)).withRel("abrir"),
							linkTo(methodOn(MarketPlaceControler.class).ativar(Id)).withRel("ativar")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{marketplaceId}/ativar")
	public ResponseEntity<?> ativar(@PathVariable("marketplaceId") Long Id) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> {
					marketplace.ativar();
					MarketPlace updatedMarketplace = marketplaceServices.salvar(marketplace);
					return ResponseEntity.ok(EntityModel.of(updatedMarketplace,
							linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).desativar(Id)).withRel("desativar")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{marketplaceId}/desativar")
	public ResponseEntity<?> desativar(@PathVariable("marketplaceId") Long Id) {
		return marketplaceRepository.findById(Id)
				.map(marketplace -> {
					marketplace.desativar();
					MarketPlace updatedMarketplace = marketplaceServices.salvar(marketplace);
					return ResponseEntity.ok(EntityModel.of(updatedMarketplace,
							linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withSelfRel(),
							linkTo(methodOn(MarketPlaceControler.class).ativar(Id)).withRel("ativar")));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	private void merge(Map<String, Object> camposOrigem, MarketPlace marketplaceDestino) {
		ObjectMapper objectMapper = new ObjectMapper();
		MarketPlace marketplaceOrigem = objectMapper.convertValue(camposOrigem, MarketPlace.class);

		camposOrigem.forEach((nomePropiedade, valorPropiedade) -> {
			Field field = ReflectionUtils.findField(MarketPlace.class, nomePropiedade);
			field.setAccessible(true);
			Object novoValor = ReflectionUtils.getField(field, marketplaceOrigem);

			ReflectionUtils.setField(field, marketplaceDestino, novoValor);
		});
	}

	// Comandos DELET
	@DeleteMapping("/{marketplaceId}")
	public ResponseEntity<?> remover(@PathVariable("marketplaceId") Long Id) {
		try {
			marketplaceServices.excluir(Id);
			return ResponseEntity.noContent()
					.header("Location",
							linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
									null, null, null, null)).toUri().toString())
					.build();
		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(EntityModel.of(null,
							linkTo(methodOn(MarketPlaceControler.class).buscar(Id)).withRel(IanaLinkRelations.SELF),
							linkTo(methodOn(MarketPlaceControler.class).listar(null, null, null, null, null, null, null,
									null, null, null, null)).withRel("marketplaces")));
		}
	}

}
