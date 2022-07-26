package com.mercado_solidario.api.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
	List<Usuario> findAllByNomeContains(String nome);
	
	List<Usuario> findAllByEmailContains(String email);
	
	List<Usuario> findAllByDataCadastroGreaterThanEqual(Date dataCadastro);
	
	List<Usuario> findAllByEndereçoCidadeNomeContains(String cidade);
	
	List<Usuario> findAllByEndereçoCidadeEstadoNomeContains(String estado);

}
