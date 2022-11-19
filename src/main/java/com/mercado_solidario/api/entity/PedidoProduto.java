package com.mercado_solidario.api.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class PedidoProduto {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	@Min(1)
	private int quantidade;
	
	@Column(nullable = false)
	private BigDecimal precoTotal;
	
	@Column(nullable = true)
	private String observacao;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "pedido_id")
	private Pedido pedido;

	@ManyToOne
	@JoinColumn(name = "produto_id")
	private Produto produto;
	
	public void calcularTotal() {
		this.precoTotal = this.produto.getPreco().multiply(BigDecimal.valueOf(this.quantidade));
	}
	
	public void addQuantidade(int adicao) {
		this.quantidade = this.quantidade+adicao;
		this.calcularTotal();
	}
	
	public void subQuantidade(int subtracao) {
		this.quantidade = this.quantidade-subtracao;
		if(this.quantidade<0) this.quantidade=0;
		this.calcularTotal();
	}
	
	public PedidoProduto() {
		
	}

	public PedidoProduto(int quantidade, String observacao, Pedido pedido, Produto produto) {
		this.quantidade = quantidade;
		this.observacao = observacao;
		this.pedido = pedido;
		this.produto = produto;
	}
	
}
