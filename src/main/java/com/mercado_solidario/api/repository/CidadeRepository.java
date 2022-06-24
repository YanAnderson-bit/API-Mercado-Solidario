package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Cidade;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {

	List<Cidade> findAllByNomeContains(String nome);
	
	List<Cidade> findAllByEstadoNomeContains(String nome);
	
	//List<Cidade> findAllByEstadoId(Long id);//Não funciona
	
}
