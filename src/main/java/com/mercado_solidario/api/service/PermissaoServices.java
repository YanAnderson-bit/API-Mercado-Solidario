package com.mercado_solidario.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.mercado_solidario.api.entity.Permissao;
import com.mercado_solidario.api.execption.EntidadeEmUsoExeption;
import com.mercado_solidario.api.execption.EntidadeNaoEncontradaExeption;
import com.mercado_solidario.api.repository.PermissaoRepository;

@Service
public class PermissaoServices {

	@Autowired 
	private PermissaoRepository permissaoRepository;
	
	public Permissao salvar(Permissao permissao) {
		return permissaoRepository.save(permissao);
	}
	
	public void excluir(Long Id){ 
		try {
			permissaoRepository.deleteById(Id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro do permissão de código %d", Id));
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					String.format("Permissão de código %d não pode ser removida por estar em uso", Id));
		}
	}
	
	public Permissao buscarOuFalhar(Long permissionId) {
		return permissaoRepository.findById(permissionId)
			.orElseThrow(() -> new EntidadeNaoEncontradaExeption(
					String.format("Não existe cadastro do estado de código %d", permissionId)));
	}

}
