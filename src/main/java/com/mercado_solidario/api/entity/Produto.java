package com.mercado_solidario.api.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Produto {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String nome;
	
	@Column(nullable = true)
	private String descricao;
	
	@Column(nullable = false)
	private BigDecimal preco;
	
	@Column(nullable = false)
	private boolean disponivel;
	
	@Column(nullable = false)
	private String natureza;
	
	@Column(nullable = false)
	private String origem;
	
	@Column(nullable = false)
	private String categoria;
	
	@Column(nullable = true)
	private String manuseio;
	
	@Column(nullable = true)
	private String unidade;
	
	@Column(nullable = true)
	private Integer quantidade;
	
	@Column(nullable = true)
	private String frequenciaDisponibilidade;
	
	@Column(nullable = true)
	//Guarda a string com o valor Base64 da imagem, conversão deve ser tratada pelas apllicações
	private String fotoData;
	
	@JsonIgnore
	@ManyToOne
	private Fornecedor fornecedor;
}
