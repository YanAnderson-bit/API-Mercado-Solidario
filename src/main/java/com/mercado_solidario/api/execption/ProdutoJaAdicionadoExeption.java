package com.mercado_solidario.api.execption;

public class ProdutoJaAdicionadoExeption extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProdutoJaAdicionadoExeption(String mensagem) {
		super(mensagem);
	}

}
