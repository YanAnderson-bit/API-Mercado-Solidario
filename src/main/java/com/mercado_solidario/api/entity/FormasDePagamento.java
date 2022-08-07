package com.mercado_solidario.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class FormasDePagamento {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String descricao;
	/*
	@JsonIgnore
	@ManyToMany(mappedBy="formasDePagamentos")
    private List<MarketPlace> marketPlaces = new ArrayList<>();

	public void addMarketPlace(MarketPlace marketPlace) {
		this.marketPlaces.add(marketPlace);
		
        //fornecedor.getProdutos().add(this);
    }
 
    public void removeMarketPlace(MarketPlace marketPlace) {
    	this.marketPlaces.remove(marketPlace);
  
  //      fornecedor.getProdutos().remove(this);
    }*/
}

