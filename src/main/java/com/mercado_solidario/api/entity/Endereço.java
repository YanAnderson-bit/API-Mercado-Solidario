package com.mercado_solidario.api.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//@Entity
@Embeddable
public class Endereço {
/*
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String cep;
	
	@Column(nullable = true)
	private String logadouro;
	
	@Column(nullable = false)
	private Integer numero;
	
	@Column(nullable = false)
	private String complemento;
	
	@Column(nullable = false)
	private String bairro;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Cidade cidade;
*/
	@Column(nullable = false, name = "endereco_cep")
	private String cep;
	
	@Column(nullable = true, name = "endereco_logadouro")
	private String logadouro;
	
	@Column(nullable = false, name = "endereco_numero")
	private Integer numero;
	
	@Column(nullable = true, name = "endereco_complemento")
	private String complemento;
	
	@Column(nullable = false, name = "endereco_bairro")
	private String bairro;
	
	@ManyToOne
	@JoinColumn(nullable = false, name = "endereco_cidade")
	private Cidade cidade;

}
