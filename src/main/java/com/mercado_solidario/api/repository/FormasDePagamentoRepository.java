package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.FormasDePagamento;

@Repository
public interface FormasDePagamentoRepository extends JpaRepository<FormasDePagamento, Long>{
	
	List<FormasDePagamento> findAllByDescricaoContains(String descricao);

}
