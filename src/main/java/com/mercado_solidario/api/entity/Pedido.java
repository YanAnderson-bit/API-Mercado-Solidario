package com.mercado_solidario.api.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercado_solidario.api.enumarations.StatusPedido;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Pedido {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String codigo;
	
	@Column(nullable = false)
	private BigDecimal subTotal;
	
	@Column(nullable = false)
	private BigDecimal taxaFrete;
	
	@Column(nullable = false)
	private BigDecimal valorTotal;
	
	//@JsonIgnore
	@CreationTimestamp
	@Column(nullable = false)
	private Date dataCriacao;
	
	@Column(nullable = true)
	private Date dataConfirmação;
	
	@Column(nullable = true)
	private Date dataCançelamento;
	
	@Column(nullable = true)
	private Date dataEntrega;
	
	@Column(nullable = false)
	private StatusPedido status;//Status Pedido
	
	@JsonIgnoreProperties(value = {"grupo", "endereço"})
	@ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
	
	@JsonIgnore
	@ManyToOne//(fetch = FetchType.LAZY)
	private MarketPlace marketPlace;
	
	//@ManyToOne
    //@JoinColumn(name = "endereço_id")
	@JsonIgnore
	@Embedded
    private Endereço endereço;	

	@JsonIgnoreProperties(value = {"pedido"})
	@OneToMany(mappedBy="pedido", cascade = CascadeType.ALL)
    private List<PedidoProduto> pedidoProdutos = new ArrayList<>();
	
	public void incrementoDecremento(int index, int qauntidade, boolean incremento) {
		List<PedidoProduto> pedidoProdutosAlterado = this.getPedidoProdutos();
		if (incremento) {
			pedidoProdutosAlterado.get(index).addQuantidade(qauntidade);
		}else {
			pedidoProdutosAlterado.get(index).subQuantidade(qauntidade);
		}
		this.setPedidoProdutos(pedidoProdutosAlterado);
		this.calcularSubTotal();
		this.calcularValorTotal();
    }
	
	public void calcularSubTotal() {
		float soma = 0;
		for(int i=0; i<this.getPedidoProdutos().size();i++) {
			soma+=this.getPedidoProdutos().get(i).getPrecoTotal().floatValue();
		}
		this.setSubTotal(BigDecimal.valueOf(soma));
	}
	
	public void calcularValorTotal() {
		this.setValorTotal(this.getSubTotal().multiply(this.getTaxaFrete().add(BigDecimal.valueOf(1))));
	}
	
	public void criacao(Pedido pedido) {//o que estiver confirmado não pode ser alterado
			this.setStatus(StatusPedido.CRIADO);
		if(pedido==null) {
			this.setDataCriacao(Date.from(Instant.now()));
		}else{
			this.setDataCriacao(pedido.getDataCriacao());
		}
	}
	
	public void confirmar() {
		this.setDataConfirmação(Date.from(Instant.now()));
		this.setStatus(StatusPedido.CONFIRMADO);
	}
	
	public void cancelar() {
		this.setDataCançelamento(Date.from(Instant.now()));
		this.setStatus(StatusPedido.CANCELADO);
	}
	
	public void entregue() {
		this.setDataCançelamento(Date.from(Instant.now()));
		this.setStatus(StatusPedido.ENTREGUE);
	}
	
}
