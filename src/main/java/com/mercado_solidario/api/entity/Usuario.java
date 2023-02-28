package com.mercado_solidario.api.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
	
	//@JsonIgnore
	@Column(nullable = false)
	private String senha;
	
	@Column(nullable = false)
	private Date dataCadastro;
	
	//@JsonIgnore
	//@ManyToOne
	//@JoinColumn(nullable = false)
	@Embedded
	/*@AttributeOverrides({
		  @AttributeOverride( name = "firstName", column = @Column(name = "contact_first_name")),
		  @AttributeOverride( name = "lastName", column = @Column(name = "contact_last_name")),
		  @AttributeOverride( name = "phone", column = @Column(name = "contact_phone"))
		})*/
	private Endereço endereço;
	
	//@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	//@JoinColumn(name = "grupo_id", nullable = false)
	//private Grupo grupo;
	@JoinTable(name = "user_grupo", joinColumns =  @JoinColumn(name = "user_id"),
									inverseJoinColumns = @JoinColumn(name = "grupo_id"))
	private Set<Grupo> grupo = new HashSet<>();
	
	@JsonIgnore
	@OneToMany(mappedBy="usuario")
    private List<Pedido> pedidos;
	
	@Column(nullable = false)
	private boolean isNew;

	public boolean isNew() {
		return this.isNew;
	}
	
	public void adicionarGrupo(Grupo grupo) {
		this.grupo.add(grupo);
	}
	
	public void removeGrupo(Grupo grupo) {
		this.grupo.remove(grupo);
	}
	
}
