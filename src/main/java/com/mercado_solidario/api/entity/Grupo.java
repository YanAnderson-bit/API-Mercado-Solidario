package com.mercado_solidario.api.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

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
	@ManyToMany(fetch = FetchType.EAGER)
	//private List<Permissao> permissao = new ArrayList<>();
	//@JOinTable(name = "Nome_da_Tabela_riada", 
	//		joinColumns = @JoinColumn(name = "nome_coluna")),
	//		inverseJoinColumns = @JoinColumn(name = "nome_da_outa_coluna"))
	@JoinTable(name = "grupo_permissao", joinColumns =  @JoinColumn(name = "grupo_id"),
									inverseJoinColumns = @JoinColumn(name = "permissao_id"))
	private Set<Permissao> permissao = new HashSet<>();
	//private Set<Permissao> permissoes = new HashSet<>();

	//@JsonIgnore
	//@OneToMany(mappedBy = "grupo")
	//private List<Usuario> usuarios = new ArrayList<>();
	
	public void adicionarPermissao(Permissao permissao) {
		this.permissao.add(permissao);
	}
	
	public void removePermissao(Permissao permissao) {
		this.permissao.remove(permissao);
	}
	
	
}
