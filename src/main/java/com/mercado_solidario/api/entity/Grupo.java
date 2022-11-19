package com.mercado_solidario.api.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Grupo {
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;
	
	//@JsonIgnore
	@ManyToMany
	//@JOinTable(name = "Nome_da_Tabela_riada", 
	//		joinColumns = @JoinColumn(name = "nome_coluna")),
	//		inverseJoinColumns = @JoinColumn(name = "nome_da_outa_coluna"))
	private List<Permissao> permissao = new ArrayList<>();
	
	//@JsonIgnore
	@OneToMany(mappedBy = "grupo")
	private List<Usuario> usuarios = new ArrayList<>();
	
	
}
