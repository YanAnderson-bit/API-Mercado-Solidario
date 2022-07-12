package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Permissao;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long>{
	
	List<Permissao> findAllByNomeContains(String nome);
	
	List<Permissao> findAllByDescricaoContains(String descricao);

}
