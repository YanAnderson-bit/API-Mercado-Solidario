package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long>{
	
	
	List<Estado> findAllByNomeContains(String nome);
	
	List<Estado> findAllBySiglaLike(String sigla);

}