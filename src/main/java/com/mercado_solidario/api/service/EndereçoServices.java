package com.mercado_solidario.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.CidadeRepository;
import com.mercado_solidario.api.repository.EndereçoRepository;

//@Service
public class EndereçoServices {
/*
	@Autowired 
	private EndereçoRepository endereçoRepository;
	
	@Autowired 
	private CidadeRepository cidadeRepository;
	
	@Bean
	public Endereço salvar(Endereço endereço) {
		Long Id = endereço.getCidade().getId();	
		Cidade cidade = cidadeRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de didade de código %d", Id)));
		
		endereço.setCidade(cidade);
		
		return endereçoRepository.save(endereço);
	}
	
	@Bean
	public void excluir(Long Id){ 
		try {
			endereçoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da endereço de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Endereço de código %d não pode ser removida por estar em uso", Id));
		}
	}
*/
}
