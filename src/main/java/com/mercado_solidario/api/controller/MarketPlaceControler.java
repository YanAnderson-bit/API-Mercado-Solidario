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

import javax.persistence.criteria.Predicate;
import javax.swing.GroupLayout.Group;
import javax.persistence.criteria.Join;

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
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.entity.MarketPlace;
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.MarketPlaceRepository;
import com.mercado_solidario.api.service.MarketPlaceServices;

@RestController
@RequestMapping(value = "/marketplaces")
public class MarketPlaceControler {

	@Autowired
	private MarketPlaceRepository marketplaceRepository;

	@Autowired
	private MarketPlaceServices marketplaceServices;

	// Comando GET
	@GetMapping
	public List<MarketPlace> listar(@RequestParam(required = false) String nome,
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
		return marketplaceRepository.findAll(spec);
	}

	@GetMapping("/{marketplaceId}") // -> /marketplace/marketplaceId
	public ResponseEntity<MarketPlace> buscar(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isPresent()) {
			return ResponseEntity.ok(marketplace.get());
		}

		return ResponseEntity.notFound().build();
	}

	// -> /marketplace/marketplaceId/fornecedores
	@GetMapping("/{marketplaceId}/fornecedores")
	public List<Fornecedor> FornecedoresPorMarketPlaces(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isPresent()) {
			List<Fornecedor> fornecedors = marketplace.get().getFornecedores();
			return fornecedors;
		}
		return null;
	}

	// -> /marketplace/marketplaceId/produtos
	@GetMapping("/{marketplaceId}/produtos")
	public List<List<Produto>> ProdutosPorMarketPlaces(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isPresent()) {
			List<Fornecedor> fornecedors = marketplace.get().getFornecedores();
			List<List<Produto>> listProdutos = new ArrayList<>();
			fornecedors.forEach(fornecedor -> listProdutos.add(fornecedor.getProdutos()));
			return listProdutos;
		}
		return null;
	}

	// -> /marketplace/marketplaceId/pedidos
	@GetMapping("/{marketplaceId}/pedidos")
	public List<Pedido> PedidosPorMarketPlaces(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isPresent()) {
			List<Pedido> pedidos = marketplace.get().getPedidos();
			return pedidos;
		}
		return null;
	}

	// Comando POST
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MarketPlace adicionar(@RequestBody MarketPlace marketplace) {
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
		return marketplaceServices.salvar(marketplace);
	}

	// Comandos PUT
	@PutMapping("/{marketplaceId}")
	public ResponseEntity<?> atualizar(@PathVariable("marketplaceId") Long Id, @RequestBody MarketPlace marketplace) {
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
		try {
			Optional<MarketPlace> marketplaceAtual = marketplaceRepository.findById(Id);

			if (marketplaceAtual.isPresent()) {
				marketplace.setDataCadastro(marketplaceAtual.get().getDataCadastro());
				BeanUtils.copyProperties(marketplace, marketplaceAtual.get(), "id", "endereço", "dataCadastro");
				MarketPlace marketplaceSalvo = marketplaceServices.salvar(marketplaceAtual.get());

				return ResponseEntity.ok(marketplaceSalvo);
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
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		merge(campos, marketplace.get());
		return atualizar(Id, marketplace.get());
	}

	@PatchMapping("/{marketplaceId}/abrir")
	public ResponseEntity<?> abrir(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		marketplace.get().abrir();

		return atualizar(Id, marketplace.get());
	}

	@PatchMapping("/{marketplaceId}/fechar")
	public ResponseEntity<?> fechar(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		marketplace.get().fechar();

		return atualizar(Id, marketplace.get());
	}

	@PatchMapping("/{marketplaceId}/ativar")
	public ResponseEntity<?> ativar(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		marketplace.get().ativar();

		return atualizar(Id, marketplace.get());
	}

	@PatchMapping("/{marketplaceId}/desativar")
	public ResponseEntity<?> desativar(@PathVariable("marketplaceId") Long Id) {
		Optional<MarketPlace> marketplace = marketplaceRepository.findById(Id);

		if (marketplace.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		marketplace.get().desativar();

		return atualizar(Id, marketplace.get());
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
	public ResponseEntity<MarketPlace> remover(@PathVariable("marketplaceId") Long Id) {
		try {

			marketplaceServices.excluir(Id);
			return ResponseEntity.noContent().build();

		} catch (EntidadeNaoEncontradaExeption e) {
			return ResponseEntity.notFound().build();
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

	}

}
