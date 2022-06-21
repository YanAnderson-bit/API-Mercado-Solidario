package com.mercado_solidario.api;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;

import com.mercado_solidario.api.entity.Estado;

@Component
public class testes {

	@PersistenceContext
	private EntityManager manager;

	public List<Estado> listar(){
		TypedQuery<Estado> query = manager.createQuery("from estado", Estado.class);
	
		return query.getResultList();
	}
	
}
