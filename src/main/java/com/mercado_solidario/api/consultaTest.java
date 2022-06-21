package com.mercado_solidario.api;

import java.util.List;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import com.mercado_solidario.api.entity.Estado;

public class consultaTest {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new SpringApplicationBuilder(ApiMercadoSolidarioApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
		
		testes test = applicationContext.getBean(testes.class);
	
		
		List<Estado> estados = test.listar();
		
		
		for (Estado estado : estados) {
			System.out.println(estado.getNome());
		}
		
		
	}

}
