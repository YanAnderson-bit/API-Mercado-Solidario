package com.mercado_solidario.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mercado_solidario.api.entity.Endereço;

@Repository
public interface EndereçoRepository {/* extends JpaRepository<Endereço, Long>{
	
	List<Endereço> findAllByCepContains(String cep);
	
	List<Endereço> findAllByNumero(Integer numero);
	
	List<Endereço> findAllByComplementoContains(String complement);
	
	List<Endereço> findAllByBairroContains(String bairro);
	
	List<Endereço> findAllByCidadeNomeContains(String nome);
	
	List<Endereço> findAllByCidadeEstadoNomeContains(String nome);
*/
}
