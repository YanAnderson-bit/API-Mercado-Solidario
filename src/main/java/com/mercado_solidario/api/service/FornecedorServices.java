package com.mercado_solidario.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.EndereçoRepository;
import com.mercado_solidario.api.repository.FornecedorRepository;

@Service
public class FornecedorServices {
	
	@Autowired 
	private FornecedorRepository fornecedorRepository;
	
	@Autowired 
	private EndereçoRepository endereçoRepository;
	
	public Fornecedor salvar(Fornecedor fornecedor) { 
		Long Id = fornecedor.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", Id)));
		
		fornecedor.setEndereço(endereço);
		
		return fornecedorRepository.save(fornecedor);
	}
	
	public void excluir(Long Id){ 
		try {
			fornecedorRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da fornecedor de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Fornecedor de código %d não pode ser removida por estar em uso", Id));
		}
	}
	
}
