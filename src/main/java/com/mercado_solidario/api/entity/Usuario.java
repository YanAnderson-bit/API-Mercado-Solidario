package com.mercado_solidario.api.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Usuario {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String nome;
	
	@Column(nullable = true)
	private String email;
	
	@Column(nullable = false)
	private String senha;
	
	@Column(nullable = false)
	private Date dataCadastro;
	
	@JsonIgnore
	//@ManyToOne
	//@JoinColumn(nullable = false)
	@Embedded
	private Endereço endereço;
	
	//@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "grupo_id", nullable = false)
	private Grupo grupo;
	
	@JsonIgnore
	@OneToMany(mappedBy="usuario")
    private List<Pedido> pedidos;


}
