package com.mercado_solidario.api.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Fornecedor {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String nome;
	
//	@OneToOne
//	@JoinColumn
	@JsonIgnore
	@Embedded
	private Endereço endereço;
	
	//@JsonIgnore
	@OneToMany(mappedBy="fornecedor")
    private List<Produto> produtos = new ArrayList<>();
	
	@JsonIgnore
	@ManyToOne//(fetch = FetchType.LAZY)
	private MarketPlace marketPlace;
}