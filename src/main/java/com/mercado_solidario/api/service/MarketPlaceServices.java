package com.mercado_solidario.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.FormasDePagamento;
import com.mercado_solidario.api.entity.Fornecedor;
import com.mercado_solidario.api.entity.MarketPlace;
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.CidadeRepository;
import com.mercado_solidario.api.repository.FormasDePagamentoRepository;
import com.mercado_solidario.api.repository.FornecedorRepository;
import com.mercado_solidario.api.repository.MarketPlaceRepository;
import com.mercado_solidario.api.repository.PedidoRepository;

@Service
public class MarketPlaceServices {

	@Autowired 
	private MarketPlaceRepository marketplaceRepository;
	
	@Autowired 
	private FormasDePagamentoRepository formasDePagamentoRepository;
	
	@Autowired 
	private FornecedorRepository fornecedorRepository;
	
	@Autowired 
	private PedidoRepository pedidoRepository;
	
	@Autowired 
	private CidadeRepository cidadeRepository;
//	@Autowired 
//	private EndereçoRepository endereçoRepository;
	
	public MarketPlace salvar(MarketPlace marketplace) {
		
		if(marketplace.getId()==null) {
			marketplace.setDataCadastro(Date.from(Instant.now()));
		}
		
		marketplace.setDataAtualizacao(Date.from(Instant.now()));
		
	/*	Long Id = marketplace.getEndereço().getId();	
		Endereço endereço = endereçoRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de endereço de código %d", Id)));
		
		marketplace.setEndereço(endereço);
		*/

		Long IdCidade = marketplace.getEndereço().getCidade().getId();	
		Cidade cidade = cidadeRepository.findById(IdCidade)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de cidade de código %d", IdCidade)));
		
		marketplace.getEndereço().setCidade(cidade);
		
		
		List<Long> idsFormasPagamento = new ArrayList<>();
		marketplace.getFormasDePagamento().forEach(f -> idsFormasPagamento.add(f.getId()));
		
		List<FormasDePagamento> formasDePagamentos = new ArrayList<>();
		for(Long id: idsFormasPagamento) {
			FormasDePagamento formasDePagamento = formasDePagamentoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de forma de pagamento de código %d", id)));
			formasDePagamentos.add(formasDePagamento);
		}
		
		List<Long> idsFornecedores = new ArrayList<>();
		marketplace.getFormasDePagamento().forEach(f -> idsFornecedores.add(f.getId()));
		
		List<Fornecedor> fornecedores = new ArrayList<>();
		for(Long id: idsFornecedores) {
			Fornecedor fornecedor = fornecedorRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de fornecedor de código %d", id)));
			fornecedores.add(fornecedor);
		}
		
		List<Long> idsPedidos = new ArrayList<>();
		marketplace.getPedidos().forEach(f -> idsPedidos.add(f.getId()));
		
		List<Pedido> pedidos = new ArrayList<>();
		for(Long id: idsPedidos) {
			Pedido pedido = pedidoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de pedido de código %d", id)));
			pedidos.add(pedido);
		}
		
		marketplace.setFornecedors(fornecedores);
		
		marketplace.setFormasDePagamento(formasDePagamentos);
		
		marketplace.setPedidos(pedidos);

		return marketplaceRepository.save(marketplace);
	}
	
	public void excluir(Long Id){ 
		try {
			MarketPlace marketPlaceAntigo = marketplaceRepository.findById(Id).get();
			marketplaceRepository.deleteById(Id);
			
			List<Fornecedor> fornecedores = marketPlaceAntigo.getFornecedors();
			fornecedores.forEach(fornecedor -> fornecedorRepository.delete(fornecedor));
			
			List<Pedido> pedidos = marketPlaceAntigo.getPedidos();
			pedidos.forEach(pedido -> pedidoRepository.delete(pedido));
			
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro da marketplace de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Marketplace de código %d não pode ser removida por estar em uso", Id));
		}
	}

}