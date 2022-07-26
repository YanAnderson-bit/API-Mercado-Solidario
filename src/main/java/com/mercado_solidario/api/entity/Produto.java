package com.mercado_solidario.api.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

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
	
	@JsonIgnore
	@ManyToMany(mappedBy="produtos")
    private List<Fornecedor> fornecedores = new ArrayList<>();
	
	public void addFornecedor(Fornecedor fornecedor) {
        this.fornecedores.add(fornecedor);
        fornecedor.getProdutos().add(this);
    }
 
    public void removeFornecedor(Fornecedor fornecedor) {
        this.fornecedores.remove(fornecedor);
        fornecedor.getProdutos().remove(this);
    }
	
}
