package com.mercado_solidario.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Cidade;
import com.mercado_solidario.api.entity.MarketPlace;
import com.mercado_solidario.api.entity.Pedido;
import com.mercado_solidario.api.entity.PedidoProduto;
import com.mercado_solidario.api.entity.Produto;
import com.mercado_solidario.api.entity.Usuario;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.CidadeRepository;
import com.mercado_solidario.api.repository.MarketPlaceRepository;
import com.mercado_solidario.api.repository.PedidoRepository;
import com.mercado_solidario.api.repository.ProdutoRepository;
import com.mercado_solidario.api.repository.UsuarioRepository;

@Service
public class PedidoServices {
	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	// @Autowired
	// private EndereçoRepository endereçoRepository;

	@Autowired
	private CidadeRepository cidadeRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private MarketPlaceRepository marketplaceRepository;

	public Pedido salvar(Pedido pedido) {
		Long idUsuario = pedido.getUsuario().getId();
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de usuario de código %d", idUsuario)));

		Long Id = pedido.getMarketPlace().getId();
		MarketPlace marketPlace = marketplaceRepository.findById(Id)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de feira de código %d", Id)));
		;

		/*
		 * Long idEndereço = pedido.getEndereço().getId();
		 * Endereço endereço = endereçoRepository.findById(idEndereço)
		 * .orElseThrow(() -> new EntidadeNaoEncontradaExeption(
		 * String.format("Não existe cadastro de endereço de código %d", idEndereço)));
		 */

		Long IdCidade = pedido.getEndereço().getCidade().getId();
		Cidade cidade = cidadeRepository.findById(IdCidade)
				.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
						String.format("Não existe cadastro de cidade de código %d", IdCidade)));

		pedido.getEndereço().setCidade(cidade);

		List<Long> IdProdutos = new ArrayList<>();
		List<PedidoProduto> pedidosProdutos = new ArrayList<>();
		pedido.getPedidoProdutos().forEach(produto -> {
			IdProdutos.add(produto.getProduto().getId());
			pedidosProdutos.add(produto);
		});

		List<Produto> produtos = new ArrayList<>();
		for (Long id : IdProdutos) {
			Produto produto = produtoRepository.findById(id)
					.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
							String.format("Não existe cadastro de produto de código %d", id)));
			produtos.add(produto);
		}

		for (int i = 0; i < produtos.size(); i++) {
			pedidosProdutos.get(i).setProduto(produtos.get(i));
			pedidosProdutos.get(i).setPedido(pedido);
			pedidosProdutos.get(i).calcularTotal();
		}

		pedido.setPedidoProdutos(pedidosProdutos);

		pedido.calcularSubTotal();
		pedido.calcularValorTotal();

		if (pedido.getDataCriacao() == null) {
			pedido.criacao(null);
		}

		// pedido.setEndereço(endereço);
		pedido.setUsuario(usuario);
		pedido.setMarketPlace(marketPlace);

		return pedidoRepository.save(pedido);
	}

	public void excluir(Long Id) {
		try {
			pedidoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe pedido de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Pedido de código %d não pode ser removido", Id));
		}
	}

}
