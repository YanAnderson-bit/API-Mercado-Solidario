package com.mercado_solidario.api.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class MarketPlace {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String nome;
	
	@Column(nullable = true)
	private String classificacao;
	
	@Column(nullable = false)
	private BigDecimal taxaFrete;
	
	@Column(nullable = false)
	private boolean ativo;
	
	@Column(nullable = false)
	private boolean aberto;
	
	//@JsonIgnore
	@CreationTimestamp
	@Column(nullable = false)
	private Date dataCadastro;
	
	//@JsonIgnore
	@UpdateTimestamp
	@Column(nullable = false)
	private Date dataAtualizacao;
	
	//@OneToOne
	//@JoinColumn
	@JsonIgnore
	@Embedded
	private Endereço endereço;
	
	//@JsonIgnore
	@ManyToMany
	//@JoinColumn(nullable = false)
	private List<FormasDePagamento> formasDePagamento = new ArrayList<>();
}
