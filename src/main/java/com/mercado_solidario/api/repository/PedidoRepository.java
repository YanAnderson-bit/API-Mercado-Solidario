package com.mercado_solidario.api.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findAllByCodigoContains(String codigo);
	
	List<Pedido> findAllByTaxaFreteBetween(BigDecimal taxaInicial, BigDecimal taxaFinal);
	
	List<Pedido> findAllByTaxaFreteGreaterThanEqual(BigDecimal taxa);
	
	List<Pedido> findAllByTaxaFreteLessThanEqual(BigDecimal taxa);
	
	List<Pedido> findAllByValorTotalBetween(BigDecimal valorInicial, BigDecimal valorFinal);
	
	List<Pedido> findAllByValorTotalGreaterThanEqual(BigDecimal valor);
	
	List<Pedido> findAllByValorTotalLessThanEqual(BigDecimal valor);
	
	List<Pedido> findAllByDataCriacaoBetween(Date dataInicial, Date dataFinal);
	
	List<Pedido> findAllByDataCriacaoGreaterThanEqual(Date data);
	
	List<Pedido> findAllByDataCriacaoLessThanEqual(Date data);
	
	List<Pedido> findAllByStatusEquals(String status);
	
	List<Pedido> findAllByPedidoProdutosProdutoId(Long id);
	
	List<Pedido> findAllByPedidoProdutosProdutoNomeContains(String produto);

}
