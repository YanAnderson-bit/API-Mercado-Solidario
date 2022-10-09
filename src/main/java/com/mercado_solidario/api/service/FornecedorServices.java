package com.mercado_solidario.api.service;

import java.util.ArrayList;
import java.util.List;

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
public class FornecedorServices {
	
	@Autowired 
	private FornecedorRepository fornecedorRepository;
	
//	@Autowired 
//	private EndereçoRepository endereçoRepository;
	
	@Autowired 
	private ProdutoRepository produtoRepository;
	
	public Fornecedor salvar(Fornecedor fornecedor) { 
	/*	Long Id = fornecedor.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", Id)));
		*/
		List<Long> idsPermissao = new ArrayList<>();
		fornecedor.getProdutos().forEach(t -> idsPermissao.add(t.getId()));
		
		List<Produto> produtos = new ArrayList<>();
		for(Long id: idsPermissao) {
			Produto produto = produtoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de profuyo de código %d", id)));
			produtos.add(produto);
		}
		fornecedor.setProdutos(produtos);//evitar duplicatas
		//fornecedor.setEndereço(endereço);
		
		return fornecedorRepository.save(fornecedor);
	}
	
	public void excluir(Long Id){ 
		try {
			Fornecedor fornecedorAntigo = fornecedorRepository.findById(Id).get();
			fornecedorRepository.deleteById(Id);
			List<Produto> produtos = fornecedorAntigo.getProdutos();
			produtos.forEach(produto -> produtoRepository.delete(produto));
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da fornecedor de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Fornecedor de código %d não pode ser removida por estar em uso", Id));
		}
	}
	
}
