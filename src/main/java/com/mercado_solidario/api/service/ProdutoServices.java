package com.mercado_solidario.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.FornecedorRepository;
import com.mercado_solidario.api.repository.ProdutoRepository;

@Service
public class ProdutoServices {

	@Autowired 
	private ProdutoRepository produtoRepository;
	
	@Autowired 
	private FornecedorRepository fornecedorRepository;
	
	public Produto salvar(Produto produto) {
		
		Long id = produto.getFornecedor().getId();
		Fornecedor fornecedor = fornecedorRepository.findById(id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de profuyo de código %d", id)));

		produto.setFornecedor(fornecedor);
		
		return produtoRepository.save(produto);
	}
	
	public void excluir(Long Id){ 
		try {
			produtoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe produto de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Produto de código %d não pode ser removido", Id));
		}
	}

}
