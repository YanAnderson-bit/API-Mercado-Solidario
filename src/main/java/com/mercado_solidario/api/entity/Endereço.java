package com.mercado_solidario.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Endereço {

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

}
