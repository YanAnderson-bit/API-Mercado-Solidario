package com.mercado_solidario.api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Produto;


@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>{
	
	List<Produto> findAllByNomeContains(String nome);
	
	List<Produto> findAllByDescricaoContains(String descricao);
	
	List<Produto> findAllByPrecoGreaterThanEqual(BigDecimal preco);
	
	List<Produto> findAllByPrecoLessThanEqual(BigDecimal preco);
	
	List<Produto> findAllByPrecoBetween(BigDecimal menorPreco, BigDecimal maiorPreco);
	
	List<Produto> findAllByDisponivelEquals(boolean disponivel);
	
	List<Produto> findAllByNaturezaContains(String natureza);
	
	List<Produto> findAllByOrigemContains(String origem);
	
	List<Produto> findAllByCategoriaContains(String categoria);

}