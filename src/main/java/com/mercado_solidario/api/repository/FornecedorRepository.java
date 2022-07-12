package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Fornecedor;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

	List<Fornecedor> findAllByNomeContains(String nome);
	
	List<Fornecedor> findAllByEndereçoCidadeNomeContains(String cidade);
	
	List<Fornecedor> findAllByEndereçoCidadeEstadoNomeContains(String estado);
	
}
