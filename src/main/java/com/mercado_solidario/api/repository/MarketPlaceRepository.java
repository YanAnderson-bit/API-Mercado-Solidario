package com.mercado_solidario.api.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.MarketPlace;

@Repository
public interface MarketPlaceRepository extends JpaRepository<MarketPlace, Long>{
	
	List<MarketPlace> findAllByNomeContains(String nome);
	
	List<MarketPlace> findAllByClassificacaoContains(String classificacao);
	
	List<MarketPlace> findAllByTaxaFreteBetween(BigDecimal taxaInicial, BigDecimal taxaFinal);
	
	List<MarketPlace> findAllByTaxaFreteGreaterThanEqual(BigDecimal taxa);
	
	List<MarketPlace> findAllByTaxaFreteLessThanEqual(BigDecimal taxa);
	
	List<MarketPlace> findAllByAtivo(boolean ativo);
	
	List<MarketPlace> findAllByAberto(boolean aberto);
	
	List<MarketPlace> findAllByDataCadastroBetween(Date dataInicial, Date dataFinal);
	
	List<MarketPlace> findAllByDataCadastroGreaterThanEqual(Date data);
	
	List<MarketPlace> findAllByDataCadastroLessThanEqual(Date data);
	
	List<MarketPlace> findAllByDataAtualizacaoBetween(Date dataInicial, Date dataFinal);
	
	List<MarketPlace> findAllByDataAtualizacaoGreaterThanEqual(Date data);
	
	List<MarketPlace> findAllByDataAtualizacaoLessThanEqual(Date data);
	
	List<MarketPlace> findAllByFormasDePagamentoId(Long formasDePagamentoId);
	
	List<MarketPlace> findAllByFormasDePagamentoDescricaoContains(String descricao);
	
	List<MarketPlace> findAllByEndereçoCidadeNomeContains(String cidade);
	
	List<MarketPlace> findAllByEndereçoCidadeEstadoNomeContains(String estado);
}