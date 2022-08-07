package com.mercado_solidario.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Endereço;
import com.mercado_solidario.api.entity.FormasDePagamento;
import com.mercado_solidario.api.entity.MarketPlace;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.EndereçoRepository;
import com.mercado_solidario.api.repository.FormasDePagamentoRepository;
import com.mercado_solidario.api.repository.MarketPlaceRepository;

@Service
public class MarketPlaceServices {

	@Autowired 
	private MarketPlaceRepository marketplaceRepository;
	
	@Autowired 
	private FormasDePagamentoRepository formasDePagamentoRepository;
	
	@Autowired 
	private EndereçoRepository endereçoRepository;
	
	public MarketPlace salvar(MarketPlace marketplace) {
		
		if(marketplace.getId()==null) {
			marketplace.setDataCadastro(Date.from(Instant.now()));
		}
		
		marketplace.setDataAtualizacao(Date.from(Instant.now()));
		
		Long Id = marketplace.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", Id)));
		
		marketplace.setEndereço(endereço);
		
		List<Long> idsFormasPagamento = new ArrayList<>();
		marketplace.getFormasDePagamento().forEach(f -> idsFormasPagamento.add(f.getId()));
		
		List<FormasDePagamento> formasDePagamentos = new ArrayList<>();
		for(Long id: idsFormasPagamento) {
			FormasDePagamento formasDePagamento = formasDePagamentoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de forma de pagamento de código %d", id)));
			formasDePagamentos.add(formasDePagamento);
		}
		
		marketplace.setFormasDePagamento(formasDePagamentos);

		return marketplaceRepository.save(marketplace);
	}
	
	public void excluir(Long Id){ 
		try {
			marketplaceRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da marketplace de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Marketplace de código %d não pode ser removida por estar em uso", Id));
		}
	}

}