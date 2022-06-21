package com.mercado_solidario.api.execption;

public class EntidadeNaoEncontradaExeption extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EntidadeNaoEncontradaExeption(String mensagem) {
		super(mensagem);
	}
	
}
