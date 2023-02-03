package com.mercado_solidario.api.execption;

public class NegocioException  extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NegocioException(String mensagem) {
		super(mensagem);
	}

}
