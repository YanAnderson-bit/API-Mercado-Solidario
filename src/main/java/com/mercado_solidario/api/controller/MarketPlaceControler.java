package com.mercado_solidario.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
	public List<MarketPlace> listar() {
		return marketplaceRepository.findAll();
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

	// -> /marketplace/por-nome?nome=nome_buscado
	@GetMapping("/por-nome")
	public List<MarketPlace> MarketPlacesPorNome(String nome) {
		return marketplaceRepository.findAllByNomeContains(nome);
	}

	// -> /marketplace/por-classificacao?classificacao=classificacao_buscado
	@GetMapping("/por-classificacao")
	public List<MarketPlace> MarketPlacesPorClassificacao(String classificacao) {
		return marketplaceRepository.findAllByClassificacaoContains(classificacao);
	}

	////////////////////
	// ->
	//////////////////// /marketplaces/entre-taxas-frete?taxaInicial=taxaInicial&taxaFinal=taxaFinal
	@GetMapping("/entre-taxas-frete")
	public List<MarketPlace> MarketPlacesEntreTaxasFrete(BigDecimal taxaInicial, BigDecimal taxaFinal) {
		return marketplaceRepository.findAllByTaxaFreteBetween(taxaInicial, taxaFinal);
	}

	// -> /marketplaces/com-taxa-frete-maior?taxa=taxa
	@GetMapping("/com-taxa-frete-maior")
	public List<MarketPlace> MarketPlacesComTaxaFreteMaior(BigDecimal taxa) {
		return marketplaceRepository.findAllByTaxaFreteGreaterThanEqual(taxa);
	}

	// -> /marketplaces/com-taxa-frete-menor?taxa=taxa
	@GetMapping("/com-taxa-frete-menor")
	public List<MarketPlace> MarketPlacesComTaxaFreteMenor(BigDecimal taxa) {
		return marketplaceRepository.findAllByTaxaFreteLessThanEqual(taxa);
	}

	////////////////////
	// -> /marketplace/por-ativo?ativo=ativo_buscado
	@GetMapping("/por-ativo")
	public List<MarketPlace> MarketPlacePorAtivo(boolean ativo) {
		return marketplaceRepository.findAllByAtivo(ativo);
	}

	// -> /marketplace/por-aberto?aberto=aberto_buscado
	@GetMapping("/por-aberto")
	public List<MarketPlace> MarketPlacePorAberto(boolean aberto) {
		return marketplaceRepository.findAllByAberto(aberto);
	}

	////////////////////
	// ->
	//////////////////// /pedidos/entre-datas-de-cadastro?dataInicial=MM/DD/AAAA&dataFinal=MM/DD/AAAA
	@GetMapping("/entre-datas-de-cadastro")
	public List<MarketPlace> MarketPlaceEntreDatasCadastro(Date dataInicial, Date dataFinal) {
		return marketplaceRepository.findAllByDataCadastroBetween(dataInicial, dataFinal);
	}

	// -> /pedidos/com-data-de-cadastro-maior?data=MM/DD/AAAA
	@GetMapping("/com-data-de-cadastro-maior")
	public List<MarketPlace> MarketPlaceComDataCadastroMaior(Date data) {
		return marketplaceRepository.findAllByDataCadastroGreaterThanEqual(data);
	}

	// -> /pedidos/com-data-de-cadastro-menor?data=MM/DD/AAAA
	@GetMapping("/com-data-de-cadastro-menor")
	public List<MarketPlace> MarketPlaceComDataCadastroMenor(Date data) {
		return marketplaceRepository.findAllByDataCadastroLessThanEqual(data);
	}

	////////////////////
	// ->
	//////////////////// /pedidos/entre-datas-de-ataulizacao?dataInicial=MM/DD/AAAA&dataFinal=MM/DD/AAAA
	@GetMapping("/entre-datas-de-ataulizacao")
	public List<MarketPlace> MarketPlaceEntreDatasAtaulizacao(Date dataInicial, Date dataFinal) {
		return marketplaceRepository.findAllByDataAtualizacaoBetween(dataInicial, dataFinal);
	}

	// -> /pedidos/com-data-de-ataulizacao-maior?data=MM/DD/AAAA
	@GetMapping("/com-data-de-ataulizacao-maior")
	public List<MarketPlace> MarketPlaceComDataAtaulizacaoMaior(Date data) {
		return marketplaceRepository.findAllByDataAtualizacaoGreaterThanEqual(data);
	}

	// -> /pedidos/com-data-de-ataulizacao-menor?data=MM/DD/AAAA
	@GetMapping("/com-data-de-ataulizacao-menor")
	public List<MarketPlace> MarketPlaceComDataAtaulizacaoMenor(Date data) {
		return marketplaceRepository.findAllByDataAtualizacaoLessThanEqual(data);
	}
	////////////////////

	// -> /marketplace/por-formasDePagamento-id?id=id
	@GetMapping("/por-formasDePagamento-id")
	public List<MarketPlace> MarketPlacePorFormasDePagamentoId(Long id) {
		return marketplaceRepository.findAllByFormasDePagamentoId(id);
	}

	// -> /marketplace/por-formasDePagamento-descricao?descricao=descricao
	@GetMapping("/por-formasDePagamento-descricao")
	public List<MarketPlace> MarketPlacePorFormasDePagamentoDescricao(String descricao) {
		return marketplaceRepository.findAllByFormasDePagamentoDescricaoContains(descricao);
	}
	////////////////////

	// -> /marketplace/por-cidade?cidade=cidade
	@GetMapping("/por-cidade")
	public List<MarketPlace> MarketPlacePorCidade(String cidade) {
		return marketplaceRepository.findAllByEndereçoCidadeNomeContains(cidade);
	}

	// -> /marketplace/por-estado?estado=estado
	@GetMapping("/por-estado")
	public List<MarketPlace> MarketPlacePorEstado(String estado) {
		return marketplaceRepository.findAllByEndereçoCidadeEstadoNomeContains(estado);
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
